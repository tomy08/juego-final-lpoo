package Mapa;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CollisionMap {
    private BufferedImage collisionImage;
    private int width;
    private int height;
    private double scaleFactor = 35.0; // Escala visual, no afecta el mapa real

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
        int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return brightness < 128;
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
