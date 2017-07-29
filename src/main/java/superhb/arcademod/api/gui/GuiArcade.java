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
import org.lwjgl.input.Keyboard;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.network.RewardMessage;
import superhb.arcademod.network.ServerCoinMessage;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.ArcadePacketHandler;
import superhb.arcademod.util.ArcadeSoundRegistry;

import java.io.IOException;

// TODO: Add comments all functions
public class GuiArcade extends GuiScreen {
    // Tick Variables
    public int tickCounter = 0;
    public float prevTick = 0;
    private boolean useTick = true;

    // Menu Variables
    public boolean inMenu = true;
    public int menuOption = 0;
    public int menu = -1, startMenu = 0;

    // GUI Variables
    private int guiLeft, guiTop;
    private int xSize = 0, ySize = 0;
    private ResourceLocation gui;
    private GuiButton insertCoin;
    private int buttonX = 0, buttonY = 0;
    public int buttonWidth, buttonHeight = 20;
    private int[] offset = { 0, 0 };

    // Cost Variables
    private int cost = 1;
    private boolean enoughCoins = true;
    public boolean canGetCoinBack = true;

    // Constructor Variable
    private TileEntityArcade tileEntity;
    private EntityPlayer player; // TODO: Remove player?
    private World world;

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

    /**
     * Set the width and height of the GUI Texture
     *
     * @param width Width of Texture
     * @param height Height of Texture
     */
    public void setGuiSize (int width, int height) {
        xSize = width;
        ySize = height;
    }

    /**
     * Set the 'Insert Coin' Button Position.
     * (0,0) is the top left corner of GUI
     *
     * @param x X Position
     * @param y Y Position
     */
    public void setButtonPos (int x, int y) {
        buttonX = x;
        buttonY = y;
    }

    // Sets the offset of the Text of the Insert Coin Menu

    /**
     * Offsets the text of the Insert Coin Menu.
     * This will do nothing if useBasicMenu is false
     *
     * @param x X Offset
     * @param y Y Offset
     */
    public void setOffset (int x, int y) {
        offset[0] = x;
        offset[1] = y;
    }

    /**
     * Sets ID of which menu is the Main Menu
     *
     * @param startMenu Main Menu ID
     */
    public void setStartMenu (int startMenu) {
        this.startMenu = startMenu;
    }

    /**
     * Returns Main Menu ID
     *
     * @return Main Menu ID
     */
    public int getStartMenu () {
        return startMenu;
    }

    /**
     * Returns TileEntity passed by GUIHandler
     *
     * @return TileEntity
     */
    public TileEntityArcade getTileEntity () {
        return tileEntity;
    }

    /**
     * Set GUI Texture
     *
     * @param texture GUI Texture Location
     */
    public void setTexture (ResourceLocation texture) {
        gui = texture;
    }

    /**
     * Set a custom size to the 'Insert Coin' Button
     *
     * @param width Width of Button
     * @param height Height of Button
     */
    public void setButtonSize (int width, int height) {
        buttonWidth = width;
        buttonHeight = height;
    }

    public void isEnoughCoins (boolean enough) {
        enoughCoins = enough;
    }

    /**
     * Checks if coin usage is disabled or enabled in config
     *
     * @return Boolean set in config
     */
    public boolean useCoins () {
        return !Arcade.disableCoins;
    }

    /**
     * Set how many coins the player needs to play game
     *
     * @param cost Max 64
     */
    public void setCost (int cost) throws IndexOutOfBoundsException {
        if (cost > 64) throw new IndexOutOfBoundsException("Max is 64");
        else this.cost = cost;
    }

    /**
     * Returns World passed by GUIHandler
     *
     * @return world
     */
    public World getWorld () {
        return world;
    }

    @Override
    public void updateScreen () {
        if (useTick) tickCounter++;
    }

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

        // TODO: Make bool to allow people to turn of this insert coin menu and make their own
        if (inMenu) {
            switch (menu) {
                case -1: // Insert Coin Menu
                    buttonList.get(0).enabled = true;
                    buttonList.get(0).visible = true;
                    this.fontRendererObj.drawString(I18n.format("button.arcademod:insert.locale") + "...", (width / 2) - (coinWidth / 2) + offset[0], height / 2 + offset[1], 16777215);
                    if (!enoughCoins) {
                        if (cost == 1) this.fontRendererObj.drawString(cost + " " + I18n.format("text.arcademod:needed.locale"), (width / 2) - (neededSingWidth / 2) + offset[0], (height / 2) + 10 + offset[1], 16711680);
                        else this.fontRendererObj.drawString(cost + " " + I18n.format("text.arcademod:needed_plural.locale"), (width / 2) - (neededPluralWidth / 2) + offset[0], (height / 2) + 10 + offset[1], 16711680);
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

    @Override
    public void initGui () {
        super.initGui();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        if (useCoins()) this.buttonList.add(insertCoin = new GuiSoundButton(0, (guiLeft + buttonX), (guiTop + buttonY), buttonWidth, buttonHeight, I18n.format("button.arcademod:insert.locale"), ArcadeSoundRegistry.INSERT_COIN));
    }

    // UNUSED
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
    // UNUSED

    @Override
    protected void actionPerformed (GuiButton button) throws IOException {
        if (button == insertCoin) {
            if (menu == -1) ArcadePacketHandler.INSTANCE.sendToServer(new ServerCoinMessage(new ItemStack(ArcadeItems.coin), cost));
        }
    }

    /**
     * Checks if key is pressed.
     * Use isKeyDown(keyCode), if you want to check if key is held down
     *
     * @param typedChar Key Character
     * @param keyCode Key Code
     * @throws IOException
     */
    @Override
    protected void keyTyped (char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // Esc
            if (canGetCoinBack && !mc.player.isCreative() && menu != -1) giveReward(ArcadeItems.coin, cost);
        }
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Checks if key is held down
     *
     * @param keyCode Key Code
     * @return true if pressed
     */
    public boolean isKeyDown (int keyCode) {
        return Keyboard.isKeyDown(keyCode);
    }

    /**
     * Gives player ItemStack
     *
     * @param reward ItemStack to give player
     */
    public void giveReward (ItemStack reward) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(reward));
    }

    /**
     * Gives player item
     *
     * @param item Item to give player
     * @param amount How much of the item to give
     */
    public void giveReward (Item item, int amount) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, 0));
    }

    /**
     * Gives player item
     * @param item Item to give player
     * @param amount How much of the item to give
     * @param meta Item Metadata
     */
    public void giveReward (Item item, int amount, int meta) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, meta));
    }

    /**
     * Give player item
     *
     * @param item Item to give player
     * @param amount How much of the item to give
     * @param meta Item Metadata
     * @param compound NBT Tag Compound for item
     */
    public void giveReward (Item item, int amount, int meta, NBTTagCompound compound) {
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, meta, compound));
    }

    /**
     * Checks which menu (Main Menu or Coin Menu) to go to
     */
    public void checkMenuAfterGameOver () {
        if (useCoins()) {
            if (!mc.getMinecraft().player.isCreative()) menu = -1;
            else menu = startMenu;
        } else menu = startMenu;
    }
}
