package entities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import Mapa.CollisionMap;
import main.GW;
import main.GamePanel;
import main.GameWindow;
import entities.Inventory;
import entities.Item;
import entities.ItemStack;

public class Player {
    private double x, y;
    private double speed;
    private int size;
    private Color color;
    private GamePanel panel;
    private CollisionMap collisionMap;
    public Image image;
    public boolean facingLeft = false;
    public Inventory inventory;
    private boolean teleportCooldown = false;
    
    private long lastSound = 0;
    private int delaySound = 350;
    
    // Sistema de animación
    private BufferedImage[] downFrames;
    private BufferedImage[] upFrames;
    private BufferedImage[] sideFrames;
    private BufferedImage idleDown;  // Sprite estático mirando abajo
    private BufferedImage idleUp;    // Sprite estático mirando arriba
    private BufferedImage idleSide;  // Sprite estático mirando al lado
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private int frameDelay = 150; // ms entre frames
    private String currentDirection = "down"; // down, up, left, right
    private boolean isMoving = false;
    
    public Player(double startX, double startY, GamePanel panel) {
        this.x = startX;
        this.y = startY;
        this.speed = GW.SQ(8);
        this.size = GW.SX(40);
        this.panel = panel;
        this.color = Color.CYAN;
        this.collisionMap = null; // Se establecerá después
        // Crear inventario: hotbar 9, grid 9x3 (como Minecraft simplificado)
        this.inventory = new Inventory(9, 3);
        
        // Cargar animaciones
        loadAnimations();
    }
    
    /**
     * Establece el mapa de colisiones para este jugador
     */
    public void setCollisionMap(CollisionMap collisionMap) {
        this.collisionMap = collisionMap;
    }
    
    /**
     * Carga las animaciones del jugador desde los archivos
     */
    private void loadAnimations() {
        try {
            // Cargar frames de animación hacia abajo
            downFrames = new BufferedImage[4];
            downFrames[0] = ImageIO.read(new File("resources/Sprites/Jugador/down-frame1.png"));
            downFrames[1] = ImageIO.read(new File("resources/Sprites/Jugador/down-frame2.png"));
            downFrames[2] = ImageIO.read(new File("resources/Sprites/Jugador/down-frame3.png"));
            downFrames[3] = ImageIO.read(new File("resources/Sprites/Jugador/down-frame4.png"));
            
            // Cargar frames de animación hacia arriba
            upFrames = new BufferedImage[4];
            upFrames[0] = ImageIO.read(new File("resources/Sprites/Jugador/up-frame1.png"));
            upFrames[1] = ImageIO.read(new File("resources/Sprites/Jugador/up-frame2.png"));
            upFrames[2] = ImageIO.read(new File("resources/Sprites/Jugador/up-frame3.png"));
            upFrames[3] = ImageIO.read(new File("resources/Sprites/Jugador/up-frame4.png"));
            
            // Cargar frames de animación lateral
            sideFrames = new BufferedImage[4];
            sideFrames[0] = ImageIO.read(new File("resources/Sprites/Jugador/side-frame1.png"));
            sideFrames[1] = ImageIO.read(new File("resources/Sprites/Jugador/side-frame2.png"));
            sideFrames[2] = ImageIO.read(new File("resources/Sprites/Jugador/side-frame3.png"));
            sideFrames[3] = ImageIO.read(new File("resources/Sprites/Jugador/side-frame4.png"));
            
            // Cargar sprites estáticos (idle)
            idleDown = ImageIO.read(new File("resources/Sprites/Jugador/pj-down.png"));
            idleUp = ImageIO.read(new File("resources/Sprites/Jugador/pj-up.png"));
            idleSide = ImageIO.read(new File("resources/Sprites/Jugador/pj-side.png"));
            
            System.out.println("Animaciones del jugador cargadas correctamente");
        } catch (Exception e) {
            System.err.println("Error al cargar animaciones del jugador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean canTeleport() { return !teleportCooldown; }

    public void setTeleportCooldown() {
        teleportCooldown = true;
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() { teleportCooldown = false; }
        }, 400); // 400ms sin volver a teletransportar
    }

    
    public void move(double deltaX, double deltaY, int screenWidth, int screenHeight) {
        // Determinar si el jugador se está moviendo
        isMoving = (deltaX != 0 || deltaY != 0);
        
        // Determinar dirección basada en el movimiento
        if (isMoving) {
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                // Movimiento horizontal predominante
                if (deltaX > 0) {
                    currentDirection = "right";
                    facingLeft = false;
                } else {
                    currentDirection = "left";
                    facingLeft = true;
                }
            } else {
                // Movimiento vertical predominante
                if (deltaY > 0) {
                    currentDirection = "down";
                } else {
                    currentDirection = "up";
                }
            }
        }
        
        // Calcular nueva posición
        double newX = x + (deltaX * speed);
        double newY = y + (deltaY * speed);
        
        // Si hay mapa de colisiones, verificar colisión
        if (collisionMap != null && collisionMap.isLoaded()) {
            // Verificar colisión en X
            if (collisionMap.canMoveTo(newX, y, size, size)) {
                x = newX;
            }
            // Verificar colisión en Y
            if (collisionMap.canMoveTo(x, newY, size, size)) {
                y = newY;
            }
        } else {
            // Sin mapa de colisiones, usar límites de pantalla
            if (newX >= 0 && newX <= screenWidth - size) {
                x = newX;
            }
            if (newY >= 0 && newY <= screenHeight - size) {
                y = newY;
            }
        }
        
        // Reproducir sonido de pasos SOLO si está en movimiento
        if(isMoving && System.currentTimeMillis() >= lastSound + delaySound) {        	
        	GameWindow.reproducirSonido("resources/sounds/footStep.wav");
        	lastSound = System.currentTimeMillis();
        }
        
        
    }
    
    public void update() {
    	// Seguimiento de la camara
    	panel.CameraX = x - panel.getWidth() / 2 + size / 2;
        panel.CameraY = y - panel.getHeight() / 2 + size / 2;
        
        // Actualizar animación SOLO si se está moviendo
        if (isMoving) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime >= frameDelay) {
                currentFrame = (currentFrame + 1) % 4; // Ciclar entre 0-3
                lastFrameTime = currentTime;
            }
        } else {
            // Si no se mueve, resetear a frame 0 y pausar animación
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis(); // Reset del timer
        }
    }
    
    public void draw(Graphics2D g2d) {
        // Obtener el frame actual según la dirección
        BufferedImage currentSprite = getCurrentSprite();
        
        if (currentSprite != null) {
            // Calcular posición en pantalla (relativa a la cámara)
            int screenX = (int)x - (int)panel.CameraX;
            int screenY = (int)y - (int)panel.CameraY;
            
            // Si se mueve a la izquierda, voltear la imagen horizontalmente
            if (currentDirection.equals("left")) {
                // Dibujar volteado horizontalmente
                g2d.drawImage(currentSprite, 
                    screenX + size, screenY-size,  // Posición X invertida
                    -size, size*2,               // Ancho negativo para voltear
                    null);
            } else {
                // Dibujar normalmente
                g2d.drawImage(currentSprite, screenX, screenY-size, size, size*2, null);
            }
            
        } else {
            // Fallback: dibujar el cuadrado original si las imágenes no cargan
            g2d.setColor(color);
            g2d.fillRect((int)x - (int)panel.CameraX, (int)y - (int)panel.CameraY, size, size);
            
            // Dibujar borde
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect((int)x - (int)panel.CameraX, (int)y - (int)panel.CameraY, size, size);
            
            // Dibujar una pequeña cruz en el centro para indicar dirección
            g2d.setColor(Color.WHITE);
            int centerX = (int)x + size/2;
            int centerY = (int)y + size/2;
            int crossSize = 6;
            g2d.drawLine(centerX - crossSize - (int)panel.CameraX, centerY - (int)panel.CameraY, centerX + crossSize - (int)panel.CameraX, centerY - (int)panel.CameraY);
            g2d.drawLine(centerX - (int)panel.CameraX, centerY - crossSize - (int)panel.CameraY, centerX - (int)panel.CameraX, centerY + crossSize  - (int)panel.CameraY);
        }
    }
    
    /**
     * Obtiene el sprite actual según la dirección y frame de animación
     */
    private BufferedImage getCurrentSprite() {
        if (downFrames == null || upFrames == null || sideFrames == null) {
            return null;
        }
        
        // Si no se está moviendo, usar sprites estáticos (idle)
        if (!isMoving) {
            switch (currentDirection) {
                case "down":
                    return idleDown;
                case "up":
                    return idleUp;
                case "left":
                case "right":
                    return idleSide;
                default:
                    return idleDown;
            }
        }
        
        // Si se está moviendo, usar frames de animación
        switch (currentDirection) {
            case "down":
                return downFrames[currentFrame];
            case "up":
                return upFrames[currentFrame];
            case "left":
            case "right":
                return sideFrames[currentFrame];
            default:
                return downFrames[currentFrame];
        }
    }
    
    // Getters
    public Rectangle getBounds() {
    	return new Rectangle((int)x, (int)y, size, size);
    }
    
    // Permitir recoger un item (devuelve lo que no pudo entrar)
    public int pickupItem(Item item, int amount) {
        if (item == null || amount <= 0) return amount;
        return inventory.addItem(item, amount);
    }
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