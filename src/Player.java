import java.awt.*;

public class Player {
    private double x, y;
    private double speed;
    private int size;
    private Color color;
    
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 5.0;
        this.size = 30;
        this.color = Color.CYAN;
    }
    
    public void move(double deltaX, double deltaY, int screenWidth, int screenHeight) {
        // Calcular nueva posición
        double newX = x + (deltaX * speed);
        double newY = y + (deltaY * speed);
        
        // Limitar movimiento dentro de los límites de la pantalla
        if (newX >= 0 && newX <= screenWidth - size) {
            x = newX;
        }
        if (newY >= 0 && newY <= screenHeight - size) {
            y = newY;
        }
    }
    
    public void update() {
        // Aquí se pueden agregar más actualizaciones del jugador en el futuro
        // como animaciones, efectos, etc.
    }
    
    public void draw(Graphics2D g2d) {
        // Dibujar el jugador como un cuadrado
        g2d.setColor(color);
        g2d.fillRect((int)x, (int)y, size, size);
        
        // Dibujar borde
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int)x, (int)y, size, size);
        
        // Dibujar una pequeña cruz en el centro para indicar dirección
        g2d.setColor(Color.WHITE);
        int centerX = (int)x + size/2;
        int centerY = (int)y + size/2;
        int crossSize = 6;
        g2d.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY);
        g2d.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize);
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return speed; }
    public int getSize() { return size; }
    
    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setColor(Color color) { this.color = color; }
}