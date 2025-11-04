package main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MainMenu extends JPanel {
    private GameWindow gameWindow;
    private int selectedOption = 0;
    private int selectedSureOption = 0;
    private boolean estasSeguro = false;
    private String[] menuOptions;
    private String[] sureOptions = {
    		"Si",
    		"No"
    };
    private boolean hayPartidaGuardada = false;
  
    
    public MainMenu(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        GameWindow.cargar_font();
        setFocusable(true);
        requestFocusInWindow();
        
        // Verificar si existe una partida guardada
        hayPartidaGuardada = GameSaveManager.existePartidaGuardada();

        actualizarOpciones();
    }
    
    public void actualizarOpciones() {
        hayPartidaGuardada = GameSaveManager.existePartidaGuardada();

        if (hayPartidaGuardada) {
            menuOptions = new String[]{"CONTINUAR", "NUEVA PARTIDA", "NIVELES",  "CONFIGURACION", "SALIR"};
        } else {
            menuOptions = new String[]{"JUGAR", "NIVELES", "CONFIGURACION", "SALIR"};
        }

        selectedOption = 0; // resetear selección
        repaint();
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Título del juego
        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(145f)));
        FontMetrics titleMetrics = g2d.getFontMetrics();
        String title = "La odisea de Moya";
        int titleX = (width - titleMetrics.stringWidth(title)) / 2;
        int titleY = height / 3;
        g2d.drawString(title, titleX, titleY);
        
        // Opciones del menú
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(75f)));
        FontMetrics menuMetrics = g2d.getFontMetrics();
        
        int menuStartY = height / 2;
        int menuSpacing = height / 13;
        
        for (int i = 0; i < menuOptions.length; i++) {
            String option = menuOptions[i];
            int optionX = (width - menuMetrics.stringWidth(option)) / 2;
            int optionY = menuStartY + (i * menuSpacing);
            
            // Resaltar opción seleccionada
            if (i == selectedOption) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("> " + option + " <", optionX - 50, optionY);
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString(option, optionX, optionY);
            }
        }
        
        // Instrucciones
        g2d.setColor(Color.GRAY);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(45f)));
        FontMetrics instructionMetrics = g2d.getFontMetrics();
        String instructions = "Usa las flechas ARRIBA/ABAJO para navegar, ENTER para seleccionar";
        int instructionX = (width - instructionMetrics.stringWidth(instructions)) / 2;
        int instructionY = height - height / 10;
        g2d.drawString(instructions, instructionX, instructionY);
        
        if(estasSeguro) {
        	
        	g2d.setColor(Color.BLACK);
        	g2d.fillRect(GW.SX(520), GW.SY(460), GW.SX(890), GW.SY(300));
        	
        	// Dibujar borde
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(GW.SX(520), GW.SY(460), GW.SX(890), GW.SY(300));
            
            // Textos
            String texto = "Estás seguro?";
            int textoX = (width - instructionMetrics.stringWidth(texto)) / 2;
            g2d.drawString(texto, textoX, GW.SY(520));
            
            g2d.setColor(Color.RED);
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(30)));
            String texto1 = "crear una nueva partida sobreescribirá los datos anteriores";
            FontMetrics metrics = g2d.getFontMetrics();
            int texto1X = (width - metrics.stringWidth(texto1)) / 2;
            g2d.drawString(texto1, texto1X, GW.SY(560));
            
            // Opciones
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(60)));
            int Spacing = GW.SX(460);
            int startX = GW.SX(685);
            for(int i = 0; i< sureOptions.length; i++) {
            	String option = sureOptions[i];
                int optionX = startX + (i * Spacing);
                
                // Resaltar opción seleccionada
                if (i == selectedSureOption) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString("> " + option + " <", optionX - 50, GW.SY(720));
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(option, optionX, GW.SY(720));
                }
            }
        }
    }
    
    public void handleKeyPress(int keyCode) {
    	
    	if(estasSeguro) {
    		switch(keyCode) {
    		case KeyEvent.VK_RIGHT:
    			selectedSureOption = (selectedSureOption - 1 + sureOptions.length) % sureOptions.length;
                repaint();
                break;
                
    		case KeyEvent.VK_LEFT:
                selectedSureOption = (selectedSureOption + 1) % sureOptions.length;
                repaint();
                break;
                
    		case KeyEvent.VK_ENTER:
                selectSureOption();
                break;
        	}
    	} else {
    		switch (keyCode) {
            
            case KeyEvent.VK_UP:
                selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                repaint();
                break;
            case KeyEvent.VK_DOWN:
                selectedOption = (selectedOption + 1) % menuOptions.length;
                repaint();
                break;
            case KeyEvent.VK_ENTER:
                selectOption();
                break;
            case KeyEvent.VK_ESCAPE:
                gameWindow.exitGame();
                break;
        }
    	}
    	
        
    }
    
    private void selectOption() {
        if (hayPartidaGuardada) {
            // Menú con partida guardada: CONTINUAR, NUEVA PARTIDA, CONFIGURACION, SALIR
            switch (selectedOption) {
                case 0: // CONTINUAR
                    gameWindow.continueGame();
                    break;
                case 1: // NUEVA PARTIDA
                    // Preguntar si quiere sobrescribir
                	estasSeguro = true;
                	repaint();
                    break;
                case 2: // SELECTOR NIVELES
                	gameWindow.showNiveles();
                	break;
                case 3: // SETTINGS
                    gameWindow.settingsGame();
                    break;
                case 4: // EXIT
                    gameWindow.exitGame();
                    break;
            }
        } else {
            // Menú sin partida guardada: JUGAR, CONFIGURACION, SALIR
            switch (selectedOption) {
                case 0: // PLAY
                    gameWindow.showStory(1);
                    break;
                case 1: // SELECTOR NIVELES
                	gameWindow.showNiveles();
                	break;
                case 2: // SETTINGS
                    gameWindow.settingsGame();
                    break;
                case 3: // EXIT
                    gameWindow.exitGame();
                    break;
            }
        }
    }
    
    private void selectSureOption() {
    	switch(selectedSureOption) {
    	case 0: // Reiniciar datos
    		estasSeguro = false;
    		GameSaveManager.eliminarPartida();
            gameWindow.showStory(1);
    		break;
    		
    	case 1: // No reiniciar
    		estasSeguro = false;
    		selectedSureOption = 0;
    		repaint();
    		break;
    	}
    }
}



