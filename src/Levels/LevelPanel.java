package Levels;

import entities.arrow;
import main.GW;
import main.GameSettings;
import main.GameThread;
import main.GameWindow;
import main.ProfileManager;
import main.GameThread.Updatable;

import javax.swing.*;

import Sonidos.Musica;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LevelPanel extends JPanel implements GameThread.Updatable {
	
    private GameWindow gameWindow;
    private java.util.concurrent.CopyOnWriteArrayList<arrow> arrows;
    private final List<arrow> arrowsToRemove = new ArrayList<>();
    
    private boolean[] columnPressed = new boolean[4];
    public static boolean EnHistoria;
    
    // Tiempo Canción
    private double virtualTimeMs = 0.0;      // tiempo virtual de la canción
    private long lastUpdateNs = System.nanoTime(); // último tiempo del update
    
    // Imagenes
    private Image[] teclaImages = new Image[4];
    private Image[] teclaPressImages = new Image[4];
    private Image[] rankImages = new Image[6];
    
    // Fonts
    private final Font timeFont = GameWindow.Pixelart.deriveFont(GW.SF(65f));
    private final Font scoreFont = GameWindow.Pixelart.deriveFont(GW.SF(55f));
    private final Font comboFont = GameWindow.Pixelart.deriveFont(GW.SF(50f));
    private final Font statsFont = GameWindow.Pixelart.deriveFont(GW.SF(40f));
    
    // Colores LNs
    public Color[] colors = {
        new Color(255, 0, 0, 125),    // Rojo
        new Color(0, 255, 0, 125),    // Verde
        new Color(0, 0, 255, 125),    // Azul
        new Color(255, 255, 0, 125)   // Amarillo
    };

    
    // Estadísticas
    private int puntaje = 0;
    private int combo = 0;
    private int maxCombo = 0;
    
    // Vida
    public static int Max_vida = 50;
    public static double multiplicador_puntos = 1;
    public static int plusSuma = 0;
    private int vida = Max_vida/2;
    private int barraAltoMax = GW.SX(650);
    
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
    private boolean esperandoNombre = false; // Nuevo estado: esperando la entrada del jugador
    private String nombreActual = ""; // El nombre que el jugador está escribiendo
    private int nivelIndexGanado = -1; // Para guardar el índice del nivel mientras se escribe el nombre
    private static final int MAX_NOMBRE_CHARS = 10;
    private boolean pausa = false;
    private boolean lose = false;
    public String level;
    private int speed;
    private int bpm;
    
    // Tiempo
    private double msTotalTime;
    private int minutos = 0;
    private int segundos;
    
    // Pausa
    private String[] pauseOptions = {"Continuar", "Reiniciar", "Configuracion", "Salir"};
    private int selectedPauseOption = 0;

    // Perder
    
    private String[] loseOptions = {"Reintentar", "Salir"};
    private int selectedLoseOption = 0;

    public LevelPanel(GameWindow gw, String levelName, int speed, int bpm) {
        this.gameWindow = gw;
        this.level = levelName;
        this.speed = speed;
        this.bpm = bpm; // Guardar BPM
        setBackground(Color.BLACK);
        setFocusable(true);
        
        // Cargar Imagenes
        for(int i = 0; i < 4; i++) {
            teclaImages[i] = new ImageIcon("resources/Sprites/Ritmo/tecla" + (i + 1) + ".png").getImage();
            teclaPressImages[i] = new ImageIcon("resources/Sprites/Ritmo/tecla" + (i + 1) + "Press.png").getImage();
        }
        
        rankImages[0] = new ImageIcon("resources/Sprites/rankings/rankSS.png").getImage();
        rankImages[1] = new ImageIcon("resources/Sprites/rankings/rankS.png").getImage();
        rankImages[2] = new ImageIcon("resources/Sprites/rankings/rankA.png").getImage();
        rankImages[3] = new ImageIcon("resources/Sprites/rankings/rankB.png").getImage();
        rankImages[4] = new ImageIcon("resources/Sprites/rankings/rankC.png").getImage();
        rankImages[5] = new ImageIcon("resources/Sprites/rankings/rankD.png").getImage();

        Musica.disableLoop();
        Musica.reproducirMusica("resources/Music/"+levelName+".wav");
        
        File file = new File("resources/Levels/level"+levelName+".txt");
        
        // Cargar notas
        List<arrow> loadedArrows = ChartLoader.loadChart(file, gw, true, GW.SX(speed), bpm);
        this.arrows = new java.util.concurrent.CopyOnWriteArrayList<>(loadedArrows);
        for (arrow a : this.arrows) {
        	if(a.Long) {
            	a.color = colors[getColumnFromX((int)a.x)];
            } else if(!a.isEnd) {
            	a.image = new ImageIcon("resources/Sprites/Ritmo/Nota"+(getColumnFromX((int)a.x)+1)+".png").getImage();
            }
        }
        
        // Tiempo total
        msTotalTime = ChartLoader.getTotalTime(file, bpm);
        int totalSeconds = (int) (msTotalTime / 1000);
        minutos = totalSeconds / 60;
        segundos = totalSeconds % 60;
    }

    public void update() {
        
    	if (!pausa && !lose) {
            long now = System.nanoTime();
            double deltaMs = (now - lastUpdateNs) / 1_000_000.0; // nanosegundos a milisegundos
            virtualTimeMs += deltaMs;  // solo sumamos si no está en pausa
            lastUpdateNs = now;
        } else {
            lastUpdateNs = System.nanoTime();
        }
        if(pausa || lose) {
            return;
        }
        
        // Perder
        if(vida <= 0) {
            lose = true;
        }
        
        int totalSeconds = (int) (msTotalTime / 1000) - (int) (virtualTimeMs / 1000);
        minutos = totalSeconds / 60;
        segundos = totalSeconds % 60;
        if(segundos <= 0) {
        	segundos = 0;
        }
        
        // Max Vida
        
        arrowsToRemove.clear();
        for (arrow a : arrows) {
            if (a == null) {
                arrowsToRemove.add(a); 
                continue;
            }
              
            
            a.update(virtualTimeMs, GW.SY(125)); // recalcula su Y según tiempo virtual


            if(a.isEnd && a.y <= GW.SY(125)) {
            	arrows.remove(a);
            	if(!win) {
            		Ganar();
            	}
            	
                return;
            }

            if (a.y + a.size < 0) {
                arrowsToRemove.add(a); 
                combo = 0;
                vida -= 4;
                missCount++;
                continue;
            }

            if(a.Long && a.y - a.size*2/3 <= GW.SY(125) && columnPressed[getColumnFromX((int)a.x)]) {
                arrowsToRemove.add(a);
                combo++;
                vida += 1;

                if(vida + 1 < Max_vida) {
                    vida++;
                } else if(vida + 1 >= Max_vida) {
                	vida = Max_vida;
                }
                puntaje += 25;
            }
        }
        
        arrows.removeAll(arrowsToRemove);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Fondo
        g2d.setColor(new Color(90,90,90));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Fondo transparente
        g2d.setColor(new Color(0,0,0,125));
        g2d.fillRect(GW.SX(650), 0, GW.SX(620), getHeight());
        // Teclas Jugador
    	int posX = GW.SX(685);
    	for(int i = 0; i<4; i++) {
    		
    		if(columnPressed[i]) {
    	        g2d.drawImage(teclaPressImages[i],
    	            posX,
    	            GW.SY(125),
    	            GW.SX(100),
    	            GW.SY(100),
    	            this);
    	    } else {
    	        g2d.drawImage(teclaImages[i],
    	            posX,
    	            GW.SY(125),
    	            GW.SX(100),
    	            GW.SY(100),
    	            this);
    	    }
    		
    		posX += GW.SX(150);
    	}
        

        for (arrow a : arrows) {
        	if(a != null ) {
        		
        		if(a.y < getHeight()) {
        			 g2d.drawImage(a.image, (int)a.x, (int)a.y, a.size, a.size, this);
                     if(a.Long) {
                     	a.draw(g2d, bpm);
        		}
                     
        	}
}
        }
        
        drawUI(g2d);

        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(35f)));
        g2d.drawString("ESC para pausar", 50, 50);
        
        
        // PANTALLA DE PAUSA
        if(pausa) {
        	drawPAUSE(g2d);
        }
        
        // PANTALLA DE PERDER
        
        if(lose) {
        	drawLOSE(g2d);
        }
        
        // PANTALLA DE WIN
        if(win) {
        	drawWIN(g2d);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
    	
    	
    	// Vida Jugador
    	g2d.setColor(Color.RED);
        g2d.fillRect(GW.SX(1300), GW.SY(270), GW.SX(20), barraAltoMax);
        g2d.setColor(Color.BLACK);
        
        int barraVida = (int)((vida / (double)Max_vida) * barraAltoMax);
        g2d.setColor(Color.GREEN);
        int barraVida_PosY = (GW.SY(270) + barraAltoMax) - barraVida;
        g2d.fillRect(GW.SX(1300), barraVida_PosY, GW.SX(20), barraVida);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(GW.SX(1300), GW.SY(270), GW.SX(20), barraAltoMax);
        
    	
        // Puntaje Jugador
        g2d.setFont(scoreFont);
        FontMetrics fm1 = g2d.getFontMetrics(scoreFont);
        String textoPuntaje = "" + puntaje;
        int anchoPuntaje = fm1.stringWidth(textoPuntaje);
        g2d.setColor(Color.WHITE);
        g2d.drawString(textoPuntaje, GW.SX(1880) - anchoPuntaje, GW.SY(100));
        
        // Tiempo restante
        g2d.setFont(timeFont);
        String tiempoTexto = String.format("%02d:%02d", minutos, segundos);
        g2d.drawString(tiempoTexto, GW.SX(1650), GW.SY(1020));
        
        int textWidth;
        // Mostrar Combo
        if(combo != 0) {
        	g2d.setColor(Color.WHITE);
            g2d.setFont(comboFont);

            // Centrar Texto
            FontMetrics fm = g2d.getFontMetrics(comboFont);
            String textoCombo = "" + combo;
            textWidth = fm.stringWidth(textoCombo);
            g2d.drawString(textoCombo, GW.SX(955) - textWidth / 2, GW.SY(500));
        }
        

        // Mostrar porcentaje
        Image image = percentageToRank(getAccuracyPercentage());
        g2d.setFont(scoreFont);
        FontMetrics fm3 = g2d.getFontMetrics(scoreFont);
        String textoPorcentaje = getAccuracyPercentage() + "%";
        int anchoPorcentaje = fm3.stringWidth(textoPorcentaje);
        g2d.drawString(textoPorcentaje, GW.SX(1880) - anchoPorcentaje, GW.SY(200));
        g2d.drawImage(image, GW.SX(1615), GW.SY(150), GW.SX(60), GW.SY(60), this);
        
        // Contadores
        g2d.setFont(statsFont);
        g2d.setColor(new Color(200,0,255));
        g2d.drawString("SIGMA: " + sigmaCount, GW.SX(50), GW.SY(450));
        g2d.setColor(new Color(205,205,50));
        g2d.drawString("Aura: " + auraCount, GW.SX(50), GW.SY(500));
        g2d.setColor(new Color(50,200,50));
        g2d.drawString("Bue: " + bueCount, GW.SX(50), GW.SY(550));
        g2d.setColor(new Color(120, 0, 0));
        g2d.drawString("Pete: " + peteCount, GW.SX(50), GW.SY(600));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Miss: " + missCount, GW.SX(50), GW.SY(650));

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
            
            g2d.setFont(statsFont);
            textWidth = g2d.getFontMetrics().stringWidth(lastHitText);
            g2d.drawString(lastHitText, GW.SX(955) - textWidth / 2, GW.SY(600));
        }
    }
    
    private void drawWIN(Graphics2D g2d) {
    	double porcentajeFinal = getAccuracyPercentage();
        int nivelIndex = getLevelIndex(this.level);
    	
    	// Oscurecer fondo
    	g2d.setColor(new Color(0,0,0,150));
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	
    	g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(60f)));
    	g2d.setColor(Color.WHITE);
    	g2d.drawString("PUNTUACION", GW.SX(800), GW.SY(250));
    	
    	// Imagen de la nota final
    	Image image = percentageToRank(porcentajeFinal);
    	g2d.drawImage(image, GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	
    	// Porcentaje
    	g2d.drawString(porcentajeFinal+"%", GW.SX(600), GW.SY(700));
    	
    	// Max Combo
    	g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(40f)));
    	if(missCount == 0 && peteCount == 0) {
    		g2d.setColor(Color.YELLOW);
    	}
    	g2d.drawString("Max Combo: " + maxCombo, GW.SX(600), GW.SY(750));
    	
    	int totalHits = sigmaCount + auraCount + bueCount + peteCount + missCount;
    	// Estadísticas
    	g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(45f)));
    	g2d.setColor(new Color(200,0,255));
    	g2d.drawString("(" + porcentaje(sigmaCount, totalHits) + "%) SIGMA: " + sigmaCount, GW.SX(1050), GW.SY(400));
    	
    	g2d.setColor(new Color(205,205,50));
    	g2d.drawString("(" + porcentaje(auraCount, totalHits) + "%) Aura: " + auraCount, GW.SX(1050), GW.SY(475));
    	
    	g2d.setColor(new Color(50,200,50));
    	g2d.drawString("(" + porcentaje(bueCount, totalHits) + "%) Bue: " + bueCount, GW.SX(1050), GW.SY(550));
    	
    	g2d.setColor(new Color(120, 0, 0));
    	g2d.drawString("(" + porcentaje(peteCount, totalHits) + "%) Pete: " + peteCount, GW.SX(1050), GW.SY(625));
    	
    	g2d.setColor(Color.WHITE);
    	g2d.drawString("(" + porcentaje(missCount, totalHits) + "%) Misses: " + missCount, GW.SX(1050), GW.SY(700));
    	
    	
    	if (nivelIndex != -1 && !EnHistoria) {
            // TOP SCORERS
    		
    		// Titulo
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(60f)));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("TOP SCORES", GW.SX(170), GW.SY(400)); // Colocar en otra ubicación
            
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(35f)));
            int startY = GW.SY(500);
            int spacing = GW.SY(50);
            
            // Obtener la matriz de Top Scores de ProfileManager
            double[][] scores = ProfileManager.topScoresPorNivel;
            String[][] nombres = ProfileManager.topNombresPorNivel;
            
            for (int i = 0; i < ProfileManager.MAX_TOP_ENTRIES; i++) {
                double score = scores[nivelIndex][i];
                String nombre = nombres[nivelIndex][i];
                
                if (score > 0.0) { // Solo dibujar si hay una puntuación válida
                    String rankText = String.format("%d. %s: %.2f%%", 
                        (i + 1), 
                        nombre != null ? nombre : "---", // Mostrar "---" si el nombre es null
                        score
                    );
                    
                    // Usar color diferente para el primer puesto
                    g2d.setColor(i == 0 ? Color.CYAN : Color.WHITE); 
                    g2d.drawString(rankText, GW.SX(170), startY + (i * spacing));
                } else {
                    // Dibujar una entrada vacía si no hay score
                    g2d.setColor(Color.GRAY);
                    g2d.drawString(String.format("%d. Vacio", (i + 1)), GW.SX(170), startY + (i * spacing));
                }
            }
        } else if(EnHistoria) {
        	// Monedas Conseguidas:
        	g2d.setColor(Color.WHITE);
        	g2d.drawString("Conseguiste:", GW.SX(250), GW.SY(500));
        	g2d.drawString((int) ((puntaje * getAccuracyPercentage() / 400) * multiplicador_puntos) + "$", GW.SX(250), GW.SY(550));
        }
    	
    	if (esperandoNombre) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int boxWidth = GW.SX(700);
            int boxHeight = GW.SY(300);
            int boxX = centerX - boxWidth / 2;
            int boxY = centerY - boxHeight / 2;
            
            // 1. Dibujar el cuadro negro
            g2d.setColor(new Color(0, 0, 0, 230));
            g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
            
            // 2. Dibujar el borde blanco
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(GW.SX(5)));
            g2d.drawRect(boxX, boxY, boxWidth, boxHeight);
            
            // 3. Dibujar el texto de título
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(60f)));
            String title = "NUEVO TOP SCORE!";
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, centerX - titleWidth / 2, boxY + GW.SY(70));
            
            // 4. Dibujar el nombre actual (entrada de texto)
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(50f)));
            String inputPrompt = "NOMBRE: " + nombreActual;
            int inputWidth = g2d.getFontMetrics().stringWidth(inputPrompt);
            g2d.drawString(inputPrompt, centerX - inputWidth / 2, boxY + GW.SY(150));
            
            // 5. Dibujar el cursor parpadeante (si el nombre no excede el límite)
            if (nombreActual.length() < MAX_NOMBRE_CHARS && (System.currentTimeMillis() % 1000) < 500) {
                int cursorX = centerX + inputWidth / 2 + GW.SX(10);
                g2d.fillRect(cursorX, boxY + GW.SY(115), GW.SX(5), GW.SY(40));
            }
            
            // 6. Instrucción (ENTER)
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(30f)));
            String hint = "Presiona ENTER para continuar";
            int hintWidth = g2d.getFontMetrics().stringWidth(hint);
            g2d.drawString(hint, centerX - hintWidth / 2, boxY + GW.SY(250));
        }
    	
    	// Ayuda
    	g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(50f)));
    	g2d.setColor(Color.WHITE);
    	g2d.drawString("ESC para volver", GW.SX(800), GW.SY(880));
    }
    
    private void drawPAUSE(Graphics2D g2d) {
    	
    	// Oscurecer fondo
    	g2d.setColor(new Color(0,0,0,150));
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	
    	g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(100f)));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "PAUSA";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, GW.SY(350));
        
        // Opciones
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(65f)));
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
    
    public void drawLOSE(Graphics2D g2d) {
    	// Oscurecer fondo
    	g2d.setColor(new Color(0,0,0,150));
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	
    	g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(100f)));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "PERDISTE";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, GW.SY(350));
        
     // Opciones
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(65f)));
        FontMetrics optionMetrics = g2d.getFontMetrics();
        int startY = GW.SY(600);
        int spacing = GW.SY(100);

        for (int i = 0; i < loseOptions.length; i++) {
            String option = loseOptions[i];
            int optionX = (getWidth() - optionMetrics.stringWidth(option)) / 2;
            int optionY = startY + (i * spacing);

            if (i == selectedLoseOption) {
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

        double scoreSum = sigmaCount * 100 + auraCount * 95 + bueCount * 66 + peteCount * 25 + missCount * 0;
        double percentage = scoreSum / totalHits;
        return Math.round(percentage * 100.0) / 100.0; // redondear a 2 decimales
    }
    
    public Image percentageToRank(double percentage) {
    	
    	if(percentage == 100) {
    		return rankImages[0];
    	} else if (percentage >= 95) {
    		return rankImages[1];
    	} else if (percentage >= 90) {
    		return rankImages[2];
    	} else if (percentage >= 80) {
    		return rankImages[3];	
    	} else if (percentage >= 70) {
    		return rankImages[4];
    	} else {
    		return rankImages[5];
    	}
    	
    }
    
    public double porcentaje(int numero, int total) {
        if (total == 0) return 0.0;
        double percentage = ((double) numero / (double) total) * 100.0;
        return Math.round(percentage * 100.0) / 100.0; // redondea a 2 decimales
    }


    
    // Chequear la columna segun la tecla
    
    private void checkHit(int column) {
        int hitY = GW.SY(150);         
        int WindowPete = GW.SY(200);
        int WindowBue = GW.SY(120);
        int WindowAura = GW.SY(80);
        int WindowSigma = GW.SY(45);
        
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
                        SumaV = 3 + plusSuma;
                        
                    } else if (Math.abs(a.y - hitY) <= WindowAura) {
                    	auraCount++;
                        lastHitText = "Aura";
                        puntaje += 100;
                        combo++;
                        SumaV = 1 + plusSuma;
                        
                    } else if (Math.abs(a.y - hitY) <= WindowBue) {
                    	bueCount++;
                        lastHitText = "Bue";
                        puntaje += 50;
                        combo++;
                        SumaV = 0 + plusSuma;
                    } else {
                    	peteCount++;
                        lastHitText = "Pete";
                        puntaje += 25;
                        combo = 0; // resetear combo si pegaste mal
                        SumaV = -7;
                    }
                    
                    if(combo > maxCombo) {
                    	maxCombo = combo;
                    }
                    
                    // Sumar o Restar vida
                    if(vida + SumaV < Max_vida) {
                    	vida += SumaV;
                    } else if (vida + SumaV >= Max_vida) {
                    	vida = Max_vida;
                    }

                    hitDisplayTime = System.currentTimeMillis(); // empieza a mostrar el texto
                    arrows.remove(i);
                    column = -1;
                    return;
                }
            }
        }
    }

    
    private int getColumnFromX(int x) {
        int baseX = GW.SX(685); // donde arranca la primera columna del jugador
        int spacing = GW.SX(150); // distancia entre columnas
        for (int i = 0; i < 4; i++) {
            int colX = baseX + i * spacing;
            if (Math.abs(x - colX) < spacing / 2) {
                return i;
            }
        }
        return -1;
    }
    
    // Ganar
 // Dentro de LevelPanel

    private void Ganar() {
        win = true;
        
        // --- LÓGICA DE ACTUALIZACIÓN DE TOP SCORE ---
        if(!EnHistoria) {
            double porcentajeFinal = getAccuracyPercentage();
            int nivelIndex = getLevelIndex(this.level);
            
            // Verificamos si la puntuación califica para el Top (posición 0-4)
            if (nivelIndex != -1) {
                
                // Llama a agregarNuevoTopScore para ver si entra y ordenar la lista
                boolean isNewTop = ProfileManager.agregarNuevoTopScore(
                    nivelIndex, 
                    porcentajeFinal, 
                    "ZZZ"
                );
                
                if (isNewTop) {
                     // Si entró al Top, cambiamos al estado de entrada de nombre
                     this.esperandoNombre = true;
                     this.nivelIndexGanado = nivelIndex;
                     System.out.println("Nuevo Top Score, esperando nombre...");
                     return; // Detenemos la función aquí, la guardaremos después del input
                }
            }
        }
        LevelToReward(level);
        if(EnHistoria) {
            gameWindow.gamePanel.monedas += (int) ((puntaje * getAccuracyPercentage() / 400) * multiplicador_puntos);
        }
    }
    
    
    private void LevelToReward(String level) {
    	switch(level) {
    	
    	case "Melody":
    		if(EnHistoria) {
    			// Desbloquear taller
        		gameWindow.gamePanel.triggerNPC("Zambrana", 1);
        		gameWindow.gamePanel.triggerNPC("Melody", 2);
        		gameWindow.gamePanel.triggerNPC("Kreimer", 1);
        		gameWindow.gamePanel.taller = true;
    		}
    		
    		// Desbloquar nivel
    		ProfileManager.nivel2Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    	
    	case "Vagos":
    		if(EnHistoria) {
    			// trigger al de la cantina para que te de un item al hablar (no dar item directo)
    			gameWindow.gamePanel.triggerNPC("Cantina", 1);
    			gameWindow.gamePanel.triggerNPC("Vagos", 2);
    		}
    		
    		ProfileManager.nivel4Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Gennuso":
    		if(EnHistoria) {
    			// Conseguir Item: IG de renaa_gm
        		gameWindow.gamePanel.givePlayerItem("renaa_gm", "IG de renaa_gm", "@renaa_gm.png", 1, 1);
        		gameWindow.gamePanel.gennusoBien = false;
        		gameWindow.gamePanel.triggerNPC("Gennuso", 1);
        		GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel3Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Rita":
    		if(EnHistoria) {
    			// Conseguir Item: Procesador
        		gameWindow.gamePanel.givePlayerItem("procesador", "Procesador", "procesador.png", 1, 1);
        		gameWindow.gamePanel.triggerNPC("Rita", 1);
        		GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel8Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Casas":
    		if(EnHistoria) {
    			// Conseguir Item: Llave de SUM
    			gameWindow.gamePanel.givePlayerItem("llave_sum", "Llave del SUM", "llave_SUM.png", 1, 1);
    			gameWindow.gamePanel.triggerNPC("Casas", 3);
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel7Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Ledesma":
    		if(EnHistoria) {
    			// Conseguir Item: Pastafrola
    			gameWindow.gamePanel.givePlayerItem("pastafrola", "Pastafrola", "pastafrola.png", 1, 5);
    			gameWindow.gamePanel.triggerNPC("Ledesma", 3);
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel6Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Pecile":
    		if(EnHistoria) {
    			// Conseguir Item: Llave de la reja
    			gameWindow.gamePanel.givePlayerItem("llave_reja", "Llave de la reja", "llave_Reja.png", 1, 1);
    			gameWindow.gamePanel.triggerNPC("Pecile", 2);
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel10Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Signorello":
    		if(EnHistoria) {
    			// Conseguir Item: Marcador de findlay
    			gameWindow.gamePanel.givePlayerItem("marcador_findlay", "Marcador azul", "marcador_Azul.png", 1, 8);
    			gameWindow.gamePanel.triggerNPC("Signorello", 2);
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel5Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Martin":
    		if(EnHistoria) {
    			// Conseguir Item: Marcador de findlay
    			gameWindow.gamePanel.givePlayerItem("marcador_findlay", "Marcador verde", "marcador_Verde.png", 1, 8);
    			gameWindow.gamePanel.triggerNPC("Martin", 3);
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		
    		ProfileManager.nivel9Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Pacheco":
    		if(EnHistoria) {
    			gameWindow.gamePanel.triggerNPC("Pacheco", 1);
    		}
    		
    		ProfileManager.nivel1Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Moya":
    		ProfileManager.nivel11Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Linzalata":
    		ProfileManager.nivel12Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    		
    	case "Ricky":
    		ProfileManager.nivel13Pasado = true;
    		ProfileManager.guardarPerfil();
    		break;
    	}
    }
    
    // Mensaje según qué vos eliminaste
    private String LevelToMSG(String level) {
    	String message = "";
    	
    	switch(level) {
    	case "Melody":
    		message = "Has desbloqueado la zona ''El Taller'' ";
    		break;
    	
    	case "Los Vagos":
    		message = "Espantaste a Los Vagos de la cantina.";
    		break;
    		
    	case "Gennuso":
    		message = "Has conseguido ''IG de renna_gm''... Gennuso ahora está enojado con vos";
    		break;
    		
    	case "Rita":
    		message = "Has conseguido ''Procesador''";
    		break;
    		
    	case "Casas":
    		message = "Has conseguido ''Llave del SUM''";
    		break;
    		
    	case "Ledesma":
    		message = "Has conseguido ''Pastafrola''";
    		break;
    		
    	case "Pecile":
    		message = "Has conseguido ''Llave de la reja''";
    		break;
    		
    	case "Signorello":
    		message = "Has conseguido ''Marcador azul''";
    		break;
    		
    	case "Martin":
    		message = "Has conseguido ''Marcador verde''... Martin ahora está enojado con vos";
    		break;
    		
    	case "Pacheco":
    		message = "Muy probablemente NO aprendiste a jugar a esto... gracias a Pacheco";
    		break;
    		
    	case "Vagos":
    		message = "Hiciste que los vagos se calmen. Quedaron re pillos";
    		break;
    	}
    	
    	return message;
    }
    
    public int getLevelIndex(String levelName) {
        return switch (levelName) {
            case "Pacheco" -> 0;
            case "Melody" -> 1;
            case "Gennuso" -> 2;
            case "Vagos" -> 3;
            case "Signorello" -> 4;
            case "Ledesma" -> 5;
            case "Casas" -> 6;
            case "Rita" -> 7;
            case "Martin" -> 8;
            case "Pecile" -> 9;
            case "Moya" -> 10;
            case "Linzalata" -> 11;
            case "Ricky" -> 12;
            default -> -1;
        };
    }
    
    // Detectar teclas
    public void handleKeyPress(int keyCode) {
    	
        if (keyCode == KeyEvent.VK_ESCAPE) {
        	
        	if(esperandoNombre) {
        		return;
        	}
        	
        	
        	if(win && !esperandoNombre) {
        		
        		if(EnHistoria) {
        		
        		switch(level) {
        		
        		case "Moya": // Final normal
        			gameWindow.showStory(2);
        			break;
        			
        		case "Linzalata": // Final bueno
        			gameWindow.showStory(3);
        			break;
        			
        		case "Ricky": // Final secreto
        			gameWindow.showStory(4);
        			break;
        		
        		default:
        		gameWindow.startRealGame(); // volver al RPG
             	gameWindow.SWM(LevelToMSG(level)); // Mensaje del GamePanel al ganar
             	if(!gameWindow.gamePanel.musicaParada) {
             		Musica.reproducirMusica("resources/Music/Fondo.wav");
             	}
                return;
        		}
        		
        		} else {
        			gameWindow.showNiveles();
        		}
        		
        		
        	} else if(!lose){
        		pausa = !pausa;
        		if(pausa) {
        			Musica.pausarMusica();
        		} else {
        			Musica.reanudarMusica();
        		}
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
        
        if(lose) {
        	switch(keyCode) {
        		case KeyEvent.VK_UP:
        			selectedLoseOption = (selectedLoseOption - 1 + loseOptions.length) % loseOptions.length;
        			GameWindow.reproducirSonido("resources/sounds/menu.wav");
        			repaint();
        			break;
        		case KeyEvent.VK_DOWN:
        			selectedLoseOption = (selectedLoseOption + 1) % loseOptions.length;
        			GameWindow.reproducirSonido("resources/sounds/menu.wav");
        			repaint();
        			break;
        		case KeyEvent.VK_ENTER:
        			handleLoseSelection();
        			break;
        	}
        	return;
        }
        
        if (win && esperandoNombre) {
            if (keyCode == KeyEvent.VK_ENTER) {
                handleNameInputComplete();
                return;
            } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                // Borrar el último carácter
                if (!nombreActual.isEmpty()) {
                    nombreActual = nombreActual.substring(0, nombreActual.length() - 1);
                    GameWindow.reproducirSonido("resources/sounds/menu.wav"); // Sonido de borrado
                }
                return;
            }
            
            // Capturar letras y números
            String charToAdd = null;

            if (keyCode == KeyEvent.VK_SPACE) {
                // Caso especial: La barra espaciadora agrega un guion bajo (_)
                charToAdd = "_";
            } else {
                // Capturar otras teclas alfanuméricas
                // Usamos KeyText() y filtramos solo la primera letra/dígito
                String keyText = KeyEvent.getKeyText(keyCode).toUpperCase();
                if (keyText.length() == 1 && (Character.isLetterOrDigit(keyText.charAt(0)))) {
                     charToAdd = keyText;
                }
            }
            
            // Aplicar el carácter si es válido y hay espacio
            if (charToAdd != null) {
                if (nombreActual.length() < MAX_NOMBRE_CHARS) {
                    nombreActual += charToAdd;
                    GameWindow.reproducirSonido("resources/sounds/hitsound.wav"); 
                }
            }
            repaint();
            return;
        }
        
        int column = -1;
        if (keyCode == GameSettings.KEY_NLEFT && !columnPressed[0]) {
        	column = 0;
        	columnPressed[0] = true;
        	GameWindow.reproducirSonido("resources/sounds/hitsound.wav");
        }
        else if (keyCode == GameSettings.KEY_NDOWN && !columnPressed[1]) {
        	column = 1;
        	columnPressed[1] = true;
        	GameWindow.reproducirSonido("resources/sounds/hitsound.wav");
        }
        else if (keyCode == GameSettings.KEY_NUP && !columnPressed[2]) {
        	column = 2;
        	columnPressed[2] = true;
        	GameWindow.reproducirSonido("resources/sounds/hitsound.wav");
        }
        else if (keyCode == GameSettings.KEY_NRIGHT && !columnPressed[3]) {
        	column = 3;
        	columnPressed[3] = true;
        	GameWindow.reproducirSonido("resources/sounds/hitsound.wav");
        }

        if (column != -1) {
            checkHit(column);
        }
    }

    public void handleKeyRelease(int keyCode) {
    	if (keyCode == GameSettings.KEY_NLEFT) {
        	columnPressed[0] = false;
        }
        else if (keyCode == GameSettings.KEY_NDOWN) {
        	columnPressed[1] = false;
        }
        else if (keyCode == GameSettings.KEY_NUP) {
        	columnPressed[2] = false;
        }
        else if (keyCode == GameSettings.KEY_NRIGHT) {
        	columnPressed[3] = false;
        }
    }
    
    private void handlePauseSelection() {
        switch (selectedPauseOption) {
            case 0: // Continuar
                pausa = false;
                Musica.reanudarMusica();
                break;
            case 1: // Reiniciar
            	Musica.detenerMusica();
            	gameWindow.startRitmo(level, speed, bpm);
                break;
            case 2:
            	gameWindow.settingsGame();
            	break;
            case 3: // Salir
            	if(EnHistoria) {
            		gameWindow.startRealGame(); // volver al RPG
            	} else {
            		gameWindow.showNiveles();
            	}
                break;
        }
    }
    
    private void handleLoseSelection() {
        switch (selectedLoseOption) {
            case 0: // Reintentar
            	Musica.detenerMusica();
            	gameWindow.startRitmo(level, speed, bpm);
                break;
            case 1: // Salir
            	if(EnHistoria) {
            		gameWindow.startRealGame();
            		Musica.detenerMusica();
            	} else {
            		gameWindow.showNiveles();
            	}
                break;
        }
    }
    
    private void handleNameInputComplete() {
        if (nivelIndexGanado == -1) {
            // Error de seguridad, pero si pasa, salimos del estado
            esperandoNombre = false;
            return;
        }
        
        String nombreFinal = nombreActual.trim();
        if (nombreFinal.isEmpty()) {
            nombreFinal = "Player";
        }
        
        // Reinsertar puntuación con nombre
        double porcentajeFinal = getAccuracyPercentage();
        
        for (int i = 0; i < ProfileManager.MAX_TOP_ENTRIES; i++) {
            if ("ZZZ".equals(ProfileManager.topNombresPorNivel[nivelIndexGanado][i])) {
                ProfileManager.topNombresPorNivel[nivelIndexGanado][i] = nombreFinal;
                break;
            }
        }
        
        // Guardar estado
        ProfileManager.guardarPerfil(); 
        LevelToReward(level);
        
        esperandoNombre = false;
        nivelIndexGanado = -1;
        nombreActual = "";
        repaint();
    }
    
}
