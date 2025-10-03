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
	public boolean Trigger = false;
	
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
		if(Tipo.equals("Mauro") && Trigger) {
			line = 3;
		}
		
		return line;
	}
	
	public int npcFinalLine() {
		
		if(Tipo.equals("random") && Trigger) {
			FinalLine = 1;
		}
		
		return FinalLine;
	}
	
	public void drawNPC(Graphics2D g2d) {
		
		g2d.setColor(color);
        g2d.fillRect((int)x - (int)panel.CameraX, (int)y - (int)panel.CameraY, size, size);
		
	}
	
	public void drawInteractive(Graphics2D g2d) {
		if(interactive) {
	        g2d.setColor(new Color(0,0,0,125));
	        g2d.fillRect((int)x - (int)panel.CameraX + size/2 - 15, (int)y - (int)panel.CameraY - 60, 30, 30);
	        
	        // Dibujar borde
	        g2d.setColor(Color.WHITE);
	        g2d.setStroke(new BasicStroke(2));
	        g2d.drawRect((int)x - (int)panel.CameraX + size/2 - 15, (int)y - (int)panel.CameraY - 60, 30, 30);
	        
	        g2d.setFont(GameWindow.Pixelart.deriveFont(20f));
	        g2d.drawString("E",(int)x - (int)panel.CameraX + size/2 - 7, (int)y - (int)panel.CameraY - 40);
		}
		
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x, (int)y, size, size);
	}
	
	public Rectangle getArea() {
		return new Rectangle((int)x - size, (int)y - size, size*3, size*3);
	}
	
}
