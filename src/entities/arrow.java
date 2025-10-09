package entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import main.GW;

public class arrow {
	public int size = GW.SX(100);
	private double speed;
	public boolean Long;
	public Color color;
	public double x, y;
	public boolean isEnd = false;
	public Image image;
	
	public arrow(double startX, double startY, double speed, boolean isEnd, boolean Long) {
		this.x = startX;
		this.y = startY;
		this.speed = speed;
		this.isEnd = isEnd;
		this.Long = Long;
	}
	
	public void move() {
		y -= speed;
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x, (int)y, size, size);
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(color);
		g2d.fillRect((int)x + size/3, (int)y, size/3, size);
    }
}
