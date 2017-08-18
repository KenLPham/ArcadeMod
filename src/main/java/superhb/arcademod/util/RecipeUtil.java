package superhb.arcademod.util;

import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;
import superhb.arcademod.client.*;

public class RecipeUtil {
    public static void registerRecipes () {
        GameRegistry.addRecipe(new ItemStack(ArcadeItems.coin, 1), "gg", "gg", 'g', Items.GOLD_NUGGET);
        GameRegistry.addRecipe(new ItemStack(ArcadeBlocks.prizeBox, 1), "iii", "igi", "iri", 'i', Items.IRON_INGOT, 'g', Blocks.GLASS_PANE, 'r', Items.REDSTONE);

        registerRecipeWithNBT(new ItemStack(ArcadeBlocks.arcadeMachine), "Game", 0, "bgb", "wbw", "brb", 'b', new ItemStack(Blocks.WOOL, 1, 15), 'g', Blocks.GLASS_PANE, 'w', new ItemStack(Blocks.WOOL, 1, 0), 'r', Items.REDSTONE);
        registerRecipeWithNBT(new ItemStack(ArcadeBlocks.arcadeMachine), "Game", 1, "bgb", "bbb", "brb", 'b', new ItemStack(Blocks.WOOL, 1, 11), 'g', Blocks.GLASS_PANE, 'r', Items.REDSTONE);
        registerRecipeWithNBT(new ItemStack(ArcadeBlocks.arcadeMachine), "Game", 2, "ygy", "yby", "yry", 'y', new ItemStack(Blocks.WOOL, 1, 4), 'g', Blocks.GLASS_PANE, 'r', Items.REDSTONE);
    }

    private static void registerRecipeWithNBT (ItemStack output, String key, int value, Object... recipe) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(key, value);
        output.setTagCompound(compound);
        GameRegistry.addRecipe(output, recipe);
    }
}
