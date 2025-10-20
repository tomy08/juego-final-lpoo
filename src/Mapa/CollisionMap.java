package Mapa;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CollisionMap {
    private BufferedImage collisionImage;
    private int width;
    private int height;
    private double scaleFactor = 28.0; 
    public CollisionMap(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("⚠ No se encontró el mapa de colisiones: " + imagePath);
                collisionImage = null;
                width = height = 0;
                return;
            }

            collisionImage = ImageIO.read(imageFile);
            if (collisionImage == null) {
                System.err.println("⚠ No se pudo leer la imagen: " + imagePath);
                width = height = 0;
                return;
            }

            width = (int)(collisionImage.getWidth() * scaleFactor);
            height = (int)(collisionImage.getHeight() * scaleFactor);

            System.out.println("✓ Mapa cargado: " + collisionImage.getWidth() + "x" + collisionImage.getHeight());
            System.out.println("  Escalado lógico: " + width + "x" + height);

        } catch (IOException e) {
            System.err.println("⚠ Error al cargar el mapa de colisiones:");
            e.printStackTrace();
        }
    }

    // 🔹 Verifica colisión usando coordenadas escaladas, pero lee píxeles originales
    public boolean hasCollision(int x, int y) {
        if (collisionImage == null) return false;

        // Convertir coordenadas del mundo (escaladas) a píxeles del mapa original
        int mapX = (int)(x / scaleFactor);
        int mapY = (int)(y / scaleFactor);

        if (mapX < 0 || mapX >= collisionImage.getWidth() ||
            mapY < 0 || mapY >= collisionImage.getHeight()) {
            return true; // fuera de límites = colisión
        }

        int rgb = collisionImage.getRGB(mapX, mapY);
        Color color = new Color(rgb, true);
        
        // Ignorar colisión en zonas rojas (teleport)
        if (isRedPixel(color)) {
            return false;
        }
        
        int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return brightness < 128;
    }
    
    // Verifica si hay un teleport en la posición dada
    public boolean isTeleportZone(int x, int y) {
        if (collisionImage == null) return false;

        // Convertir coordenadas del mundo (escaladas) a píxeles del mapa original
        int mapX = (int)(x / scaleFactor);
        int mapY = (int)(y / scaleFactor);

        if (mapX < 0 || mapX >= collisionImage.getWidth() ||
            mapY < 0 || mapY >= collisionImage.getHeight()) {
            return false;
        }

        int rgb = collisionImage.getRGB(mapX, mapY);
        Color color = new Color(rgb, true);
        return isRedPixel(color);
    }
    
    public int getTeleportId(int x, int y) {
        if (collisionImage == null) return -1;

        // Convertir coordenadas del mundo (escaladas) a píxeles del mapa original
        int mapX = (int)(x / scaleFactor);
        int mapY = (int)(y / scaleFactor);

        if (mapX < 0 || mapX >= collisionImage.getWidth() ||
            mapY < 0 || mapY >= collisionImage.getHeight()) {
            return -1;
        }

        int rgb = collisionImage.getRGB(mapX, mapY);
        Color color = new Color(rgb, true);
        
        if (isRedPixel(color)) {
            // Retornar el valor del canal rojo como ID del teleport
            // Esto permite tener hasta 56 teleports diferentes (200-255)
            return color.getRed();
        }
        
        return -1;
    }
    
    // 🔹 Verifica si el jugador está en una zona de teleport (verifica rectángulo)
    public boolean isTeleportZoneRect(int x, int y, int width, int height) {
        // Verificar las 4 esquinas y el centro
        if (isTeleportZone(x, y)) return true;
        if (isTeleportZone(x + width - 1, y)) return true;
        if (isTeleportZone(x, y + height - 1)) return true;
        if (isTeleportZone(x + width - 1, y + height - 1)) return true;
        if (isTeleportZone(x + width/2, y + height/2)) return true;
        
        return false;
    }
    
    // 🔹 Obtiene el ID del teleport donde está el jugador (verifica rectángulo)
    public int getTeleportIdRect(int x, int y, int width, int height) {
        // Verificar el centro primero (más preciso)
        int id = getTeleportId(x + width/2, y + height/2);
        if (id != -1) return id;
        
        // Verificar las 4 esquinas
        id = getTeleportId(x, y);
        if (id != -1) return id;
        
        id = getTeleportId(x + width - 1, y);
        if (id != -1) return id;
        
        id = getTeleportId(x, y + height - 1);
        if (id != -1) return id;
        
        id = getTeleportId(x + width - 1, y + height - 1);
        if (id != -1) return id;
        
        return -1;
    }
    
    // 🔹 Encuentra la posición correspondiente del teleport en el otro mapa según el ID
    public Point findTeleportDestination(int teleportId) {
        if (collisionImage == null) return null;
        
        // Buscar el píxel rojo con el mismo ID en el mapa
        for (int y = 0; y < collisionImage.getHeight(); y++) {
            for (int x = 0; x < collisionImage.getWidth(); x++) {
                int rgb = collisionImage.getRGB(x, y);
                Color color = new Color(rgb, true);
                if (isRedPixel(color) && color.getRed() == teleportId) {
                    // Devolver la posición escalada
                    return new Point((int)(x * scaleFactor), (int)(y * scaleFactor));
                }
            }
        }
        
        return null; // No se encontró teleport con ese ID
    }
    
    // 🔹 Verifica si un color es rojo (teleport)
    private boolean isRedPixel(Color color) {
        // Un píxel es considerado rojo si:
        // - Red > 200
        // - Green < 100
        // - Blue < 100
        return color.getRed() > 200 && color.getGreen() < 100 && color.getBlue() < 100;
    }

    public boolean hasCollisionRect(int x, int y, int width, int height) {
        if (hasCollision(x, y)) return true;
        if (hasCollision(x + width - 1, y)) return true;
        if (hasCollision(x, y + height - 1)) return true;
        if (hasCollision(x + width - 1, y + height - 1)) return true;

        int step = 5;
        for (int i = x; i < x + width; i += step) {
            if (hasCollision(i, y)) return true;
            if (hasCollision(i, y + height - 1)) return true;
        }
        for (int j = y; j < y + height; j += step) {
            if (hasCollision(x, j)) return true;
            if (hasCollision(x + width - 1, j)) return true;
        }

        return false;
    }

    public boolean canMoveTo(double newX, double newY, int entityWidth, int entityHeight) {
        return !hasCollisionRect((int)newX, (int)newY, entityWidth, entityHeight);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isLoaded() { return collisionImage != null; }
    public BufferedImage getImage() { return collisionImage; }
}
