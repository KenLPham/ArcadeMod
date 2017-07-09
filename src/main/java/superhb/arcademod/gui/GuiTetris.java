package superhb.arcademod.gui;

import net.minecraft.entity.player.EntityPlayer;
import superhb.arcademod.api.gui.GuiArcade;
import net.minecraft.world.World;
import superhb.arcademod.tileentity.TileEntityArcade;

public class GuiTetris extends GuiArcade {
    // 10x18 Blocks

    public GuiTetris (World world, TileEntityArcade tileEntity, EntityPlayer player) {
        super(world, tileEntity, player);
    }
}
