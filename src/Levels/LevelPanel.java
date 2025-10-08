package Levels;

import entities.arrow;
import main.GW;
import main.GameThread;
import main.GameWindow;
import main.GameThread.Updatable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

public class LevelPanel extends JPanel implements GameThread.Updatable {
	
    private GameWindow gameWindow;
    private List<arrow> arrows;
    
    private boolean[] columnPressed = new boolean[4];
    
    // Estadísticas
    private int puntaje = 0;
    private int combo = 0;
    private int maxCombo = 0;
    
    // Vida
    private int Max_vida = 50;
    private int vida = Max_vida/2;
    private int barraAnchoMax = GW.SX(1350);
    
    // Efectos
    private String lastHitText = "";
    private long hitDisplayTime = 0;
    private static final long HIT_TEXT_DURATION = 600; 
    
    // Contadores
    private int sigmaCount = 0;
    private int auraCount = 0;
    private int bueCount = 0;
    private int peteCount = 0;
    private int missCount = 0;
    
    // Juego
    private boolean win = false;
    private boolean pausa = false;
    private String level;
    private int speed;
    
    // Pausa
    private String[] pauseOptions = {"Continuar", "Reiniciar", "Salir"};
    private int selectedPauseOption = 0;


    public LevelPanel(GameWindow gw, String levelName, int speed) {
        this.gameWindow = gw;
        this.level = levelName;
        this.speed = speed;
        setBackground(Color.BLACK);
        setFocusable(true);

        
        arrows = ChartLoader.loadChart(new File("resources/Levels/"+levelName+".txt"), gw, true, GW.SQ(speed));
    }

    public void update() {
    	
    	if(pausa) {
    		return;
    	}
    	
    	// Perder
    	if(vida <= 0) {
    		System.out.println("perdiste");
    	}
    	
    	// Max Vida
    	if(vida > Max_vida) {
    		vida = Max_vida;
    	}
    	
        for (arrow a : arrows) {
            a.move();
        }
        
        // Desaparecer si se va o si llega a las notas del enemigo
        for (int i = 0; i < arrows.size(); i++) {
            arrow a = arrows.get(i);

            // Si es nota de fin
            if (a.isEnd && a.y <= GW.SY(125)) {
                win = true;
                return;
            }

            if (a.y < GW.SY(125) && a.x < GW.SX(1200)) {
                arrows.remove(i);
            } else if (a.y + a.size < 0) { // notas del jugador
                arrows.remove(i);
                combo = 0;
                vida -= 3;
                missCount++;
            }
        }
        
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Teclas Jugador
    	int posX = GW.SX(1250);
    	for(int i = 0; i<4; i++) {
    		g2d.setColor(new Color(135,135,135,125));
    		if(columnPressed[i]) {
    			g2d.setColor(new Color(200,200,200,125));
    		}
    		g2d.fillRect(posX, GW.SY(125), GW.SX(80), GW.SY(80));
    		posX += GW.SX(125);
    	}
    	
    	// Teclas Enemigo
    	posX = GW.SX(225);
    	for(int i = 0; i<4; i++) {
    		g2d.setColor(new Color(155,155,155,125));
    		g2d.fillRect(posX, GW.SY(125), GW.SX(80), GW.SY(80));
    		posX += GW.SX(125);
    	}
        

        for (arrow a : arrows) {
            a.draw(g2d);
        }
        
        drawUI(g2d);

        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(15f));
        g2d.drawString("Juego de ritmo - presiona ESC para volver", 50, 50);
        
        
        // PANTALLA DE PAUSA
        if(pausa) {
        	drawPAUSE(g2d);
        }
        
        // PANTALLA DE WIN
        if(win) {
        	drawWIN(g2d);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
    	
    	
    	// Vida Jugador
    	g2d.setColor(Color.RED);
        g2d.fillRect(GW.SX(270), GW.SY(900), barraAnchoMax, GW.SY(20));
        g2d.setColor(Color.BLACK);
        
        int barraVida = (int)((vida / (double)Max_vida) * barraAnchoMax);
        g2d.setColor(Color.GREEN);
        int barraVida_PosX = (GW.SX(270) + barraAnchoMax) - barraVida;
        g2d.fillRect(barraVida_PosX, GW.SY(900), barraVida, GW.SY(20));
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(GW.SX(270), GW.SY(900), barraAnchoMax, GW.SY(20));
        
    	
    	// Puntaje Jugador
    	g2d.setColor(Color.WHITE);
    	g2d.setFont(GameWindow.Pixelart.deriveFont(25f));
    	g2d.drawString("Puntaje: " + puntaje, GW.SX(850), GW.SY(1000));
    	
    	// Mostrar combo
        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(25f));
        g2d.drawString("Combo: " + combo, GW.SX(1250), GW.SY(1000));
        
        // Mostrar porcentaje
        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(25f));
        g2d.drawString("Precisión: " + getAccuracyPercentage() + "%", GW.SX(450), GW.SY(1000));

        // Mostrar texto del hit
        if (!lastHitText.isEmpty() && System.currentTimeMillis() - hitDisplayTime < HIT_TEXT_DURATION) {
        	if(lastHitText.equals("SIGMA")) {
        		g2d.setColor(new Color(200,0,255));
        	} else if(lastHitText.equals("Aura")) {
        		g2d.setColor(new Color(205,205,50));
        	} else if(lastHitText.equals("Bue")) {
        		g2d.setColor(new Color(50,200,50));
        	} else {
        		g2d.setColor(new Color(120, 0, 0));
        	}
            
            g2d.setFont(GameWindow.Pixelart.deriveFont(30f));
            int textWidth = g2d.getFontMetrics().stringWidth(lastHitText);
            g2d.drawString(lastHitText, GW.SX(1475 - textWidth), GW.SY(400));
        }
    }
    
    private void drawWIN(Graphics2D g2d) {
    	double porcentajeFinal = getAccuracyPercentage();
    	
    	// Oscurecer fondo
    	g2d.setColor(new Color(0,0,0,150));
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(45f));
    	g2d.setColor(Color.WHITE);
    	g2d.drawString("PUNTUACION", GW.SX(800), GW.SY(250));
    	
    	// Imagen de la nota final
    	if(porcentajeFinal == 100) {
    		g2d.drawImage(new ImageIcon("resources/Sprites/rankings/rankSS.png").getImage(), GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	} else if (porcentajeFinal >= 95) {
    		g2d.drawImage(new ImageIcon("resources/Sprites/rankings/rankS.png").getImage(), GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	} else if (porcentajeFinal >= 90) {
    		g2d.drawImage(new ImageIcon("resources/Sprites/rankings/rankA.png").getImage(), GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	} else if (porcentajeFinal >= 80) {
    		g2d.drawImage(new ImageIcon("resources/Sprites/rankings/rankB.png").getImage(), GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this); 		
    	} else if (porcentajeFinal >= 70) {
    		g2d.drawImage(new ImageIcon("resources/Sprites/rankings/rankC.png").getImage(), GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	} else {
    		g2d.drawImage(new ImageIcon("resources/Sprites/rankings/rankD.png").getImage(), GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	}
    	
    	// Porcentaje
    	g2d.drawString(porcentajeFinal+"%", GW.SX(600), GW.SY(700));
    	
    	// Max Combo
    	int totalHits = sigmaCount + auraCount + bueCount + peteCount + missCount;
    	g2d.setFont(GameWindow.Pixelart.deriveFont(25f));
    	if(maxCombo == totalHits) {
    		g2d.setColor(Color.YELLOW);
    	}
    	g2d.drawString("Max Combo: " + maxCombo, GW.SX(600), GW.SY(750));
    	
    	// Estadísticas
    	g2d.setFont(GameWindow.Pixelart.deriveFont(35f));
    	g2d.setColor(new Color(200,0,255));
    	g2d.drawString("(" + porcentaje(sigmaCount, totalHits) + "%) SIGMA: " + sigmaCount, GW.SX(1050), GW.SY(400));
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(35f));
    	g2d.setColor(new Color(205,205,50));
    	g2d.drawString("(" + porcentaje(auraCount, totalHits) + "%) Aura: " + auraCount, GW.SX(1050), GW.SY(475));
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(35f));
    	g2d.setColor(new Color(50,200,50));
    	g2d.drawString("(" + porcentaje(bueCount, totalHits) + "%) Bue: " + bueCount, GW.SX(1050), GW.SY(550));
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(35f));
    	g2d.setColor(new Color(120, 0, 0));
    	g2d.drawString("(" + porcentaje(peteCount, totalHits) + "%) Pete: " + peteCount, GW.SX(1050), GW.SY(625));
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(35f));
    	g2d.setColor(Color.WHITE);
    	g2d.drawString("(" + porcentaje(missCount, totalHits) + "%) Misses: " + missCount, GW.SX(1050), GW.SY(700));
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(40f));
    	g2d.setColor(Color.WHITE);
    	g2d.drawString("ESC para volver", GW.SX(800), GW.SY(880));
    }
    
    private void drawPAUSE(Graphics2D g2d) {
    	
    	// Oscurecer fondo
    	g2d.setColor(new Color(0,0,0,150));
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	
    	
    	g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(80f));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "PAUSA";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, GW.SY(350));
        
        // Opciones
        g2d.setFont(GameWindow.Pixelart.deriveFont(55f));
        FontMetrics optionMetrics = g2d.getFontMetrics();
        int startY = GW.SY(600);
        int spacing = GW.SY(100);

        for (int i = 0; i < pauseOptions.length; i++) {
            String option = pauseOptions[i];
            int optionX = (getWidth() - optionMetrics.stringWidth(option)) / 2;
            int optionY = startY + (i * spacing);

            if (i == selectedPauseOption) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("> " + option + " <", optionX - 50, optionY);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString(option, optionX, optionY);
            }
        }
        
    }
    
    // Calcular porcentaje
    
    public double getAccuracyPercentage() {
        int totalHits = sigmaCount + auraCount + bueCount + peteCount + missCount;
        if (totalHits == 0) return 0.0;

        double scoreSum = sigmaCount * 100 + auraCount * 90 + bueCount * 50 + peteCount * 20 + missCount * 0;
        double percentage = scoreSum / totalHits;
        return Math.round(percentage * 100.0) / 100.0; // redondear a 2 decimales
    }
    
    public double porcentaje(int numero, int total) {
        if (total == 0) return 0.0;
        double percentage = ((double) numero / (double) total) * 100.0;
        return Math.round(percentage * 100.0) / 100.0; // redondea a 2 decimales
    }


    
    // Chequear la columna segun la tecla
    
    private void checkHit(int column) {
        int hitY = GW.SY(150);         
        int WindowPete = GW.SY(150);
        int WindowBue = GW.SY(85);
        int WindowAura = GW.SY(60);
        int WindowSigma = GW.SY(35);
        
        int SumaV;
        
        for (int i = 0; i < arrows.size(); i++) {
            arrow a = arrows.get(i);

            int arrowColumn = getColumnFromX((int)a.x);
            if (arrowColumn == column) {
                if (Math.abs(a.y - hitY) <= WindowPete) {

                    if (Math.abs(a.y - hitY) <= WindowSigma) {
                    	sigmaCount++;
                        lastHitText = "SIGMA";
                        puntaje += 200;
                        combo++;
                        SumaV = 2;
                        
                    } else if (Math.abs(a.y - hitY) <= WindowAura) {
                    	auraCount++;
                        lastHitText = "Aura";
                        puntaje += 100;
                        combo++;
                        SumaV = 1;
                        
                    } else if (Math.abs(a.y - hitY) <= WindowBue) {
                    	bueCount++;
                        lastHitText = "Bue";
                        puntaje += 50;
                        combo++;
                        SumaV = 0;
                    } else {
                    	peteCount++;
                        lastHitText = "Pete";
                        puntaje += 25;
                        combo = 0; // resetear combo si pegaste mal
                        SumaV = -2;
                    }
                    
                    if(combo > maxCombo) {
                    	maxCombo = combo;
                    }
                    
                    // Sumar o Restar vida
                    if(vida < Max_vida) {
                    	vida += SumaV;
                    }

                    hitDisplayTime = System.currentTimeMillis(); // empieza a mostrar el texto
                    arrows.remove(i);
                    return;
                }
            }
        }
    }

    
    private int getColumnFromX(int x) {
        int baseX = GW.SX(1250); // donde arranca la primera columna del jugador
        int spacing = GW.SX(125); // distancia entre columnas
        for (int i = 0; i < 4; i++) {
            int colX = baseX + i * spacing;
            if (Math.abs(x - colX) < spacing / 2) {
                return i;
            }
        }
        return -1;
    }

    
    // Detectar teclas
    public void handleKeyPress(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
        	
        	if(win) {
        		gameWindow.startGame(); // volver al RPG
                return;
        	} else {
        		pausa = !pausa;
        	}
            
        }
        
        if (pausa) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    selectedPauseOption = (selectedPauseOption - 1 + pauseOptions.length) % pauseOptions.length;
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    selectedPauseOption = (selectedPauseOption + 1) % pauseOptions.length;
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                    repaint();
                    break;
                case KeyEvent.VK_ENTER:
                    handlePauseSelection();
                    break;
            }
            return;
        }
        
        int column = -1;
        if (keyCode == KeyEvent.VK_D) {
        	column = 0;
        	columnPressed[0] = true;
        }
        else if (keyCode == KeyEvent.VK_F) {
        	column = 1;
        	columnPressed[1] = true;
        }
        else if (keyCode == KeyEvent.VK_J) {
        	column = 2;
        	columnPressed[2] = true;
        }
        else if (keyCode == KeyEvent.VK_K) {
        	column = 3;
        	columnPressed[3] = true;
        }

        if (column != -1) {
            checkHit(column);
        }
    }

    public void handleKeyRelease(int keyCode) {
    	if (keyCode == KeyEvent.VK_D) {
        	columnPressed[0] = false;
        }
        else if (keyCode == KeyEvent.VK_F) {
        	columnPressed[1] = false;
        }
        else if (keyCode == KeyEvent.VK_J) {
        	columnPressed[2] = false;
        }
        else if (keyCode == KeyEvent.VK_K) {
        	columnPressed[3] = false;
        }
    }
    
    private void handlePauseSelection() {
        switch (selectedPauseOption) {
            case 0: // Continuar
                pausa = false;
                break;
            case 1: // Reiniciar
            	gameWindow.startRitmo(level, speed); // Necesitás crear este método en GameWindow
                break;
            case 2: // Salir
                gameWindow.startGame(); // volver al RPG
                break;
        }
    }
    
}
