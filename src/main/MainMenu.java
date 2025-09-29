package main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MainMenu extends JPanel {
    private GameWindow gameWindow;
    private int selectedOption = 0;
    private String[] menuOptions = {"PLAY", "EXIT"};
    
    public MainMenu(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        GameWindow.cargar_font();
        setFocusable(true);
        requestFocusInWindow();
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
        g2d.setFont(GameWindow.Pixelart.deriveFont(120f));
        FontMetrics titleMetrics = g2d.getFontMetrics();
        String title = "La odisea de Moya";
        int titleX = (width - titleMetrics.stringWidth(title)) / 2;
        int titleY = height / 3;
        g2d.drawString(title, titleX, titleY);
        
        // Opciones del menú
        g2d.setFont(GameWindow.Pixelart.deriveFont(60f));
        FontMetrics menuMetrics = g2d.getFontMetrics();
        
        int menuStartY = height / 2;
        int menuSpacing = height / 15;
        
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
        g2d.setFont(GameWindow.Pixelart.deriveFont(35f));
        FontMetrics instructionMetrics = g2d.getFontMetrics();
        String instructions = "Usa las flechas ARRIBA/ABAJO para navegar, ENTER para seleccionar";
        int instructionX = (width - instructionMetrics.stringWidth(instructions)) / 2;
        int instructionY = height - height / 10;
        g2d.drawString(instructions, instructionX, instructionY);
    }
    
    public void handleKeyPress(int keyCode) {
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
    
    private void selectOption() {
        switch (selectedOption) {
            case 0: // PLAY
                gameWindow.startGame();
                break;
            case 1: // EXIT
                gameWindow.exitGame();
                break;
        }
    }
}