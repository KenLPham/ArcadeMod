package superhb.arcademod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import superhb.arcademod.Arcade;
import superhb.arcademod.tileentity.TileEntityArcade;
import superhb.arcademod.tileentity.TileEntityPrize;

public class GuiHandler implements IGuiHandler {
    private int SNAKE = 0, TETROMINOES = 1, PRIZE = -1;

    @Override
    public Object getServerGuiElement (int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement (int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == SNAKE) return new GuiSnake(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), player);
        else if (id == TETROMINOES) return new GuiTetrominoes(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), player);
        else if (id == PRIZE) return new GuiPrize((TileEntityPrize)world.getTileEntity(new BlockPos(x, y, z)));
        return null;
    }
}
