package main;
import javax.swing.*;

import entities.NPC;
import entities.Player;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GamePanel extends JPanel {
	
    private GameWindow gameWindow;
    private Player player;
    private ArrayList<NPC> NPCs = new ArrayList<>();
    private Set<Integer> pressedKeys;
    
    // Seguimiento de Camara
    public double CameraX = 0;
    public double CameraY = 0;
    
    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        
        pressedKeys = new HashSet<>();
        
        // Inicializar jugador en el centro de la pantalla
        player = new Player(400, 400, this);
        NPCs.add(new NPC(200, 200, 30, this));
        
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar jugador
        player.draw(g2d);
        
        // Dibujar NPCs
        for(NPC npc : NPCs) {
        	 npc.drawNPC(g2d);
        	 npc.drawInteractive(g2d);
        }
        // Dibujar UI
        drawUI(g2d);
    }
    
    private void drawUI(Graphics2D g2d) {
        // Instrucciones de control
        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.PLAIN, 16);
        g2d.setFont(font);
        g2d.drawString("WASD o flechas para mover", 10, 25);
        g2d.drawString("ESC para volver al menú", 10, 45);
        
        // Mostrar posición del jugador (para debug)
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Posición: (" + (int)player.getX() + ", " + (int)player.getY() + ")", 10, getHeight() - 20);
    }
    
    public void update() {
        // Actualizar movimiento del jugador basado en teclas presionadas
        boolean moving = false;
        double deltaX = 0, deltaY = 0;
        
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) {
            deltaY = -1;
            moving = true;
        }
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) {
            deltaY = 1;
            moving = true;
        }
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) {
            deltaX = -1;
            moving = true;
        }
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            deltaX = 1;
            moving = true;
        }
        
        // Normalizar movimiento diagonal
        if (deltaX != 0 && deltaY != 0) {
            deltaX *= 0.707; // 1/sqrt(2) para mantener velocidad constante
            deltaY *= 0.707;
        }
        
        // Interactuar con NPCs
    	
        for (NPC npc : NPCs) {
        	
        	// Entrar al área para interactuar
        	if (player.getBounds().intersects(npc.getArea())) {
        		npc.interactive = true;
        	} else {
        		npc.interactive = false;
        	}
        	
        	// Colisión con el NPC
            if (player.getBounds().intersects(npc.getBounds())) {

                Rectangle p = player.getBounds();
                Rectangle n = npc.getBounds();

                int overlapLeft   = p.x + p.width - n.x; // Cuánto se metió por la izquierda
                int overlapRight  = n.x + n.width - p.x; // Por la derecha
                int overlapTop    = p.y + p.height - n.y; // Por arriba
                int overlapBottom = n.y + n.height - p.y; // Por abajo

                // El eje con menor solapamiento es el eje donde chocó
                int minOverlapX = Math.min(overlapLeft, overlapRight);
                int minOverlapY = Math.min(overlapTop, overlapBottom);

                if (minOverlapX < minOverlapY) {
                    // Colisión horizontal
                    if (overlapLeft < overlapRight) {
                        player.setX(player.getX() - overlapLeft);
                    } else {
                        player.setX(player.getX() + overlapRight);
                    }
                } else {
                    // Colisión vertical
                    if (overlapTop < overlapBottom) {
                        player.setY(player.getY() - overlapTop);
                    } else {
                        player.setY(player.getY() + overlapBottom);
                    }
                }

                moving = false;
            }
        }

        
        if (moving) {
            player.move(deltaX, deltaY, getWidth(), getHeight());
        }
        player.update();
    }
    
    public void handleKeyPress(int keyCode) {
        pressedKeys.add(keyCode);
        
        if (keyCode == KeyEvent.VK_ESCAPE) {
            gameWindow.backToMenu();
        }
    }
    
    public void handleKeyRelease(int keyCode) {
        pressedKeys.remove(keyCode);
    }
}