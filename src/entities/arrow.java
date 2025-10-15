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
    private double hitTimeMs; // tiempo exacto al llegar a la hit line

    public arrow(double startX, double hitY, double speed, boolean isEnd, boolean Long, double hitTimeMs) {
        this.x = startX;
        this.y = hitY + speed * hitTimeMs;
        this.speed = speed;
        this.isEnd = isEnd;
        this.Long = Long;
        this.hitTimeMs = hitTimeMs;
    }

    public void update(double currentTimeMs, double hitY) {
        // y se mueve según tiempo hasta la hit line
        double timeRemaining = hitTimeMs - currentTimeMs;
        y = hitY + speed * timeRemaining;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, size, size);
    }

    public void draw(Graphics2D g2d) {
        if(Long) {
            g2d.setColor(color);
            g2d.fillRect((int)x + size/3, (int)y, size/3, size);
        }
    }
}
