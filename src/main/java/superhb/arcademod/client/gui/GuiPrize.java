package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArrow;
import superhb.arcademod.tileentity.TileEntityPrize;

import java.awt.*;
import java.io.IOException;

// TODO: Enlarge GUI
public class GuiPrize extends GuiScreen {
    private GuiButton prizeNext, prizePrev, amountUp, amountDown, buy;

    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/prize_box.png");

    private static final int GUI_X = 124;
    private static final int GUI_Y = 74;
    private int guiLeft = 0, guiTop = 0;
    private int amount = 1, curPrize = 0;

    private TileEntityPrize tile;

    public GuiPrize (TileEntityPrize tile) {
        this.tile = tile;
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect((width / 2) - (GUI_X / 2), (height / 2) - (GUI_Y / 2), 0, 0, GUI_X, GUI_Y);

        super.drawScreen(mouseX, mouseY, partialTicks);

        String name = tile.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(name, guiLeft, guiTop, Color.darkGray.getRGB());

        // Amount
        fontRendererObj.drawString(String.format("%d", amount), (guiLeft + 91) - (fontRendererObj.getStringWidth(String.format("%d", amount)) / 2), (guiTop + 34), Color.white.getRGB());

        // Cost
        fontRendererObj.drawString("Cost: " + (Arcade.prizeList[curPrize].getPrice() * amount), (guiLeft + 70), (guiTop + 63), Color.darkGray.getRGB());

        // TODO: Item colors are fine, blocks are darker
        // Draw Prizes
        itemRender.zLevel = 100.0F;
        GlStateManager.enableLighting();
        drawRect((guiLeft + 31), (guiTop + 29), (guiLeft + 31 + 16), (guiTop + 29 + 16), -2130706433);
        GlStateManager.enableDepth();
        itemRender.renderItemAndEffectIntoGUI(Arcade.prizeList[curPrize].getStack(), (guiLeft + 31), (guiTop + 29));
        // TODO: Hover Overlay
        //itemRender.renderItemOverlayIntoGUI(fontRendererObj, new ItemStack(ArcadeBlocks.plushie), guiLeft, guiTop, "Item");
    }

    @Override
    public void initGui () {
        super.initGui();

        guiLeft = (width / 2) - (GUI_X / 2);
        guiTop = (height / 2) - (GUI_Y / 2);

        this.buttonList.add(prizeNext = new GuiArrow(0, (guiLeft + 50), (guiTop + 32), 14, 22, 2));
        this.buttonList.add(prizePrev = new GuiArrow(1, (guiLeft + 20), (guiTop + 32), 14, 22, 3)); // TODO: Why hitbox is bigger than texture
        this.buttonList.add(amountUp = new GuiArrow(2, (guiLeft + 85), (guiTop + 19), 11, 7, 5));
        this.buttonList.add(amountDown = new GuiArrow(3, (guiLeft + 85), (guiTop + 48), 11, 7, 4));
        this.buttonList.add(buy = new GuiButton(4, (guiLeft + 30), (guiTop + GUI_Y - 25), 30, 20, "Buy"));
    }

    @Override
    protected void actionPerformed (GuiButton button) throws IOException {
        if (button == prizeNext) {
            if (curPrize != (Arcade.prizeList.length - 1)) curPrize++;
        } else if (button == prizePrev) {
            if (curPrize != 0) curPrize--;
        } else if (button == amountUp) {
            if (amount != 64) amount++;
        } else if (button == amountDown) {
            if (amount != 1) amount--;
        } else if (button == buy) {
            // TODO: Send Packet
        }
    }

    @Override
    public boolean doesGuiPauseGame ()
    {
        return false;
    }
}
