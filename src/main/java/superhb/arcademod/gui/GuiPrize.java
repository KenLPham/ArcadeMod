package superhb.arcademod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArrow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO: Get Prize List from Config or some other editable file for player
// TODO: Enlarge GUI
public class GuiPrize extends GuiScreen {
    private GuiButton prizeNext, prizePrev, amountUp, amountDown, buy;

    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/prize_box.png");
    private Map<ItemStack, Integer> prizeList = new HashMap<ItemStack, Integer>();

    private static final int GUI_X = 124;
    private static final int GUI_Y = 74;
    private int guiLeft = 0, guiTop = 0;
    private int amount = 0, completePrice = 0;

    public GuiPrize () {
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect((width / 2) - (GUI_X / 2), (height / 2) - (GUI_Y / 2), 0, 0, GUI_X, GUI_Y);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui () {
        super.initGui();

        guiLeft = (width - GUI_X) / 2;
        guiTop = (height - GUI_Y) / 2;

        this.buttonList.add(prizeNext = new GuiArrow(0, (guiLeft + 15), (guiTop + 0), 14, 22, 2));
        this.buttonList.add(prizePrev = new GuiArrow(1, (guiLeft + 0), (guiTop + 0), 14, 22, 3));
        this.buttonList.add(amountUp = new GuiArrow(2, (guiLeft + 30), (guiTop + 0), 11, 7, 5));
        this.buttonList.add(amountDown = new GuiArrow(3, (guiLeft + 45), (guiTop + 0), 11, 7, 4));
        this.buttonList.add(buy = new GuiButton(4, (guiLeft + 60), (guiTop + 0), 30, 20, "Buy"));
    }

    @Override
    protected void actionPerformed (GuiButton button) throws IOException {
        if (button == prizeNext) {
            // TODO: Next in prize list
        } else if (button == prizePrev) {
            // TODO: Prev in prize list
        } else if (button == amountUp) {
            if (amount != 64) amount++; // TODO: State max is 64?
        } else if (button == amountDown) {
            if (amount != 0) amount++;
        } else if (button == buy) {
            // TODO: Send Packet
        }
    }

    /*
    @Override
    public boolean doesGuiPauseGame ()
    {
        return false;
    }
    */
}
