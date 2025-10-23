package entities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import main.GamePanel;
import main.GameWindow;

public class NPC {
	
	public int size;
	public double x,y;
	
	// Distinciones de npc
	public String Tipo;
	public int line = 1;
	public int FinalLine = -1;
	public int Trigger = 0; // IDs de trigger para poder tener mas varianza
	
	public boolean interactive = false;
	private GamePanel panel;
	private Color color;

	public NPC(double startX, double startY, int size, String Tipo, GamePanel panel) {
		this.x = startX;
		this.y = startY;
		this.size = size;
		this.panel = panel;
		this.Tipo = Tipo;
		this.color = Color.GREEN;
	}
	
	public int npcLine() {
		
		// Triggers de NPCs
		
		// Zambrana
		if(Tipo.equals("Zambrana") && Trigger == 1) {
			line = 3;
		}
		
		// Melody
		if(Tipo.equals("Melody") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("Melody") && Trigger == 2) {
			line = 8;
		}
		
		// Kreimer
		if(Tipo.equals("Kreimer") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("Kreimer") && Trigger == 2) {
			line = 7;
		}
		
		// Gera
		if(Tipo.equals("Gera") && Trigger == 1) {
			line = 5;
		}
		
		// Findlay
		if(Tipo.equals("Findlay") && Trigger == 1) {
			line = 8;
		}
		
		// Lavega
		if(Tipo.equals("Lavega") && Trigger == 1) {
			line = 4;
		}
		if(Tipo.equals("Lavega") && Trigger == 2) {
			line = 6;
		}
		
		// Ulises
		if(Tipo.equals("Ulises") && Trigger == 1) {
			line = 6;
		}
		
		// Martin
		if(Tipo.equals("Martin") && Trigger == 1) { // Despues de hablarle por primera vez
			line = 6;
		}
		if(Tipo.equals("Martin") && Trigger == 2) { // Cuando tenes todos los marcadores
			line = 8;
		}
		if(Tipo.equals("Martin") && Trigger == 3) { // Despues de ganarle 
			line = 12;
		}
		
		return line;
	}
	
	public int npcFinalLine() {
		
		return FinalLine;
	}
	
	public void drawNPC(Graphics2D g2d) {
		
		g2d.setColor(color);
        g2d.fillRect((int)x - (int)panel.CameraX, (int)y - (int)panel.CameraY, size, size);
		
	}
	
	public void drawInteractive(Graphics2D g2d, String tecla) {
	    if(interactive) {
	        g2d.setColor(new Color(0,0,0,125));
	        g2d.fillRect((int)x - (int)panel.CameraX + size/2 - 15, (int)y - (int)panel.CameraY - 60, 30, 30);
	        
	        // Dibujar borde
	        g2d.setColor(Color.WHITE);
	        g2d.setStroke(new BasicStroke(2));
	        g2d.drawRect((int)x - (int)panel.CameraX + size/2 - 15, (int)y - (int)panel.CameraY - 60, 30, 30);
	        
	        g2d.setFont(GameWindow.Pixelart.deriveFont(20f));
	        g2d.drawString(tecla, (int)x - (int)panel.CameraX + size/2 - 7, (int)y - (int)panel.CameraY - 40);
	    }
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x, (int)y, size, size);
	}
	
	public Rectangle getArea() {
		return new Rectangle((int)x - size, (int)y - size, size*3, size*3);
	}
	
}
