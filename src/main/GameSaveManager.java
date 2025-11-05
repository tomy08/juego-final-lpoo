package main;

import java.io.*;
import java.util.Properties;

import Levels.LevelPanel;
import entities.Item;
import entities.ItemStack;
import entities.NPC;
import entities.NPCManager;
import entities.Player;

/**
 * Maneja el guardado y carga de partidas.
 * Guarda todos los datos necesarios para continuar una partida.
 */
public class GameSaveManager {
    
    private static final String SAVE_FILE = "savedata.dat";
    
    /**
     * Guarda el estado completo del juego
     */
    
    public static boolean guardarPartida(GamePanel gamePanel, Player player) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(SAVE_FILE))) {
            
            // === 1. DATOS DEL GAMEPANEL ===
            dos.writeBoolean(gamePanel.martinBien);
            dos.writeBoolean(gamePanel.gennusoBien);
            dos.writeBoolean(gamePanel.taller);
            dos.writeBoolean(gamePanel.ascensorTaller);
            dos.writeInt(gamePanel.monedas);
            
            // Guardar stock de la tienda (3 items)
            int[] stock = gamePanel.getStockTienda();
            dos.writeInt(stock.length);
            for (int cantidad : stock) {
                dos.writeInt(cantidad);
            }
            
            // === 2. DATOS DEL JUGADOR ===
            dos.writeDouble(player.getX() / gamePanel.SCALE);
            dos.writeDouble(player.getY() / gamePanel.SCALE);
            dos.writeBoolean(gamePanel.enPlantaAlta);
            
            // === 3. INVENTARIO ===
            guardarInventario(dos, player);
            
            // === 4. VARIABLES ESTÁTICAS DEL LEVELPANEL ===
            dos.writeInt(LevelPanel.Max_vida);
            dos.writeDouble(LevelPanel.multiplicador_puntos);
            dos.writeInt(LevelPanel.plusSuma);
            
            // === 6. DATOS DEL NPCMANAGER ===
            guardarNPCManager(dos);
            
            System.out.println("Partida guardada exitosamente en " + SAVE_FILE);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error al guardar la partida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Carga el estado completo del juego
     */
    public static boolean cargarPartida(GamePanel gamePanel, Player player) {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists()) {
            System.out.println("No se encontró archivo de guardado.");
            return false;
        }
        
        try (DataInputStream dis = new DataInputStream(new FileInputStream(SAVE_FILE))) {
            
            // === 1. DATOS DEL GAMEPANEL ===
            gamePanel.martinBien = dis.readBoolean();
            gamePanel.gennusoBien = dis.readBoolean();
            gamePanel.taller = dis.readBoolean();
            gamePanel.ascensorTaller = dis.readBoolean();
            gamePanel.monedas = dis.readInt();
            
            // Cargar stock de la tienda
            int stockLength = dis.readInt();
            int[] stock = new int[stockLength];
            for (int i = 0; i < stockLength; i++) {
                stock[i] = dis.readInt();
            }
            gamePanel.setStockTienda(stock);
            
            // === 2. DATOS DEL JUGADOR ===
            double playerX = dis.readDouble();
            double playerY = dis.readDouble();
            player.setX(playerX * gamePanel.SCALE);
            player.setY(playerY * gamePanel.SCALE);
            gamePanel.enPlantaAlta = dis.readBoolean();
            
            // === 3. INVENTARIO ===
            cargarInventario(dis, player);
            
            // === 4. VARIABLES ESTÁTICAS DEL LEVELPANEL ===
            LevelPanel.Max_vida = dis.readInt();
            LevelPanel.multiplicador_puntos = dis.readDouble();
            LevelPanel.plusSuma = dis.readInt();
            
            // === 6. DATOS DEL NPCMANAGER ===
            cargarNPCManager(dis, gamePanel);
            
            System.out.println("Partida cargada exitosamente desde " + SAVE_FILE);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error al cargar la partida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void reiniciarPartida(GamePanel gamePanel, Player player) {
    	
        // === Reiniciar datos del GamePanel ===
        gamePanel.martinBien = false;
        gamePanel.gennusoBien = true;
        gamePanel.taller = false;
        gamePanel.ascensorTaller = false;
        gamePanel.monedas = 0;
        gamePanel.enPlantaAlta = true;

        // Reiniciar stock de tienda
        gamePanel.setStockTienda(new int[] {3, 3, 1});

        // === Reiniciar jugador ===
        player.setX(285 * gamePanel.SCALE);
        player.setY(55 * gamePanel.SCALE);
        for(int i = 0; i<player.inventory.getTotalSlots() ; i++) {
        	player.inventory.removeFromSlot(i, 4);
        }
        player.inventory.selectHotbar(0);
        gamePanel.CargarZona(0);

        // === Reiniciar LevelPanel ===
        LevelPanel.Max_vida = 50;
        LevelPanel.multiplicador_puntos = 1.0;
        LevelPanel.plusSuma = 0;

        // === Reiniciar NPCs ===
        NPCManager.clearAllNPCs();
    }

    
    /**
     * Verifica si existe un archivo de guardado
     */
    public static boolean existePartidaGuardada() {
        return new File(SAVE_FILE).exists();
    }
    
    /**
     * Elimina el archivo de guardado
     */
    public static boolean eliminarPartida() {
        File saveFile = new File(SAVE_FILE);
        if (saveFile.exists()) {
            return saveFile.delete();
        }
        return false;
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Guarda el inventario del jugador
     */
    private static void guardarInventario(DataOutputStream dos, Player player) throws IOException {
        int totalSlots = player.inventory.getTotalSlots();
        dos.writeInt(totalSlots);
        dos.writeInt(player.inventory.getColumns());
        dos.writeInt(player.inventory.getRows());
        dos.writeInt(player.inventory.getSelectedHotbar());
        
        int itemsGuardados = 0;
        // Guardar cada slot en su posición exacta
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = player.inventory.getSlot(i);
            if (stack == null || stack.isEmpty()) {
                dos.writeBoolean(false); // Slot vacío
            } else {
                dos.writeBoolean(true); // Slot con item
                Item item = stack.getItem();
                dos.writeUTF(item.getId());
                dos.writeUTF(item.getName());
                dos.writeInt(item.getMaxStack());
                dos.writeInt(stack.getAmount());
                // Guardar ruta de imagen si existe
                boolean hasImage = (item.getImage() != null);
                dos.writeBoolean(hasImage);
                itemsGuardados++;
            }
        }
        System.out.println("  → Inventario: " + itemsGuardados + " items guardados en " + totalSlots + " slots");
    }
    
    /**
     * Carga el inventario del jugador
     */
    private static void cargarInventario(DataInputStream dis, Player player) throws IOException {
        int totalSlots = dis.readInt();
        int columns = dis.readInt();
        int rows = dis.readInt();
        int selectedHotbar = dis.readInt();
        
        // Recrear inventario con las dimensiones guardadas
        player.inventory = new entities.Inventory(columns, rows);
        
        int itemsCargados = 0;
        // Cargar cada slot en su posición exacta
        for (int i = 0; i < totalSlots; i++) {
            boolean hasItem = dis.readBoolean();
            if (hasItem) {
                String itemId = dis.readUTF();
                String itemName = dis.readUTF();
                int maxStack = dis.readInt();
                int amount = dis.readInt();
                boolean hasImage = dis.readBoolean();
                
                // Recrear el item con su imagen
                Item item = GamePanel.createItemWithImage(itemId, itemName, maxStack);
                ItemStack stack = new ItemStack(item, amount);
                
                // Establecer el slot directamente en su posición
                player.inventory.setSlot(i, stack);
                itemsCargados++;
            }
        }
        
        System.out.println("  → Inventario: " + itemsCargados + " items cargados en " + totalSlots + " slots");
        
        // Restaurar selección del hotbar
        player.inventory.selectHotbar(selectedHotbar);
    }
    
    /**
     * Guarda el estado de todos los NPCs
     */
    private static void guardarNPCManager(DataOutputStream dos) throws IOException {
        java.util.ArrayList<NPC> npcs = NPCManager.getAllNPCs();
        dos.writeInt(npcs.size());
        
        for (NPC npc : npcs) {
            dos.writeUTF(npc.Tipo);
            dos.writeDouble(npc.x);
            dos.writeDouble(npc.y);
            dos.writeInt(npc.size);
            dos.writeInt(npc.line);
            dos.writeInt(npc.Trigger);
            dos.writeBoolean(npc.interactive);
        }
    }
    
    /**
     * Carga el estado de todos los NPCs
     */
    private static void cargarNPCManager(DataInputStream dis, GamePanel panel) throws IOException {
        int npcCount = dis.readInt();
        
        // Limpiar NPCs existentes
        NPCManager.clearAllNPCs();
        
        for (int i = 0; i < npcCount; i++) {
            String tipo = dis.readUTF();
            double x = dis.readDouble();
            double y = dis.readDouble();
            int size = dis.readInt();
            int line = dis.readInt();
            int trigger = dis.readInt();
            boolean interactive = dis.readBoolean();
            
            // Recrear el NPC
            NPC npc = NPCManager.getOrCreateNPC(tipo, x, y, size, panel);
            npc.line = line;
            npc.Trigger = trigger;
            npc.interactive = interactive;
        }
    }
}
