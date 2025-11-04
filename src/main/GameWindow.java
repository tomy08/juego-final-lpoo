package main;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;

import Levels.LevelPanel;
import Sonidos.Musica;
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
    public GamePanel gamePanel;
    private GameThread gameThread;    
    private GameSettings gameSettings;
    private LevelPanel levelPanel;
    private Niveles niveles;
    public static GameWindow instance;
    public static float volumenGlobal = 1.0f; // 0.0 a 1.0
    public static boolean efectosActivados = true;
    public static boolean musicaActivada = true; // flag global
    
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
        niveles = new Niveles(this);

        // Configurar listeners
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Mostrar menú principal inicialmente
        showMainMenu();
        ProfileManager.cargarPerfil();
    }
    
    public static void reproducirSonido(String rutaArchivo) {
        if (!efectosActivados) return; // No reproducir si efectos desactivados

        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(rutaArchivo));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            // Ajustar volumen según volumen global
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float)(-60.0 + 60.0 * volumenGlobal); // volumenGlobal = 0.0 a 1.0
                if (dB < control.getMinimum()) dB = control.getMinimum();
                if (dB > control.getMaximum()) dB = control.getMaximum();
                control.setValue(dB);
            }

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
        mainMenu.actualizarOpciones();
        getContentPane().add(mainMenu);
        revalidate();
        repaint();
        requestFocus();
    }
    
    public void showNiveles() {
    	
    	ProfileManager.cargarPerfil();
    	getContentPane().removeAll();
        getContentPane().add(niveles);
        revalidate();
        repaint();
        niveles.requestFocusInWindow();
        niveles.ignorarProximoEnter = true;
        niveles.selectedOption = 0;
        niveles.scrollOffset = 0;
        LevelPanel.EnHistoria = false;
        
    }
    
    public void showStory(int story) {
        // Mostrar la pantalla de historia antes de iniciar el juego
        currentState = GameState.STORY;
        getContentPane().removeAll();

        StoryScreen storyScreen = new StoryScreen(this, story);
        storyScreen.startTyping();
        getContentPane().add(storyScreen);
        revalidate();
        repaint();
        storyScreen.requestFocus();
    }

    public void startRealGame() {
        currentState = GameState.PLAYING;
        getContentPane().removeAll();
        getContentPane().add(gamePanel);
        revalidate();
        repaint();
        requestFocus();
        LevelPanel.EnHistoria = true;

        if (!gamePanel.musicaParada) {
            Musica.reproducirMusica("resources/Music/Fondo.wav");
            Musica.enableLoop();
        }

        startGameThread(gamePanel);
    }

    
    /**
     * Continúa una partida guardada
     */
    public void continueGame() {
        currentState = GameState.PLAYING;
        getContentPane().removeAll();
        getContentPane().add(gamePanel);
        revalidate();
        repaint();
        requestFocus();
        LevelPanel.EnHistoria = true;
        // Cargar los datos guardados
        boolean cargaExitosa = GameSaveManager.cargarPartida(gamePanel, gamePanel.player);
        System.out.println("Se cargó: " + gamePanel.enPlantaAlta);
        
        // Cargar mapa de inicio
        if(gamePanel.enPlantaAlta) {
        	gamePanel.CargarZona(0);
        } else {
        	gamePanel.CargarZona(1);
        }
        
        if (cargaExitosa) {
            if(!gamePanel.musicaParada) {
                Musica.reproducirMusica("resources/Music/Fondo.wav");
                Musica.enableLoop();
            }
            startGameThread(gamePanel);
            System.out.println("Partida cargada exitosamente");
        } else {
        	System.out.println("Error al cargar la partida");
        }
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
	
	public double DscaleY(double originalY) {
        return (originalY * (getHeight() / (double) ORIGINAL_HEIGHT));
    }
	
	public float scaleFont(float originalF) {
		float scale = Math.min(getWidth() / (float) ORIGINAL_WIDTH, getHeight() / (float) ORIGINAL_HEIGHT);
    	return (originalF * scale);
	}
	
	// Mensaje al ganar en Ritmo
	
	public void SWM(String message) {
		gamePanel.ShowWinMessage(message);
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
            gameThread = null;
        }

        Musica.detenerMusica();

        // Limpiar NPCs estáticos
        entities.NPCManager.clearAllNPCs();

        // Remover y limpiar el contenido
        getContentPane().removeAll();
        repaint();
        revalidate();

        // Crear un GamePanel nuevo y limpio
        gamePanel = new GamePanel(this);

        // Mostrar menú principal
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
