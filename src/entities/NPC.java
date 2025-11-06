package entities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

import main.GW;
import main.GamePanel;
import main.GameWindow;

public class NPC {
	
	public int size;
	public double x,y;
	
	// Distinciones de npc
	public String Tipo;
	public int line = 1;
	public int Trigger = 0; // IDs de trigger para poder tener mas varianza
	
	public boolean interactive = false;
	private GamePanel panel;
	private Color color;
	
	// imagen
	
	public Image image;

	public NPC(double startX, double startY, int size, String Tipo, GamePanel panel) {
		this.x = startX;
		this.y = startY;
		this.size = size;
		this.panel = panel;
		this.Tipo = Tipo;
		this.color = Color.GREEN;
		
		image = new ImageIcon("resources/Sprites/NPC/"+Tipo+".png").getImage();
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
			line = 11;
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
		
		// Guerra
		if(Tipo.equals("Guerra") && Trigger == 1) {
			line = 6;
		}
		if(Tipo.equals("Guerra") && Trigger == 2) {
			line = 12;
		}
		
		// Ascensores
		if(Tipo.equals("Ascensor") && Trigger == 1) {
			line = 4;
		}
		if(Tipo.equals("ASCENSOR") && Trigger == 1) {
			line = 5;
		}
		
		// Pecile
		if(Tipo.equals("Pecile") && Trigger == 1) {
			line = 4;
		}
		if(Tipo.equals("Pecile") && Trigger == 2) {
			line = 11;
		}
		
		// Ciccaroni
		if(Tipo.equals("Ciccaroni") && Trigger == 1) {
			line = 6;
		}
		if(Tipo.equals("Ciccaroni") && Trigger == 2) {
			line = 11;
		}
		
		// Signorello
		if(Tipo.equals("Signorello") && Trigger == 1) {
			line = 7;
		}
		if(Tipo.equals("Signorello") && Trigger == 2) {
			line = 9;
		}
		
		// Ledesma
		if(Tipo.equals("Ledesma") && Trigger == 1) {
			line = 5;
		}
		if(Tipo.equals("Ledesma") && Trigger == 2) {
			line = 7;
		}
		if(Tipo.equals("Ledesma") && Trigger == 3) {
			line = 12;
		}
		
		// Pacheco
		if(Tipo.equals("Pacheco") && Trigger == 1) {
			line = 4;
		}
		if(Tipo.equals("Pacheco") && Trigger == 2) {
			line = 7;
		}
		
		// Moya
		if(Tipo.equals("Moya") && Trigger == 1) {
			line = 6;
		}
		if(Tipo.equals("Moya") && Trigger == 2) {
			line = 9;
		}
		if(Tipo.equals("Moya") && Trigger == 3) {
			line = 22;
		}
		
		// Linzalata
		if(Tipo.equals("Linzalata") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("Linzalata") && Trigger == 2) {
			line = 9;
		}
		
		// Tachos de basura
		if(Tipo.equals("TAcho") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("TACho") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("TACHO") && Trigger == 1) {
			line = 3;
		}
		
		// Biblioteca
		if(Tipo.equals("Biblioteca") && Trigger == 1) {
			line = 8;
		}
		
		// Casas
		if(Tipo.equals("Casas") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("Casas") && Trigger == 2) {
			line = 8;
		}
		if(Tipo.equals("Casas") && Trigger == 3) {
			line = 10;
		}
		
		// Rita
		if(Tipo.equals("Rita") && Trigger == 1) {
			line = 7;
		}
		
		// Vagos
		if(Tipo.equals("Vagos") && Trigger == 1) {
			line = 3;
		}
		if(Tipo.equals("Vagos") && Trigger == 2) {
			line = 10;
		}
		
		// Gramajo
		if(Tipo.equals("Gramajo") && Trigger == 1) {
			line = 8;
		}
		if(Tipo.equals("Gramajo") && Trigger == 2) {
			line = 11;
		}
		
		// Gennuso
		if(Tipo.equals("Gennuso") && Trigger == 1) {
			line = 12;
		}
		
		// Interino
		if(Tipo.equals("Interino") && Trigger == 1) {
			line = 6;
		}
		if(Tipo.equals("Interino") && Trigger == 2) {
			line = 9;
		}
		
		// Estufa
		if(Tipo.equals("Estufa") && Trigger == 1) {
			line = 3;
		}
		
		// Caja
		if(Tipo.equals("Caja") && Trigger == 1) {
			line = 6;
		}
		
		return line;
	}
	
	public void drawNPC(Graphics2D g2d) {
		
		if (image != null) {
	        g2d.drawImage(image, 
	                      (int)x - (int)panel.CameraX - GW.SX(4), 
	                      (int)y - (int)panel.CameraY - GW.SX(size), 
	                      GW.SX(size) + GW.SX(8), GW.SX(size)*2, 
	                      panel);
	    } else {
			g2d.setColor(color);
	        g2d.fillRect((int)x - (int)panel.CameraX, (int)y - (int)panel.CameraY, GW.SX(size), GW.SX(size));
	    }
		
	}
	
	public void drawInteractive(Graphics2D g2d, String tecla) {
	    if(interactive) {
	        g2d.setColor(new Color(0,0,0,125));
	        g2d.fillRect((int)x - (int)panel.CameraX + GW.SX(size)/2 - 15, (int)y - (int)panel.CameraY - 60, 30, 30);
	        
	        // Dibujar borde
	        g2d.setColor(Color.WHITE);
	        g2d.setStroke(new BasicStroke(2));
	        g2d.drawRect((int)x - (int)panel.CameraX + GW.SX(size)/2 - 15, (int)y - (int)panel.CameraY - 60, 30, 30);
	        
	        g2d.setFont(GameWindow.Pixelart.deriveFont(20f));
	        g2d.drawString(tecla, (int)x - (int)panel.CameraX + GW.SX(size)/2 - 7, (int)y - (int)panel.CameraY - 40);
	    }
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x, (int)y, GW.SX(size), GW.SX(size));
	}
	
	public Rectangle getArea() {
		return new Rectangle((int)x - GW.SX(size), (int)y - GW.SX(size), GW.SX(size)*3, GW.SX(size)*3);
	}
	
}
