package superhb.arcademod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import superhb.arcademod.client.tileentity.*;

public class GuiHandler implements IGuiHandler {
	private int SNAKE = 0, TETROMINOES = 1, PACMAN = 2, PONG = 3, KONG = 4;
	private int PRIZE = -1, PUSHER = -2;
	
	@Override
	public Object getServerGuiElement (int id, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
	
	@Override
	public Object getClientGuiElement (int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == SNAKE) return new GuiSnake(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), player);
		else if (id == TETROMINOES) return new GuiTetrominoes(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), player);
		else if (id == PACMAN) return new GuiPacMan(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), new BlockPos(x, y, z), player);
		else if (id == PRIZE) return new GuiPrize((TileEntityPrize)world.getTileEntity(new BlockPos(x, y, z)));
		else if (id == PUSHER) return new GuiPusher(world, x, y, z, player);
		else if (id == PONG) return new GuiPong(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), new BlockPos(x, y, z), player);
		else if (id == KONG) return new GuiKong(world, (TileEntityArcade)world.getTileEntity(new BlockPos(x, y, z)), player);
		return null;
	}
}
