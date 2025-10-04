package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CollisionMap {
    private BufferedImage collisionImage;
    private int width;
    private int height;
    
    /**
     * Constructor que carga la imagen de colisiones
     * @param imagePath Ruta a la imagen de colisiones (negro = colisión, blanco = libre)
     */
    public CollisionMap(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("⚠ ADVERTENCIA: No se encontró el mapa de colisiones: " + imagePath);
                System.err.println("  El sistema de colisiones estará DESACTIVADO.");
                System.err.println("  Crea una imagen PNG en blanco y negro y colócala en: " + imagePath);
                collisionImage = null;
                width = 0;
                height = 0;
                return;
            }
            
            collisionImage = ImageIO.read(imageFile);
            if (collisionImage == null) {
                System.err.println("⚠ ADVERTENCIA: No se pudo leer la imagen: " + imagePath);
                System.err.println("  Asegúrate de que sea un archivo PNG válido.");
                width = 0;
                height = 0;
                return;
            }
            
            width = collisionImage.getWidth();
            height = collisionImage.getHeight();
            System.out.println("✓ Mapa de colisiones cargado: " + width + "x" + height);
        } catch (IOException e) {
            System.err.println("⚠ Error al cargar el mapa de colisiones: " + imagePath);
            e.printStackTrace();
            collisionImage = null;
            width = 0;
            height = 0;
        }
    }
    
    /**
     * Verifica si hay colisión en un punto específico
     * @param x Coordenada X
     * @param y Coordenada Y
     * @return true si hay colisión (pixel negro), false si está libre (pixel blanco)
     */
    public boolean hasCollision(int x, int y) {
        // Si no hay mapa cargado, no hay colisiones
        if (collisionImage == null) {
            return false;
        }
        
        // Verificar límites
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true; // Fuera de límites = colisión
        }
        
        // Obtener el color del pixel
        int rgb = collisionImage.getRGB(x, y);
        Color color = new Color(rgb);
        
        // Si el pixel es oscuro (cercano a negro), hay colisión
        // Usamos un umbral para considerar grises oscuros como colisión
        int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return brightness < 128; // Menor a 128 = oscuro = colisión
    }
    
    /**
     * Verifica si un rectángulo colisiona con el mapa
     * @param x Coordenada X del rectángulo
     * @param y Coordenada Y del rectángulo
     * @param width Ancho del rectángulo
     * @param height Alto del rectángulo
     * @return true si hay colisión en algún punto del rectángulo
     */
    public boolean hasCollisionRect(int x, int y, int width, int height) {
        // Verificar las cuatro esquinas del rectángulo
        if (hasCollision(x, y)) return true;                    // Esquina superior izquierda
        if (hasCollision(x + width - 1, y)) return true;        // Esquina superior derecha
        if (hasCollision(x, y + height - 1)) return true;       // Esquina inferior izquierda
        if (hasCollision(x + width - 1, y + height - 1)) return true; // Esquina inferior derecha
        
        // Verificar puntos intermedios en los bordes (para objetos grandes)
        int step = 5; // Verificar cada 5 pixels
        
        // Borde superior e inferior
        for (int i = x; i < x + width; i += step) {
            if (hasCollision(i, y)) return true;
            if (hasCollision(i, y + height - 1)) return true;
        }
        
        // Borde izquierdo y derecho
        for (int j = y; j < y + height; j += step) {
            if (hasCollision(x, j)) return true;
            if (hasCollision(x + width - 1, j)) return true;
        }
        
        return false;
    }
    
    /**
     * Verifica si una nueva posición es válida para moverse
     * @param newX Nueva coordenada X
     * @param newY Nueva coordenada Y
     * @param entityWidth Ancho de la entidad
     * @param entityHeight Alto de la entidad
     * @return true si la posición es válida (sin colisión)
     */
    public boolean canMoveTo(double newX, double newY, int entityWidth, int entityHeight) {
        return !hasCollisionRect((int)newX, (int)newY, entityWidth, entityHeight);
    }
    
    /**
     * Obtiene el ancho del mapa de colisiones
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Obtiene el alto del mapa de colisiones
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Verifica si el mapa se cargó correctamente
     */
    public boolean isLoaded() {
        return collisionImage != null;
    }
    
    /**
     * Obtiene la imagen del mapa de colisiones
     * @return BufferedImage con el mapa de colisiones, o null si no está cargado
     */
    public BufferedImage getImage() {
        return collisionImage;
    }
}
