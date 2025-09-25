import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameWindow extends JFrame implements KeyListener {
    private static final int ORIGINAL_WIDTH = 1920;
    private static final int ORIGINAL_HEIGHT = 1080;
    private static final double ASPECT_RATIO = 16.0 / 9.0;
    
    private GameState currentState;
    private MainMenu mainMenu;
    private GamePanel gamePanel;
    private GameThread gameThread;
    
    public GameWindow() {
        setTitle("La odisea de moya");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); 
        
        // Configurar pantalla completa manteniendo aspect ratio
        setupFullScreen();
        
        // Inicializar estados
        currentState = GameState.MAIN_MENU;
        mainMenu = new MainMenu(this);
        gamePanel = new GamePanel(this);
        
        // Configurar listeners
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Mostrar menú principal inicialmente
        showMainMenu();
    }
    
    private void setupFullScreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = gd.getDisplayMode();
        
        int screenWidth = displayMode.getWidth();
        int screenHeight = displayMode.getHeight();
        
        // Calcular dimensiones manteniendo aspect ratio 16:9
        int gameWidth, gameHeight;
        double screenRatio = (double) screenWidth / screenHeight;
        
        if (screenRatio > ASPECT_RATIO) {
            // Pantalla más ancha que 16:9 - ajustar por altura
            gameHeight = screenHeight;
            gameWidth = (int) (gameHeight * ASPECT_RATIO);
        } else {
            // Pantalla más estrecha que 16:9 - ajustar por anchura
            gameWidth = screenWidth;
            gameHeight = (int) (gameWidth / ASPECT_RATIO);
        }
        
        setSize(gameWidth, gameHeight);
        setLocationRelativeTo(null);
        
        // Intentar pantalla completa real si es posible
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        }
    }
    
    public void start() {
        setVisible(true);
    }
    
    public void showMainMenu() {
        currentState = GameState.MAIN_MENU;
        getContentPane().removeAll();
        getContentPane().add(mainMenu);
        revalidate();
        repaint();
        requestFocus();
    }
    
    public void startGame() {
        currentState = GameState.PLAYING;
        getContentPane().removeAll();
        getContentPane().add(gamePanel);
        revalidate();
        repaint();
        requestFocus();
        
        // Iniciar thread del juego
        if (gameThread == null || !gameThread.isRunning()) {
            gameThread = new GameThread(gamePanel);
            gameThread.start();
        }
    }
    
    public void exitGame() {
        if (gameThread != null) {
            gameThread.stopGame();
        }
        System.exit(0);
    }
    
    public void backToMenu() {
        if (gameThread != null) {
            gameThread.stopGame();
        }
        showMainMenu();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (currentState == GameState.MAIN_MENU) {
            mainMenu.handleKeyPress(e.getKeyCode());
        } else if (currentState == GameState.PLAYING) {
            gamePanel.handleKeyPress(e.getKeyCode());
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (currentState == GameState.PLAYING) {
            gamePanel.handleKeyRelease(e.getKeyCode());
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // No se usa
    }
}