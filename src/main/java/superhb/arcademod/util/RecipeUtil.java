package superhb.arcademod.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;
import superhb.arcademod.client.ArcadeBlocks;
import superhb.arcademod.client.ArcadeItems;

public class RecipeUtil {
    public static void registerRecipes () {
        GameRegistry.addRecipe(new ItemStack(ArcadeItems.coin, 1), new Object[] { "gg", "gg", 'g', Items.GOLD_NUGGET });
        GameRegistry.addRecipe(new ItemStack(Items.GOLD_NUGGET, 1), new Object[] { "tt", "tt", 't', ArcadeItems.ticket}); // Temporary Recipe

        NBTTagCompound snake = new NBTTagCompound();
        snake.setInteger("Game", 0);
        ItemStack snakeStack = new ItemStack(ArcadeBlocks.arcadeMachine);
        snakeStack.setTagCompound(snake);
        GameRegistry.addRecipe(snakeStack, new Object[] { "bgb", "wbw", "brb", 'b', new ItemStack(Blocks.WOOL, 1, 15), 'g', Blocks.GLASS_PANE, 'w', new ItemStack(Blocks.WOOL, 1, 0), 'r', Items.REDSTONE });

        NBTTagCompound tetrominoes = new NBTTagCompound();
        tetrominoes.setInteger("Game", 1);
        ItemStack tetrominoesStack = new ItemStack(ArcadeBlocks.arcadeMachine);
        tetrominoesStack.setTagCompound(tetrominoes);
        GameRegistry.addRecipe(tetrominoesStack, new Object[] { "bgb", "bbb", "brb", 'b', new ItemStack(Blocks.WOOL, 1, 11), 'g', Blocks.GLASS_PANE, 'r', Items.REDSTONE});
    }
}
