package entities;

/**
 * Representa una pila de items (item + cantidad)
 */
public class ItemStack {
    private Item item;
    private int amount;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.amount = Math.max(0, amount);
    }

    public Item getItem() { return item; }
    public int getAmount() { return amount; }

    public void setAmount(int amount) { this.amount = Math.max(0, amount); }

    public int getMaxStack() { return item != null ? item.getMaxStack() : 0; }

    public boolean isEmpty() { return item == null || amount <= 0; }

    /** Intenta añadir n elementos a la pila. Devuelve la cantidad que no entró (overflow). */
    public int add(int n) {
        if (item == null) return n;
        int space = getMaxStack() - amount;
        int toAdd = Math.min(space, n);
        amount += toAdd;
        return n - toAdd;
    }

    /** Intenta extraer n elementos de la pila. Devuelve la cantidad realmente extraída. */
    public int remove(int n) {
        int toRemove = Math.min(amount, n);
        amount -= toRemove;
        if (amount <= 0) {
            item = null;
            amount = 0;
        }
        return toRemove;
    }
}
