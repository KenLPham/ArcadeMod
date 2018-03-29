package superhb.arcademod.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonScalable extends GuiButton {
    private float scale;

    public GuiButtonScalable (int id, int x, int y, float scale, String text) {
        super(id, x, y, text);
        this.scale = scale;
    }

    public GuiButtonScalable (int id, int x, int y, int width, int height, float scale, String text) {
        super(id, x, y, width, height, text);
        this.scale = scale;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int scaledX = Math.round(mouseX / scale);
        int scaledY = Math.round(mouseY / scale);

        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = scaledX >= this.x && scaledY >= this.y && scaledX < this.x + this.width && scaledY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.mouseDragged(mc, scaledX, scaledY);

            int j = 14737632;

            if (packedFGColour != 0) j = packedFGColour;
            else if (!this.enabled) j = 10526880;
            else if (this.hovered) j = 16777120;

            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }

    @Override
    public boolean mousePressed (Minecraft mc, int mouseX, int mouseY) {
        int scaledX = Math.round(mouseX / scale);
        int scaledY = Math.round(mouseY / scale);

        return this.enabled && this.visible && scaledX >= this.x && scaledY >= this.y && scaledX < this.x + this.width && scaledY < this.y + this.height;
    }
}
