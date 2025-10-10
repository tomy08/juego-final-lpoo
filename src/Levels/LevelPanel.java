package Levels;

import entities.arrow;
import main.GW;
import main.GameSettings;
import main.GameThread;
import main.GameWindow;
import main.GameThread.Updatable;

import javax.swing.*;

import Sonidos.Musica;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

public class LevelPanel extends JPanel implements GameThread.Updatable {
	
    private GameWindow gameWindow;
    private List<arrow> arrows;
    
    private boolean[] columnPressed = new boolean[4];
    
    // Colores LNs
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
    private int Max_vida = 50;
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
    private boolean pausa = false;
    private boolean lose = false;
    private String level;
    private int speed;
    
    private int bpm; // Nuevo parámetro
    
    // Pausa
    private String[] pauseOptions = {"Continuar", "Reiniciar", "Settings", "Salir"};
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

        Musica.reproducirMusica("resources/Music/"+levelName+".wav");
        arrows = ChartLoader.loadChart(new File("resources/Levels/level"+levelName+".txt"), gw, true, GW.SQ(speed), bpm);
        for (arrow a : arrows) {
        	if(a.Long) {
            	a.color = colors[getColumnFromX((int)a.x)];
            } else if(!a.isEnd) {
            	a.image = new ImageIcon("resources/Sprites/Ritmo/Nota"+(getColumnFromX((int)a.x)+1)+".png").getImage();
            }
        }
    }

    public void update() {
    	
    	if(pausa || lose) {
    		return;
    	}
    	
    	if(!Musica.estaCorriendo() && !win) {
    		Musica.reanudarMusica();
    	}
    	
    	// Perder
    	if(vida <= 0) {
    		lose = true;
    	}
    	
    	// Max Vida
    	if(vida > Max_vida) {
    		vida = Max_vida;
    	}
    	
        for (arrow a : arrows) {
            a.move();
            
        }
        
        // Desaparecer si se van
        for (int i = 0; i < arrows.size(); i++) {
            arrow a = arrows.get(i);

            // Si es nota del final terminar
            if (a.isEnd && a.y <= GW.SY(125)) {
                win = true;
                return;
            }
            
            // Si se pasa, desaparecer
            if (a.y + a.size < 0) {
                arrows.remove(i);
                combo = 0;
                vida -= 5;
                missCount++;
            }
            
            // Nota larga
            if(a.Long && a.y <= GW.SY(125) && columnPressed[getColumnFromX((int)a.x)]) {
            	arrows.remove(i);
            	combo++;
            	vida += 1;
            	puntaje += 25;
            }
        }
        
        
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
    			g2d.drawImage(new ImageIcon("resources/Sprites/Ritmo/tecla"+(i+1)+"Press.png").getImage(),
        				posX,
        				GW.SY(125),
        				GW.SX(100),
        				GW.SY(100),
        				this);
    		} else {
    			g2d.drawImage(new ImageIcon("resources/Sprites/Ritmo/tecla"+(i+1)+".png").getImage(),
        				posX,
        				GW.SY(125),
        				GW.SX(100),
        				GW.SY(100),
        				this);
    		}
    		
    		posX += GW.SX(150);
    	}
        

        for (arrow a : arrows) {
            g2d.drawImage(a.image, (int)a.x, (int)a.y, a.size, a.size, this);
            if(a.Long) {
            	a.draw(g2d);
            }
        }
        
        drawUI(g2d);

        g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(15f));
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
        Font font1 = GameWindow.Pixelart.deriveFont(45f);
        g2d.setFont(font1);
        FontMetrics fm1 = g2d.getFontMetrics(font1);
        String textoPuntaje = "" + puntaje;
        int anchoPuntaje = fm1.stringWidth(textoPuntaje);
        g2d.setColor(Color.WHITE);
        g2d.drawString(textoPuntaje, GW.SX(1880) - anchoPuntaje, GW.SY(100));
        
        int textWidth;
        // Mostrar Combo
        if(combo != 0) {
        	g2d.setColor(Color.WHITE);
            Font font = GameWindow.Pixelart.deriveFont(40f);
            g2d.setFont(font);

            // Centrar Texto
            FontMetrics fm = g2d.getFontMetrics(font);
            String textoCombo = "" + combo;
            textWidth = fm.stringWidth(textoCombo);
            g2d.drawString(textoCombo, GW.SX(955) - textWidth / 2, GW.SY(500));
        }
        

        // Mostrar porcentaje
        Image image = percentageToRank(getAccuracyPercentage());
        Font font3 = GameWindow.Pixelart.deriveFont(45f);
        g2d.setFont(font3);
        FontMetrics fm3 = g2d.getFontMetrics(font3);
        String textoPorcentaje = getAccuracyPercentage() + "%";
        int anchoPorcentaje = fm3.stringWidth(textoPorcentaje);
        g2d.drawString(textoPorcentaje, GW.SX(1880) - anchoPorcentaje, GW.SY(200));
        g2d.drawImage(image, GW.SX(1600), GW.SY(150), GW.SX(60), GW.SY(60), this);
        
        // Contadores
        g2d.setFont(GameWindow.Pixelart.deriveFont(30f));
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
            
            g2d.setFont(GameWindow.Pixelart.deriveFont(30f));
            textWidth = g2d.getFontMetrics().stringWidth(lastHitText);
            g2d.drawString(lastHitText, GW.SX(955) - textWidth / 2, GW.SY(600));
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
    	Image image = percentageToRank(porcentajeFinal);
    	g2d.drawImage(image, GW.SX(550), GW.SY(300), GW.SX(300), GW.SY(300), this);
    	
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
    
    public void drawLOSE(Graphics2D g2d) {
    	// Oscurecer fondo
    	g2d.setColor(new Color(0,0,0,150));
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	
    	g2d.setColor(Color.WHITE);
        g2d.setFont(GameWindow.Pixelart.deriveFont(80f));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "PERDISTE";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, GW.SY(350));
        
     // Opciones
        g2d.setFont(GameWindow.Pixelart.deriveFont(55f));
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
    		return new ImageIcon("resources/Sprites/rankings/rankSS.png").getImage();
    	} else if (percentage >= 95) {
    		return new ImageIcon("resources/Sprites/rankings/rankS.png").getImage();
    	} else if (percentage >= 90) {
    		return new ImageIcon("resources/Sprites/rankings/rankA.png").getImage();
    	} else if (percentage >= 80) {
    		return new ImageIcon("resources/Sprites/rankings/rankB.png").getImage(); 		
    	} else if (percentage >= 70) {
    		return new ImageIcon("resources/Sprites/rankings/rankC.png").getImage();
    	} else {
    		return new ImageIcon("resources/Sprites/rankings/rankD.png").getImage();
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
        int WindowBue = GW.SY(100);
        int WindowAura = GW.SY(65);
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
                        SumaV = -8;
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

    
    // Detectar teclas
    public void handleKeyPress(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
        	
        	if(win) {
        		gameWindow.startGame(); // volver al RPG
        		Musica.detenerMusica();
                return;
        	} else if(!lose){
        		pausa = !pausa;
        		if(pausa) {
        			Musica.pausarMusica();
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
        
        int column = -1;
        if (keyCode == GameSettings.KEY_NLEFT && !columnPressed[0]) {
        	column = 0;
        	columnPressed[0] = true;
        }
        else if (keyCode == GameSettings.KEY_NDOWN && !columnPressed[1]) {
        	column = 1;
        	columnPressed[1] = true;
        }
        else if (keyCode == GameSettings.KEY_NUP && !columnPressed[2]) {
        	column = 2;
        	columnPressed[2] = true;
        }
        else if (keyCode == GameSettings.KEY_NRIGHT && !columnPressed[3]) {
        	column = 3;
        	columnPressed[3] = true;
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
                break;
            case 1: // Reiniciar
            	gameWindow.startRitmo(level, speed, bpm);
                break;
            case 2:
            	gameWindow.settingsGame();
            	break;
            case 3: // Salir
                gameWindow.startGame(); // volver al RPG
                break;
        }
    }
    
    private void handleLoseSelection() {
        switch (selectedLoseOption) {
            case 0: // Reintentar
            	gameWindow.startRitmo(level, speed, bpm);
                break;
            case 1: // Salir
            	gameWindow.startGame();
                break;
        }
    }
    
}
