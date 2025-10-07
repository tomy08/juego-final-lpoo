package entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import main.GW;

public class arrow {
	public int size = GW.SX(80);
	private double speed;
	public double x, y;
	public boolean isEnd = false;
	
	public arrow(double startX, double startY, double speed, boolean isEnd) {
		this.x = startX;
		this.y = startY;
		this.speed = speed;
		this.isEnd = isEnd;
	}
	
	public void move() {
		y -= speed;
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x, (int)y, size, size);
	}
	
	public void draw(Graphics2D g2d) {
		if(isEnd) {
			return;
		}
		g2d.setColor(Color.CYAN);
		
        g2d.fillRect((int)x, (int)y, size, size);
    }
}
