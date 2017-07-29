package superhb.arcademod.util;

import net.minecraft.item.Item;

public class PrizeList {
    private Item item;
    private int price;

    public PrizeList (Item item, int price) {
        this.item = item;
        this.price = price;
    }

    public Item getItem () {
        return item;
    }

    public int getPrice () {
        return price;
    }
}
