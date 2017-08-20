package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import superhb.arcademod.client.entity.EntityCamera;

import java.io.IOException;

public class GuiPusher extends GuiScreen {
    private final World world;
    private final double x, y, z;
    private final EntityPlayer player;

    public GuiPusher (World world, double x, double y, double z, EntityPlayer player) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = player;
    }

    @Override
    public void initGui () {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.mc.setRenderViewEntity(new EntityCamera(world, x, y, z));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // Esc
            this.mc.setRenderViewEntity(player);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame () {
        return false;
    }
}
