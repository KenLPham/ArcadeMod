package superhb.arcademod.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import superhb.arcademod.tileentity.TileEntityArcade;

public class GuiHandler implements IGuiHandler {
    private int SNAKE = 0, TETRIS = 1;

    // Not needed as Arcade Machines don't have containers
    @Override
    public Object getServerGuiElement (int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement (int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);

        //if (tile instanceof TileEntityArcade) {
        TileEntityArcade arcade = (TileEntityArcade)tile;

        if (id == SNAKE) return new GuiSnake(world, arcade, player);
        else if (id == TETRIS) return new GuiTetris(world, arcade, player);
        //}
        return null;
    }
}
