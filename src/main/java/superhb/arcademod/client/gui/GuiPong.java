package superhb.arcademod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.tileentity.TileEntityArcade;

import javax.annotation.Nullable;

public class GuiPong extends GuiArcade {
	public GuiPong (World world, TileEntityArcade tileEntity, @Nullable BlockPos pos, EntityPlayer player) {
		super(world, tileEntity, pos, player);
	}
}
