package main;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

import Levels.LevelPanel;
import main.GameThread.Updatable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

public class GameWindow extends JFrame implements KeyListener {
    private static final int ORIGINAL_WIDTH = 1920;
    private static final int ORIGINAL_HEIGHT = 1080;
    private static final double ASPECT_RATIO = 16.0 / 9.0;
    
    private GameState currentState;
    private GameState previousState;
    private MainMenu mainMenu;
    private GamePanel gamePanel;
    private GameThread gameThread;    
    private GameSettings gameSettings;
    private LevelPanel levelPanel;
    public static GameWindow instance;

    
    public static Font Pixelart; // Font
    
    public GameWindow() {
    	instance = this;
    	
        setTitle("La odisea de moya");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); 
        
        // Configurar pantalla completa manteniendo aspect ratio
        setupFullScreen();
        
        // Inicializar estados
        currentState = GameState.MAIN_MENU;
        mainMenu = new MainMenu(this);
        gamePanel = new GamePanel(this);
        gameSettings = new GameSettings(this);

        // Configurar listeners
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Mostrar menú principal inicialmente
        showMainMenu();
    }
    
    public static void reproducirSonido(String rutaArchivo) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(rutaArchivo));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (Exception e) {
            System.out.println("Error al reproducir sonido: " + e.getMessage());
        }
    }
    
    public static void cargar_font() {
    	try {
			File fuenteArchivo = new File("resources/font/Pixellari.ttf");

			// Crear la fuente
			Pixelart = Font.createFont(Font.TRUETYPE_FONT, fuenteArchivo);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Pixelart);

			Pixelart = Pixelart.deriveFont(24f); // Tamaño default

		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
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

        startGameThread(gamePanel);
    }
    
    public void startRitmo(String levelName, int speed, int bpm) {
        currentState = GameState.RITMO;
        getContentPane().removeAll();
        levelPanel = new LevelPanel(this, levelName, speed, bpm);
        getContentPane().add(levelPanel);
        revalidate();
        repaint();
        requestFocus();

        startGameThread(levelPanel); //  en vez de crear el thread directamente
    }
    
    public void settingsGame() {
        previousState = currentState;

        if (gameThread != null && gameThread.isRunning()) {
            gameThread.stopGame();
            gameThread = null;
        }

        getContentPane().removeAll();
        getContentPane().add(gameSettings);
        revalidate();
        repaint();
        gameSettings.requestFocusInWindow();

        // Resetear estados
        currentState = GameState.SETTINGS;
        gameSettings.esperandoTecla = false;
        gameSettings.ignorarProximoEnter = true;
    }

    
    public void returnFromSettings() {
        if (previousState == GameState.RITMO && levelPanel != null) {
            // Volver al levelPanel (modo ritmo)
            getContentPane().removeAll();
            getContentPane().add(levelPanel);
            revalidate();
            repaint();
            requestFocus();
            currentState = GameState.RITMO;
            
            startGameThread(levelPanel);
        } 
        else if (previousState == GameState.PLAYING && gamePanel != null) {
            getContentPane().removeAll();
            getContentPane().add(gamePanel);
            revalidate();
            repaint();
            requestFocus();
            currentState = GameState.PLAYING;
            
            startGameThread(gamePanel);
        } else if (previousState == GameState.MAIN_MENU && gamePanel != null) {
            getContentPane().removeAll();
            getContentPane().add(mainMenu);
            revalidate();
            repaint();
            requestFocus();
            currentState = GameState.MAIN_MENU;
        }
    }

    
    private void startGameThread(Updatable panel) {
        if (gameThread != null && gameThread.isRunning()) {
            gameThread.stopGame(); // detiene el thread anterior
        }
        gameThread = new GameThread(panel);
        gameThread.start();
    }

    
    
    // Relativizar valores
    
    public int scaleX(int originalX) {
        return (int) (originalX * (getWidth() / (double) ORIGINAL_WIDTH));
    }

    public int scaleY(int originalY) {
        return (int) (originalY * (getHeight() / (double) ORIGINAL_HEIGHT));
    }
    
    public int scaleSquare(int original) {
    	double scale = Math.min(getWidth() / (double) ORIGINAL_WIDTH, getHeight() / (double) ORIGINAL_HEIGHT);
    	return (int)(original * scale);
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
        } else if (currentState == GameState.RITMO) {
            levelPanel.handleKeyPress(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentState == GameState.PLAYING) {
            gamePanel.handleKeyRelease(e.getKeyCode());
        } else if (currentState == GameState.RITMO) {
            levelPanel.handleKeyRelease(e.getKeyCode());
        }
    }

    
    @Override
    public void keyTyped(KeyEvent e) {
        // No se usa
    }
    
    
}