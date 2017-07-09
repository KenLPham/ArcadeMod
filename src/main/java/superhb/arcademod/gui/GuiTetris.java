package superhb.arcademod.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import net.minecraft.world.World;
import superhb.arcademod.tileentity.TileEntityArcade;

public class GuiTetris extends GuiArcade {
    // 10x18 Blocks
    // Each shape placed is 17 pts
    // Final shape that makes row is worth 58 pts
    // Board Size: 130x234
    // Next Piece Area Size: 50x28
    // Side Text: Level (1-10), Rows, Score

    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/tetris.png");

    private static final int GUI_X = 210;
    private static final int GUI_Y = 254;
    private static final int PLAY_BLOCK = 13;
    private static final int PREVIEW_BLOCK = 11;

    public GuiTetris (World world, TileEntityArcade tileEntity, EntityPlayer player) {
        super(world, tileEntity, player);
        setGuiSize(GUI_X, GUI_Y);
        setTexture(texture);
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
