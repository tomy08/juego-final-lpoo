package entities;

import java.awt.Image;

/**
 * Representa un tipo de item. Contiene id, nombre y (opcional) imagen.
 */
public class Item {
    private final String id;
    private final String name;
    private final Image image; // puede ser null
    private final int maxStack;

    public Item(String id, String name, Image image, int maxStack) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.maxStack = maxStack;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Image getImage() { return image; }
    public int getMaxStack() { return maxStack; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    public boolean isConsumable() {
        String itemId = getId();
        return itemId.equals("pancho") || itemId.equals("chocolate_dubai") || itemId.equals("jugo_placer");
    }

}
