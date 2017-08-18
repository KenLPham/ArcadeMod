package superhb.arcademod.util;

import net.minecraft.item.ItemStack;

public class PrizeList {
    private ItemStack stack;
    private int price;

    public PrizeList (ItemStack stack, int price) {
        this.stack = stack;
        this.price = price;
    }

    public ItemStack getStack () {
        return stack;
    }

    public int getPrice () {
        return price;
    }
}
