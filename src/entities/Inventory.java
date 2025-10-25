package entities;

import java.awt.Graphics2D;
import java.awt.Image;

import main.GW;
import main.GameWindow;

import java.awt.Color;
import java.awt.Font;

/**
 * Inventario estilo Minecraft: slots, hotbar y stacks.
 */
public class Inventory {
    private ItemStack[] slots; // total slots (hotbar + grid)
    private int columns;
    private int rows;
    private int selectedHotbar = 0;

    public Inventory(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.slots = new ItemStack[columns * rows];
        for (int i = 0; i < slots.length; i++) slots[i] = new ItemStack(null, 0);
    }

    public int getTotalSlots() { return slots.length; }
    public int getColumns() { return columns; }
    public int getRows() { return rows; }
    public int getSelectedHotbar() { return selectedHotbar; }

    public ItemStack getSlot(int idx) {
        if (idx < 0 || idx >= slots.length) return null;
        return slots[idx];
    }

    /**
     * Intenta agregar una cantidad de un item al inventario. Devuelve la cantidad que no se pudo insertar.
     */
    public int addItem(Item item, int amount) {
        if (item == null || amount <= 0) return amount;

        // Primero intentar stacks existentes del mismo item
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = slots[i];
            if (!s.isEmpty() && s.getItem().equals(item)) {
                amount = s.add(amount);
                if (amount <= 0) return 0;
            }
        }

        // Después llenar slots vacíos
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = slots[i];
            if (s.isEmpty()) {
                int toPut = Math.min(item.getMaxStack(), amount);
                slots[i] = new ItemStack(item, toPut);
                amount -= toPut;
                if (amount <= 0) return 0;
            }
        }

        return amount; // lo que no cupo
    }

    /**
     * Remueve items de un slot específico. Devuelve la cantidad removida.
     */
    public int removeFromSlot(int slotIndex, int amount) {
        if (slotIndex < 0 || slotIndex >= slots.length || amount <= 0) return 0;
        ItemStack s = slots[slotIndex];
        if (s.isEmpty()) return 0;
        int removed = s.remove(amount);
        if (s.isEmpty()) slots[slotIndex] = new ItemStack(null, 0);
        return removed;
    }

    /**
     * Verifica si el inventario tiene al menos 'amount' unidades de un item con el id dado.
     */
    public boolean hasItem(String itemId, int amount) {
        int total = 0;
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = slots[i];
            if (!s.isEmpty() && s.getItem().getId().equals(itemId)) {
                total += s.getAmount();
                if (total >= amount) return true;
            }
        }
        return false;
    }

    /**
     * Cuenta cuántas unidades de un item específico hay en el inventario.
     */
    public int countItem(String itemId) {
        int total = 0;
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = slots[i];
            if (!s.isEmpty() && s.getItem().getId().equals(itemId)) {
                total += s.getAmount();
            }
        }
        return total;
    }

    /**
     * Remueve una cantidad específica de un item del inventario. Devuelve la cantidad realmente removida.
     */
    public int removeItem(String itemId, int amount) {
        int toRemove = amount;
        int removed = 0;
        for (int i = 0; i < slots.length && toRemove > 0; i++) {
            ItemStack s = slots[i];
            if (!s.isEmpty() && s.getItem().getId().equals(itemId)) {
                int canRemove = Math.min(s.getAmount(), toRemove);
                s.remove(canRemove);
                if (s.isEmpty()) slots[i] = new ItemStack(null, 0);
                removed += canRemove;
                toRemove -= canRemove;
            }
        }
        return removed;
    }

    public void drawFullInventory(Graphics2D g2d, int panelWidth, int panelHeight) {
        int slotSize = 64;
        int padding = 8;
        int gridW = columns * (slotSize + padding);
        int gridH = rows * (slotSize + padding);
        int startX = (panelWidth - gridW) / 2;
        int startY = (panelHeight - gridH) / 2;

        g2d.setColor(new Color(0,0,0,180));
        g2d.fillRect(startX - 20, startY - 40, gridW + 40, gridH + 80);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                int idx = r * columns + c;
                int x = startX + c * (slotSize + padding);
                int y = startY + r * (slotSize + padding);
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(x, y, slotSize, slotSize);
                g2d.setColor(Color.WHITE);
                g2d.drawRect(x, y, slotSize, slotSize);

                ItemStack s = slots[idx];
                if (!s.isEmpty()) {
                    Image img = s.getItem().getImage();
                    if (img != null) g2d.drawImage(img, x+6, y+6, slotSize-12, slotSize-12, null);
                    g2d.setFont(GameWindow.Pixelart.deriveFont((GW.SF(30f))));
                    if(s.getAmount() > 1) {
                    	g2d.drawString(String.valueOf(s.getAmount()), x+6, y+slotSize-10);
                    }
                }
            }
        }
    }
}
