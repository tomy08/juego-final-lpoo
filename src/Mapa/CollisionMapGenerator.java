package Mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Herramienta para crear un mapa de colisiones de ejemplo
 * Ejecuta esta clase para generar una imagen de prueba
 */
public class CollisionMapGenerator {
    
    public static void main(String[] args) {
        int width = 1920;  // Ancho del mapa
        int height = 1080; // Alto del mapa
        
        // Crear imagen en blanco y negro
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Fondo blanco (sin colisión)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // Dibujar áreas de colisión en negro
        g2d.setColor(Color.BLACK);
        
        // Bordes del mapa (paredes)
        int borderThickness = 50;
        g2d.fillRect(0, 0, width, borderThickness);              // Borde superior
        g2d.fillRect(0, height - borderThickness, width, borderThickness); // Borde inferior
        g2d.fillRect(0, 0, borderThickness, height);             // Borde izquierdo
        g2d.fillRect(width - borderThickness, 0, borderThickness, height);  // Borde derecho
        
        // Algunas paredes interiores de ejemplo
        g2d.fillRect(300, 200, 100, 400);   // Pared vertical
        g2d.fillRect(600, 300, 400, 100);   // Pared horizontal
        g2d.fillRect(1200, 400, 150, 300);  // Otra pared vertical
        
        // Obstáculos más pequeños
        g2d.fillRect(400, 700, 200, 200);   // Cuadrado
        g2d.fillRect(900, 150, 150, 150);   // Otro cuadrado
        
        // Crear un "laberinto" simple
        g2d.fillRect(1400, 100, 50, 600);   // Pared larga
        g2d.fillRect(1400, 600, 300, 50);   // Pared horizontal
        
        // Dibujar zonas de teleport con diferentes IDs (según el valor del canal rojo)
        // Teleport ID 255 (rojo más intenso) - en el centro
        g2d.setColor(new Color(255, 0, 0));
        g2d.fillRect(960 - 40, 540 - 40, 80, 80);
        
        // Teleport ID 220 (rojo medio) - arriba a la izquierda
        g2d.setColor(new Color(220, 0, 0));
        g2d.fillRect(200, 200, 80, 80);
        
        // Teleport ID 200 (rojo más tenue) - abajo a la derecha
        g2d.setColor(new Color(200, 0, 0));
        g2d.fillRect(1600, 800, 80, 80);
        
        g2d.dispose();
        
        // Guardar la imagen
        try {
            File outputFile = new File("resources/collision_map.png");
            outputFile.getParentFile().mkdirs(); // Crear directorio si no existe
            ImageIO.write(image, "png", outputFile);
            System.out.println("✓ Mapa de colisiones generado exitosamente en: " + outputFile.getAbsolutePath());
            System.out.println("  Tamaño: " + width + "x" + height + " pixels");
            System.out.println("  Negro = Colisión, Blanco = Libre");
            System.out.println("  Rojo = Teleport (El valor del canal R define el ID del teleport)");
            System.out.println("    - RGB(255,0,0) = Teleport ID 255");
            System.out.println("    - RGB(220,0,0) = Teleport ID 220");
            System.out.println("    - RGB(200,0,0) = Teleport ID 200");
        } catch (Exception e) {
            System.err.println("Error al guardar la imagen:");
            e.printStackTrace();
        }
    }
}
