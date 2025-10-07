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

                line = line.trim();
                if (line.isEmpty()) continue;

                // marcador de finalización de un nivel
                if (line.equals("3")) {
                    int x = GW.SX(1000);
                    int y = gw.scaleY(row * 80 + 1200);
                    arrows.add(new arrow(x, y, speed, true)); 
                    break;
                }


                String[] tokens = line.split("\\s+");
                for (int col = 0; col < tokens.length; col++) {
                    if (tokens[col].equals("1")) {
                        int x, y;
                        if (col < 4) {
                            x = GW.SX(225) + col * GW.SX(125);
                        } else if (col > 4) {
                            x = GW.SX(1250) + (col - 5) * GW.SX(125);
                        } else {
                            continue;
                        }
                        y = gw.scaleY(row * 80 + 1200);
                        arrows.add(new arrow(x, y, speed, false));
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
