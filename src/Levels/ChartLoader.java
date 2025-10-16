package Levels;

import java.io.*;
import java.util.*;
import entities.arrow;
import main.GW;
import main.GameWindow;

public class ChartLoader {

    public static List<arrow> loadChart(File file, GameWindow gw, boolean isEnemy, double arrowSpeed, double bpm) {
        List<arrow> arrows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;

            final int HIT_Y_SCALED = (int)GW.SY(125); 
            final double BEAT_DIVISION = 4.0;

            double msPerBeat = 60000.0 / bpm;
            double msPerRow = msPerBeat / BEAT_DIVISION; 

            double pixelsPerMs = GW.DSY(0.15) * arrowSpeed;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) continue;

                double hitTimeMs = row * msPerRow;

                if(line.equals("3")) { // fin del nivel
                    int x = GW.SX(10);
                    arrows.add(new arrow(x, HIT_Y_SCALED, pixelsPerMs, true, false, hitTimeMs));
                    break;
                }

                String[] tokens = line.split("\\s+");
                for(int col = 0; col < tokens.length; col++) {
                    if(tokens[col].equals("1") || tokens[col].equals("2")) {
                        int x = GW.SX(685) + col * GW.SX(150);
                        boolean isLong = tokens[col].equals("2");

                        arrows.add(new arrow(x, HIT_Y_SCALED, pixelsPerMs, false, isLong, hitTimeMs));
                    }
                }
                row++;
            }

        } catch(IOException e) {
            e.printStackTrace();
        }

        return arrows;
    }
}
