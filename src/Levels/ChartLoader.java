package Levels;

import java.io.*;
import java.util.*;

import entities.arrow;
import main.GW;
import main.GameWindow;

public class ChartLoader {

    public static List<arrow> loadChart(File file, GameWindow gw, boolean isEnemy, double arrowSpeed, int bpm) {
        List<arrow> arrows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;
            
            // --- CONSTANTES DE JUEGO Y MÚSICA ---
            
            // 1. Posición Y de la zona de golpeo (escalada).
            final int HIT_Y_SCALED = GW.SY(125); 
            final double BASE_DISTANCE_PER_BEAT = GW.SY(38);
            final double BASE_BPM = 120.0;
            
            // 4. Factor de espaciado vertical por fila (row).
            // Asumiendo que cada 'row' representa 1/4 de beat (1/16 de nota).
            final double BEAT_DIVISION = 4.0; 
            
            // --- CÁLCULO DE ESPACIADO ---

            // 1. Distancia que representa un 1/4 de nota (un beat completo) ajustado por BPM.
            // Si BPM > 120, esta distancia será menor (notas más juntas).
            double distancePerFullBeat = BASE_DISTANCE_PER_BEAT * (BASE_BPM / bpm);
            
            // 2. Distancia que representa una sola ROW (1/16 de nota), escalada por la velocidad.
            // (La distancia base de 1/16) * (El multiplicador de velocidad)
            double distancePerRow = (distancePerFullBeat / BEAT_DIVISION) * arrowSpeed;


            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) continue;

                // Distancia total de scroll acumulada hasta esta 'row'
                double totalScrollDistance = row * distancePerRow;


                if (line.equals("3")) {
                    int x = GW.SX(10);
                    // La posición Y inicial es HIT_Y_SCALED + la distancia total acumulada.
                    int y = HIT_Y_SCALED + (int)totalScrollDistance; 
                    arrows.add(new arrow(x, y, arrowSpeed, true, false)); 
                    break;
                }

                String[] tokens = line.split("\\s+");
                for (int col = 0; col < tokens.length; col++) {
                    if (tokens[col].equals("1") || tokens[col].equals("2")) {
                        int x, y;
                        x = GW.SX(685) + col * GW.SX(150);
                        
                        // La Y inicial es: Posición de Golpeo + Distancia de Scroll Necesaria.
                        y = HIT_Y_SCALED + (int)totalScrollDistance; 

                        boolean isLong = tokens[col].equals("2");
                        arrows.add(new arrow(x, y, arrowSpeed, false, isLong)); 
                    }
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrows;
    }
}