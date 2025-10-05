package Levels;

import java.io.*;
import java.util.*;

import entities.arrow;
import main.GW;
import main.GameWindow;

public class ChartLoader {
    
    public static List<arrow> loadChart(File file, GameWindow gw, boolean isEnemy, double speed) {
        List<arrow> arrows = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+"); // separa por espacios
                
                // recorremos cada "columna"
                for (int col = 0; col < tokens.length; col++) {
                    if (tokens[col].equals("1")) {
                        int x, y;
                        if (col < 4) {
                            // zona enemigo
                            x = GW.SX(225) + col * GW.SX(125);
                        } else if (col > 4) {
                            // zona jugador
                            x = GW.SX(1250) + (col - 5) * GW.SX(125);
                        } else {
                            continue;
                        }
                        
                        // cada fila más arriba en el tiempo
                        y = gw.scaleY(row * 80 + 1200); 
                        
                        arrows.add(new arrow(x, y, speed));
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
