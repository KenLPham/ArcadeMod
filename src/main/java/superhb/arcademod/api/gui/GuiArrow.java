package superhb.arcademod.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import superhb.arcademod.Reference;

public class GuiArrow extends GuiButton {
    /* Types
        0 = Large Right
        1 = Large Left
        2 = Small Right
        3 = Small Left
        4 = Small Down
        5 = Small Up
     */
    private int type = 0;
    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/button_arrows.png");

    private static final int LARGE_X = 14;
    private static final int LARGE_Y = 22;

    private static final int SMALL_HORIZONTAL_X = 7;
    private static final int SMALL_HORIZONTAL_Y = 11;

    private static final int SMALL_VERTICAL_X = 11;
    private static final int SMALL_VERTICAL_Y = 7;

    public GuiArrow (int id, int x, int y, int type) {
        super(id, x, y, "");
        this.type = type;
    }

    public GuiArrow (int id, int x, int y, int width, int height, int type) {
        super(id, x, y, width, height, "");
        this.type = type;
    }

    @Override
    public void drawButton (Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = getHoverState(hovered); //Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            switch (type) {
                case 0: // Large Right
                    if (i == 1) drawTexturedModalRect(x, y, 0, 0, LARGE_X, LARGE_Y); // Not Hovering
                    else if (i == 2) drawTexturedModalRect(x, y, 0, LARGE_Y, LARGE_X, LARGE_Y); // Hovering
                    break;
                case 1: // Large Left
                    if (i == 1) drawTexturedModalRect(x, y, LARGE_X, 0, LARGE_X, LARGE_Y); // Not Hovering
                    else if (i == 2) drawTexturedModalRect(x, y, LARGE_X, LARGE_Y, LARGE_X, LARGE_Y); // Hovering
                    break;
                case 2: // Small Right
                    if (i == 1) drawTexturedModalRect(x, y, (LARGE_X * 2), 0, SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y); // Not Hovering
                    else if (i == 2) drawTexturedModalRect(x, y, (LARGE_X * 2), SMALL_HORIZONTAL_Y, SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y); // Hovering
                    break;
                case 3: // Small Left
                    if (i == 1) drawTexturedModalRect(x, y, (LARGE_X * 2) + SMALL_HORIZONTAL_X, 0, SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y); // Not Hovering
                    else if (i == 2) drawTexturedModalRect(x, y, (LARGE_X * 2) + SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y, SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y); // Hovering
                    break;
                case 4: // Small Down
                    if (i == 1) drawTexturedModalRect(x, y, (LARGE_X * 2) + (SMALL_HORIZONTAL_X * 2), 0, SMALL_VERTICAL_X, SMALL_VERTICAL_Y); // Not Hovering
                    else if (i == 2) drawTexturedModalRect(x, y, (LARGE_X * 2) + (SMALL_HORIZONTAL_X * 2), SMALL_VERTICAL_Y, SMALL_VERTICAL_X, SMALL_VERTICAL_Y); // Hovering
                    break;
                case 5: // Small Up
                    if (i == 1) drawTexturedModalRect(x, y, (LARGE_X * 2) + (SMALL_HORIZONTAL_X * 2) + SMALL_VERTICAL_X, 0, SMALL_VERTICAL_X, SMALL_VERTICAL_Y); // Not Hovering
                    else if (i == 2) drawTexturedModalRect(x, y, (LARGE_X * 2) + (SMALL_HORIZONTAL_X * 2) + SMALL_VERTICAL_X, SMALL_VERTICAL_Y, SMALL_VERTICAL_X, SMALL_VERTICAL_Y); // Hovering
                    break;
            }
            this.mouseDragged(mc, mouseX, mouseY);

            int j = 14737632;
            if (packedFGColour != 0) j = packedFGColour;
            else if (!this.enabled) j = 10526880;
            else if (this.hovered) j = 16777120;
        }
    }
}
