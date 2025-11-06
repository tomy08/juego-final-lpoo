package main;

import javax.swing.*;

import Sonidos.Musica;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class StoryScreen extends JPanel implements KeyListener {
	
    private String[] paragraphs;

    private int currentParagraph = 0;
    private String displayedText = "";
    private int charIndex = 0;
    private Timer typingTimer;
    private GameWindow gameWindow;
    private int typeStory;
    private int delay = 40;
    private boolean typing = false;
    private boolean finished = false;
    private boolean NoSkip = false;;

    public StoryScreen(GameWindow gameWindow, int story) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        this.typeStory = story;
        Musica.reproducirMusica("resources/Music/historia.wav");
        Musica.enableLoop();
        switch(story) {
        case 1: // Principio del juego
        	paragraphs = new String[] {
        			"En la Escuela técnica 35...",
        			"Un alumno de computación estaba muy a las justas con las materias de Moya.",
        			"Este alumno aunque intentaba no podía aprobar, era muy dificil para él",
        			"Así que terminando las clases, este  decidió hablar con su profesor acerca de su nota.",
        			"El alumno decidido a aprobar, optó por hacerle una serie de favores a Moya, los cuales serían:",
        			"Llevarle una Lapicera, para poder llenar la lista de asistencia... Solo findlay te puede prestar una",
        			"Un procesador para reparar una computadora que no funciona bien en el laboratorio.",
        			"Un vinilo de la renga... que lo tiene la señora de la biblioteca",
        			"Y finalmente... Un fusible, porque el laboratorio de al lado se quedó sin luz",
        			"Si el alumno logra conseguir los objetos logrará aprobar la materia. Así que se emprenderá en esta odisea...",
        			"En la ODISEA DE MOYA."
        	};
        	break;
        	
        case 2: // Final Normal
        	paragraphs = new String[] {
        			"...",
        			"Y así fué como el alumno pudo aprobar la materia de moya...",
        			"Con varios enojos en su camino... sus compañeros no quedaron satisfechos con él...",
        			"Él mismo se siente culpable... Ahora su cabeza piensa:",
        			"Qué pasaría si hubiera hecho mejor las cosas?",
        			"Qué pasaría si no me peleaba con ninguno de mi curso y quedaba bien con todos?",
        			"Pensamientos que el alumno no puede cambiar... Pero el jugador si.",
        			"ODISEA DE MOYA: FINAL NEUTRAL",
        			"Para la próxima intentá amigarte con los de Quinto Primera en vez de pelear con ellos..."
        	};
        	break;
        	
        case 3: // Final Bueno
        	paragraphs = new String[] {
        			"...",
        			"Y así fué como el alumno pudo aprobar la materia de moya...",
        			"Esta vez el alumno no se arrepintió de nada, no solo logró aprobar la materia",
        			"También pudo quedar bien con la mayor parte de sus compañeros.",
        			"Linzalata por su parte, tuvo que aprobar al alumno con algo de rechazo",
        			"Por lo que el aprobado pudo decidirse por terminar este año del colegio sin materias previas",
        			"Preparándose para el año que viene...",
        			"Ahora solo le queda rendir las otras 8 bajas que tiene.",
        			"LA ODISEA DE MOYA: FINAL BUENO"
        	};
        	break;
        	
        case 4: // Final secreto
        	delay = 60;
        	paragraphs = new String[] {
        			"...",
        			"...",
        			"Por qué?",
        			"Simplemente... Por qué?",
        			"El Alumno tuvo que replantearse muchas cosas por su camino... Pero...",
        			"Realmente Ricky Fort estaba en el colegio? desde hace cuánto?",
        			"Luego de los acontecimientos... El alumno no lo dudó, pensó que su vida fué una farsa",
        			"El alumno dejó la secundaria, se dedicó a las conspiraciones de reddit y twitter",
        			"Nunca nadie le creyó... Pero en su mente va a quedar...",
        			"Que vió al comandante una vez más...",
        			"ODISEA DE MOYA: FINAL SECRETO"
        	};
        	break;
        	
        default: 
        	paragraphs = new String[] {
        			"No se cargó el texto"
        	};
        }
    }

    public void startTyping() {
        typing = true;
        displayedText = "";
        charIndex = 0;

        typingTimer = new Timer(delay, e -> {
            if (charIndex < paragraphs[currentParagraph].length()) {
                displayedText += paragraphs[currentParagraph].charAt(charIndex);
                charIndex++;
                GameWindow.reproducirSonido("resources/sounds/tipeo.wav");
                repaint();
            } else {
                typingTimer.stop();
                typing = false;
            }
        });
        typingTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(gameWindow.Pixelart.deriveFont(GW.SF(36f)));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(displayedText)) / 2;
        int y = getHeight() / 2;
        g.drawString(displayedText, Math.max(30, x), y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (typing) {
            	if(NoSkip) {
            		return;
            	}
                // Terminar texto instantáneamente
                typingTimer.stop();
                displayedText = paragraphs[currentParagraph];
                typing = false;
                repaint();
            } else {
                // Pasar al siguiente párrafo o terminar historia
                currentParagraph++;
                
                // Efectos por historia:
                switch(typeStory) {
                
                case 1: // Inicio del juego
                	if(currentParagraph == 10) {
                		Musica.detenerMusica();
                    	delay = 100;
                    	NoSkip = true;
                    }
                	break;
                	
                case 2: // Final neutral
                	if(currentParagraph == 7) {
                		Musica.detenerMusica();
                    	delay = 110;
                    	NoSkip = true;
                    } else {
                    	delay = 40;
                    	NoSkip = false;
                    }
                	break;
                	
                case 3: // Final bueno
                	if(currentParagraph == 8) {
                		Musica.detenerMusica();
                    	delay = 110;
                    	NoSkip = true;
                    }
                	break;
                	
                case 4: // Final secreto
                	if(currentParagraph == 10) {
                		Musica.detenerMusica();
                    	delay = 110;
                    	NoSkip = true;
                    }
                	break;
                }
                
                
                if (currentParagraph < paragraphs.length) {
                    startTyping();
                } else {
                    finished = true;
                    if(typeStory == 1) { // Iniciar juego
                    	gameWindow.startRealGame();                    	
                    } else { // Volver al menú
                    	GameSaveManager.eliminarPartida();
                    	GameSaveManager.reiniciarPartida(gameWindow.gamePanel, gameWindow.gamePanel.player);
                    	gameWindow.showMainMenu();
                    }
                }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
