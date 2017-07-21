package superhb.arcademod.api.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.network.RewardMessage;
import superhb.arcademod.network.ServerCoinMessage;
import superhb.arcademod.tileentity.TileEntityArcade;
import superhb.arcademod.util.ArcadePacketHandler;

import java.io.IOException;

// TODO: Add comments all functions
public class GuiArcade extends GuiScreen {
    private GuiButton insertCoin;
    private World world;
    protected int tickCounter = 0, tick = 20;
    protected boolean inMenu = true;
    protected int menuOption = 0;
    public int menu = -1, startMenu = 0;
    private int buttonX = 0, buttonY = 0;
    private int cost = 1;
    public int buttonWidth, buttonHeight = 20;
    private boolean enoughCoins = true;
    private TileEntityArcade tileEntity;
    private EntityPlayer player;
    private int guiLeft, guiTop;
    private int xSize = 0, ySize = 0;
    private ResourceLocation gui;

    public GuiArcade (World world, TileEntityArcade tileEntity, EntityPlayer player) {
        this.world = world;
        this.tileEntity = tileEntity;
        this.player = player;
        if (useCoins()) {
            if (!mc.getMinecraft().player.isCreative()) menu = -1;
            else menu = startMenu;
        } else menu = startMenu;
        this.fontRendererObj = mc.getMinecraft().fontRendererObj;
        buttonWidth = this.fontRendererObj.getStringWidth(I18n.format("button.arcademod:insert.locale")) + 6;
    }

    public void setGuiSize (int x, int y) {
        xSize = x;
        ySize = y;
    }

    public void setButtonPos (int x, int y) {
        buttonX = x;
        buttonY = y;
    }

    public void setStartMenu (int startMenu) {
        this.startMenu = startMenu;
    }

    public int getStartMenu () {
        return startMenu;
    }

    public TileEntityArcade getTileEntity () {
        return tileEntity;
    }

    public void setTexture (ResourceLocation texture) {
        gui = texture;
    }

    public void setButtonSize (int width, int height) {
        buttonWidth = width;
        buttonHeight = height;
    }

    public void isEnoughCoins (boolean enough) {
        enoughCoins = enough;
    }

    public boolean useCoins () {
        return !Arcade.disableCoins;
    }

    public void setCost (int cost) {
        this.cost = cost;
    }

    public World getWorld () {
        return world;
    }

    // TODO: Create functions to set Insert Coin Menu Pos
    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(gui);
        this.drawTexturedModalRect((width / 2) - (xSize / 2), (height / 2) - (ySize / 2), 0, 0, xSize, ySize);
        super.drawScreen(mouseX, mouseY, partialTicks);

        int coinWidth = this.fontRendererObj.getStringWidth(I18n.format("button.arcademod:insert.locale") + "...");
        int neededSingWidth = this.fontRendererObj.getStringWidth(cost + " " + I18n.format("text.arcademod:needed.locale"));
        int neededPluralWidth = this.fontRendererObj.getStringWidth(cost + " " + I18n.format("text.arcademod:needed_plural.locale"));

        if (inMenu) {
            switch (menu) {
                case -1: // Insert Coin Menu
                    buttonList.get(0).enabled = true;
                    buttonList.get(0).visible = true;
                    this.fontRendererObj.drawString(I18n.format("button.arcademod:insert.locale") + "...", (width / 2) - (coinWidth / 2), height / 2, 16777215);
                    if (!enoughCoins) {
                        if (cost == 1) this.fontRendererObj.drawString(cost + " " + I18n.format("text.arcademod:needed.locale"), (width / 2) - (neededSingWidth / 2), (height / 2) + 10, 16711680);
                        else this.fontRendererObj.drawString(cost + " " + I18n.format("text.arcademod:needed_plural.locale"), (width / 2) - (neededPluralWidth / 2), (height / 2) + 10, 16711680);
                    }
                    break;
            }
        }

        if (menu != -1) {
            buttonList.get(0).enabled = false;
            buttonList.get(0).visible = false;
        }
    }

    @Override
    public boolean doesGuiPauseGame () {
        return false;
    }

    // TODO: Different button texture?
    @Override
    public void initGui () {
        super.initGui();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        if (useCoins()) this.buttonList.add(insertCoin = new GuiButton(0, (guiLeft + buttonX), (guiTop + buttonY), buttonWidth, buttonHeight, I18n.format("button.arcademod:insert.locale")));
    }

    // TODO: Leaderboard
    public void addPlayerToLeaderboard (String name, int score, String difficulty) {
    }

    public void addPlayerToLeaderboard (int place, String name, int score, String difficulty) {
    }

    public NBTTagList getLeaderboard () {
        return tileEntity.getLeaderboard();
    }

    public NBTTagCompound getHighscore () {
        return tileEntity.getLeaderboard().getCompoundTagAt(0);
    }

    @Override
    protected void actionPerformed (GuiButton button) throws IOException {
        if (button == insertCoin) {
            if (menu == -1) ArcadePacketHandler.INSTANCE.sendToServer(new ServerCoinMessage(new ItemStack(ArcadeItems.coin), cost));
        }
    }

    @Override
    protected void keyTyped (char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // Esc
            if (menu == startMenu && !mc.player.isCreative()) giveReward(ArcadeItems.coin, cost);
        }
        super.keyTyped(typedChar, keyCode);
    }

    public void giveReward (ItemStack reward) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(reward));
    }

    public void giveReward (Item item, int amount) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, 0));
    }

    public void giveReward (Item item, int amount, int meta) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, meta));
    }

    public void giveReward (Item item, int amount, int meta, NBTTagCompound compound) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, meta, compound));
    }

    public void checkMenuAfterGameOver () {
        if (useCoins()) {
            if (!mc.getMinecraft().player.isCreative()) menu = -1;
            else menu = startMenu;
        } else menu = startMenu;
    }
}
