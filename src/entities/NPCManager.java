package entities;

import java.util.ArrayList;

import main.GamePanel;

public class NPCManager {

    private static ArrayList<NPC> npcs = new ArrayList<>();

    public static NPC getOrCreateNPC(String tipo, double x, double y, int size, GamePanel panel) {
        for (NPC npc : npcs) {
            if (npc.Tipo.equals(tipo)) {
                return npc;
            }
        }
        NPC nuevo = new NPC(x, y, size, tipo, panel);
        npcs.add(nuevo);
        return nuevo;
    }

    public static NPC getNPCByTipo(String tipo) {
        for (NPC npc : npcs) {
            if (npc.Tipo.equals(tipo)) {
                return npc;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los NPCs (para sistema de guardado)
     */
    public static ArrayList<NPC> getAllNPCs() {
        return npcs;
    }
    
    /**
     * Limpia todos los NPCs (para sistema de carga)
     */
    public static void clearAllNPCs() {
        npcs.clear();
    }
}
