package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;

public class Niveles extends JPanel {
    private GameWindow gameWindow;
    public int selectedOption = 0;
    public int scrollOffset = 0;
    private int scrollSpeed = 50;
    public boolean ignorarProximoEnter = true;

    private static final int TOTAL_NIVELES = 13;

    // Estados de desbloqueo de niveles (se pueden cargar del ProfileManager)
    public static boolean[] nivelesDesbloqueados = new boolean[TOTAL_NIVELES];

    public Niveles(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        setLayout(null);

        // Ejemplo temporal (desbloquea algunos niveles)
        nivelesDesbloqueados[0] = true; // Nivel 1 desbloqueado siempre
        nivelesDesbloqueados[1] = true;
        nivelesDesbloqueados[2] = true;

        // --- KEY BINDINGS robustos (funcionan aunque el foco no esté exactamente en el panel) ---
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("UP"), "niv_up");
        am.put("niv_up", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                moveUp();
            }
        });

        im.put(KeyStroke.getKeyStroke("DOWN"), "niv_down");
        am.put("niv_down", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                moveDown();
            }
        });

        im.put(KeyStroke.getKeyStroke("ENTER"), "niv_enter");
        am.put("niv_enter", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            	if(ignorarProximoEnter) {
            		ignorarProximoEnter = false;
            		return;
            	}
                seleccionarNivel();
            }
        });

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "niv_escape");
        am.put("niv_escape", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                // Vuelve al menú o estado anterior
                gameWindow.showMainMenu();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
    	int nivelIndex = this.selectedOption;
    	
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // === Título ===
        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(80f)));
        String title = "SELECCIÓN DE NIVEL";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (width + GW.SX(00) - titleWidth) / 2, height / 5);

        // === Lista de niveles ===
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(70f)));

        int startY = height / 3;
        int spacing = height / 12;

        // Base X (alineado a la izquierda)
        int baseX = GW.SX(250);

        for (int i = 0; i < TOTAL_NIVELES; i++) {
            int y = startY + (i * spacing) - scrollOffset;

            // No dibujar si está fuera de vista (pequeño margen arriba/abajo)
            if (y < height / 3 - spacing + GW.SY(60) || y > height - height / 4 + spacing) continue;

            String texto = nivelesDesbloqueados[i] ? (LeveltoName(i)) : "???";

            if (i == selectedOption) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("  " + texto, baseX, y);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString(texto, baseX, y);
            }
        }
        
        if (nivelesDesbloqueados[nivelIndex]) { 
            
            // --- Titulo ---
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(60f)));
            g2d.setColor(Color.YELLOW);
            // Ajustamos la posición X para que esté a la derecha
            g2d.drawString("TOP SCORES", GW.SX(1350), GW.SY(400)); 
            
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(35f)));
            int topListStartY = GW.SY(500); // Mover el inicio más arriba para que quepan 5
            int topListSpacing = GW.SY(50);
            int topListBaseX = GW.SX(1350); // Posición X para la lista
            
            // Obtener la matriz de Top Scores de ProfileManager
            double[][] scores = ProfileManager.topScoresPorNivel;
            String[][] nombres = ProfileManager.topNombresPorNivel;
            
            // Verificamos que ProfileManager exista y que el índice sea válido
            if (scores != null && nivelIndex >= 0 && nivelIndex < scores.length) {
                
                for (int i = 0; i < ProfileManager.MAX_TOP_ENTRIES; i++) {
                    double score = scores[nivelIndex][i];
                    String nombre = nombres[nivelIndex][i];
                    
                    int y = topListStartY + (i * topListSpacing);
                    
                    if (score > 0.0) { // Solo dibujar si hay una puntuación válida
                        String rankText = String.format("%d. %s: %.2f%%", 
                            (i + 1), 
                            nombre != null ? nombre : "---",
                            score
                        );
                        
                        // Usar color diferente para el primer puesto
                        g2d.setColor(i == 0 ? Color.CYAN : Color.WHITE); 
                        g2d.drawString(rankText, topListBaseX, y);
                    } else {
                        // Dibujar una entrada vacía si no hay score
                        g2d.setColor(Color.GRAY);
                        g2d.drawString(String.format("%d. Vacio", (i + 1)), topListBaseX, y);
                    }
                }
            }
        }

        // === Instrucciones ===
        g2d.setColor(Color.GRAY);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(40f)));
        String instructions = "Usa ↑ ↓ para moverte | ENTER para seleccionar | ESC para volver";
        int textWidth = g2d.getFontMetrics().stringWidth(instructions);
        g2d.drawString(instructions, (width - textWidth) / 2, height - height / 12);
    }

    // Métodos usados por los bindings
    private void moveUp() {
        if (selectedOption > 0) {
            selectedOption--;
            GameWindow.reproducirSonido("resources/sounds/menu.wav");
            ajustarScroll();
            repaint();
        }
    }

    private void moveDown() {
        if (selectedOption < TOTAL_NIVELES - 1) {
            selectedOption++;
            GameWindow.reproducirSonido("resources/sounds/menu.wav");
            ajustarScroll();
            repaint();
        }
    }

    private void ajustarScroll() {
        int height = getHeight();
        int spacing = Math.max(1, height / 12);
        int startY = height / 3; 
        
        // Posición del elemento seleccionado si no hubiera scroll:
        int itemYNoScroll = startY + (selectedOption * spacing);
        int referenceY = startY + (3 * spacing); // Aproximadamente 3 opciones abajo del inicio

        int newScrollOffset = itemYNoScroll - referenceY;
        if (newScrollOffset < 0) {
            newScrollOffset = 0;
        }
        int totalOptionsHeight = TOTAL_NIVELES * spacing;
        
        int viewportHeight = 7 * spacing; 

        if (totalOptionsHeight > viewportHeight) {
            int maxScroll = totalOptionsHeight - viewportHeight;
            
            int maxPossibleScroll = Math.max(0, (TOTAL_NIVELES - 1) * spacing - (height - startY - spacing));
            
            int maxLimitScroll = itemYNoScroll - referenceY; // Scroll que lleva la última opción a referenceY
            int visibleBottomY = height - height / 8; 

            int lastItemYNoScroll = startY + (TOTAL_NIVELES - 1) * spacing;
            int maxScrollLimit = Math.max(0, lastItemYNoScroll - visibleBottomY + spacing + (height/24)); 
            
            if (newScrollOffset > maxScrollLimit) {
                newScrollOffset = maxScrollLimit;
            }
        }
        
        scrollOffset = newScrollOffset;
    }

    private void seleccionarNivel() {
        if (!nivelesDesbloqueados[selectedOption]) {
            GameWindow.reproducirSonido("resources/sounds/error.wav");
            return;
        }

        GameWindow.reproducirSonido("resources/sounds/menu.wav");
        switch(LeveltoName(selectedOption)) {
        
        case "Pacheco":
        	gameWindow.startRitmo("Pacheco", 5, -1);
        	break;
        
        case "Melody":
        	gameWindow.startRitmo("Melody", 7, 135);
        	break;
        	
        case "Gennuso":
        	gameWindow.startRitmo("Gennuso", 5, -1);
        	break;
        	
        case "Vagos":
        	gameWindow.startRitmo("Vagos", 5, 172);
        	break;
        	
        case "Signorello":
        	gameWindow.startRitmo("Signorello", 8, 178);
        	break;
        	
        case "Ledesma":
        	gameWindow.startRitmo("Ledesma", 10, 102);
        	break;
        	
        case "Casas":
        	gameWindow.startRitmo("Casas", 10, 162);
        	break;
        	
        case "Rita":
        	gameWindow.startRitmo("Rita", 10, 184);
        	break;
        	
        case "Martin":
        	gameWindow.startRitmo("Martin", 10, 130);
        	break;
        	
        case "Pecile":
        	gameWindow.startRitmo("Pecile", 11, 200);
        	break;
        	
        case "Moya":
        	gameWindow.startRitmo("Moya", 12, 153);
        	break;
        	
        case "Linzalata":
        	gameWindow.startRitmo("Linzalata", 13, 222);
        	break;
        	
        case "Ricky":
        	gameWindow.startRitmo("Ricky", 13, 170);
        	break;
        }
    }
    
    private String LeveltoName(int nivel) {
    	
    	String name = "";
    	
    	switch(nivel) {
    	case 0:
    		name = "Pacheco";
    		break;
    		
    	case 1:
    		name = "Melody";
    		break;
    		
    	case 2:
    		name = "Gennuso";
    		break;
    		
    	case 3:
    		name = "Vagos";
    		break;
    		
    	case 4:
    		name = "Signorello";
    		break;
    		
    	case 5:
    		name = "Ledesma";
    		break;
    		
    	case 6:
    		name = "Casas";
    		break;
    		
    	case 7:
    		name = "Rita";
    		break;
    		
    	case 8:
    		name = "Martin";
    		break;
    		
    	case 9:
    		name = "Pecile";
    		break;
    		
    	case 10:
    		name = "Moya";
    		break;
    		
    	case 11:
    		name = "Linzalata";
    		break;
    		
    	case 12:
    		name = "Ricky";
    		break;
    	}
    	
    	return name;
    }

    /**
     * Método público por si en algún lugar preferís reenviar teclas desde GameWindow
     */
    public void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP: moveUp(); break;
            case KeyEvent.VK_DOWN: moveDown(); break;
            case KeyEvent.VK_ENTER: seleccionarNivel(); break;
            case KeyEvent.VK_ESCAPE: gameWindow.returnFromSettings(); break;
        }
    }
}
