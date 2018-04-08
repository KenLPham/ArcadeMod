package superhb.arcademod.api.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.tileentity.ArcadeLeaderboard;
import superhb.arcademod.network.RewardMessage;
import superhb.arcademod.network.ServerCoinMessage;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.*;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;

public class GuiArcade extends GuiScreen {
	// Tick Variables
	public int tickCounter = 0;
	private boolean useTick = true;
	
	// Menu Variables
	public boolean inMenu = true;
	public int menuOption = 0;
	public int menu = -1, startMenu = 0;
	public boolean useInternalMenu = true;
	
	// GUI Variables
	public int textureWidth = 256, textureHeight = 256;
	private int guiLeft, guiTop;
	private int xSize = 0, ySize = 0;
	private ResourceLocation gui;
	private GuiButton insertCoin;
	private int buttonX = 0, buttonY = 0;
	public int buttonWidth, buttonHeight = 20;
	private int[] offset = { 0, 0 };
	private float scale = 1;
	public int xScaled, yScaled;
	
	// Cost Variables
	private int cost = 1;
	private boolean enoughCoins = true;
	public boolean canGetCoinBack = true;
	
	// Constructor Variable
	private TileEntityArcade tileEntity;
	private EntityPlayer player; // TODO: Remove player?
	private World world;
	private BlockPos pos;
	
	// Arrow Texture
	private static final ResourceLocation arrows = new ResourceLocation(Reference.MODID + ":textures/gui/gui_arrows.png");
	
	public GuiArcade (World world, TileEntityArcade tileEntity, @Nullable BlockPos pos, EntityPlayer player) {
		this.pos = pos;
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
	 * @param width  Width of Texture
	 * @param height Height of Texture
	 */
	public void setGuiSize (int width, int height) {
		xSize = width;
		ySize = height;
	}
	
	/**
	 * Set the width and height of the GUI Texture along with Scale Factor
	 *
	 * @param width  Width of Texture
	 * @param height Height of Texture
	 * @param scale  Scale Factor
	 */
	public void setGuiSize (int width, int height, float scale) {
		xSize = width;
		ySize = height;
		this.scale = scale;
	}
	
	/**
	 * Set Scale Factor
	 *
	 * @param scale Scale Factor
	 */
	public void setGuiScale (float scale) {
		this.scale = scale;
	}
	
	/**
	 * Gets GUI Scale Factor
	 *
	 * @return scale
	 */
	public float getGuiScale () {
		return scale;
	}
	
	/**
	 * Disable Insert Menu to allow custom ones
	 *
	 * @param disable True = off, False = on
	 */
	public void disableInternalMenu (boolean disable) {
		useInternalMenu = !disable;
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
	 * Set GUI Texture with custom width and height
	 *
	 * @param texture GUI Texture Location
	 * @param width   Custom Texture Width
	 * @param height  Custom Texture Height
	 */
	public void setTexture (ResourceLocation texture, int width, int height) {
		gui = texture;
		textureWidth = width;
		textureHeight = height;
	}
	
	/**
	 * Set a custom size to the 'Insert Coin' Button
	 *
	 * @param width  Width of Button
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
	
	/**
	 * Returns position of block by GUIHandler.
	 * Used officially to play sounds
	 *
	 * @return BlockPos
	 */
	public BlockPos getPos () {
		return pos;
	}
	
	/**
	 * Returns player entity passed by GUIHandler
	 * Used officially to play sounds
	 *
	 * @return EntityPlayer
	 */
	public EntityPlayer getPlayer () {
		return player;
	}
	
	@Override
	public void updateScreen () {
		if (useTick) tickCounter++;
	}
	
	@Override
	public void drawScreen (int mouseX, int mouseY, float partialTicks) {
		xScaled = Math.round((width / 2) / scale);
		yScaled = Math.round((height / 2) / scale);
		
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.scale(scale, scale, scale);
		this.mc.getTextureManager().bindTexture(gui);
		this.drawModalRectWithCustomSizedTexture(xScaled - (xSize / 2), yScaled - (ySize / 2), 0, 0, xSize, ySize, textureWidth, textureHeight); // TODO: Allow offset?
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (useInternalMenu) {
			int coinWidth = this.fontRendererObj.getStringWidth(I18n.format("button.arcademod:insert.locale") + "...");
			int neededSingWidth = this.fontRendererObj.getStringWidth(cost + " " + I18n.format("text.arcademod:needed.locale"));
			int neededPluralWidth = this.fontRendererObj.getStringWidth(cost + " " + I18n.format("text.arcademod:needed_plural.locale"));
			
			if (inMenu) {
				switch (menu) {
					case -1: // Insert Coin Menu
						buttonList.get(0).enabled = true;
						buttonList.get(0).visible = true;
						this.fontRendererObj.drawString(I18n.format("button.arcademod:insert.locale") + "...", xScaled - (coinWidth / 2) + offset[0], yScaled + offset[1], 16777215);
						if (!enoughCoins) {
							if (cost == 1)
								this.fontRendererObj.drawString(cost + " " + I18n.format("text.arcademod:needed.locale"), xScaled - (neededSingWidth / 2) + offset[0], yScaled + 10 + offset[1], 16711680);
							else
								this.fontRendererObj.drawString(cost + " " + I18n.format("text.arcademod:needed_plural.locale"), xScaled - (neededPluralWidth / 2) + offset[0], yScaled + 10 + offset[1], 16711680);
						}
						break;
				}
			}
			
			if (menu != -1) {
				buttonList.get(0).enabled = false;
				buttonList.get(0).visible = false;
			}
		}
	}
	
	/**
	 * Draw left arrow
	 */
	public void drawLeftArrow (int x, int y) {
		drawLeftArrow(x, y, false);
	}
	
	/**
	 * Draw right arrow
	 */
	public void drawRightArrow (int x, int y) {
		drawRightArrow(x, y, false);
	}
	
	/**
	 * Draw up arrow
	 */
	public void drawUpArrow (int x, int y) {
		drawUpArrow(x, y, false);
	}
	
	/**
	 * Draw down arrow
	 */
	public void drawDownArrow (int x, int y) {
		drawDownArrow(x, y, false);
	}
	
	/**
	 * Draw left arrow
	 *
	 * @param bind call texture manager to bind texture
	 */
	public void drawLeftArrow (int x, int y, boolean bind) {
		if (bind) this.mc.getTextureManager().bindTexture(arrows);
		this.drawModalRectWithCustomSizedTexture(x, y, 7, 0, 7, 11, 128, 128);
	}
	
	/**
	 * Draw right arrow
	 *
	 * @param bind call texture manager to bind texture
	 */
	public void drawRightArrow (int x, int y, boolean bind) {
		if (bind) this.mc.getTextureManager().bindTexture(arrows);
		this.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 7, 11, 128, 128);
	}
	
	/**
	 * Draw up arrow
	 *
	 * @param bind call texture manager to bind texture
	 */
	public void drawUpArrow (int x, int y, boolean bind) {
		if (bind) this.mc.getTextureManager().bindTexture(arrows);
		this.drawModalRectWithCustomSizedTexture(x, y, 25, 0, 11, 7, 128, 128);
	}
	
	/**
	 * Draw down arrow
	 *
	 * @param bind call texture manager to bind texture
	 */
	public void drawDownArrow (int x, int y, boolean bind) {
		if (bind) this.mc.getTextureManager().bindTexture(arrows);
		this.drawModalRectWithCustomSizedTexture(x, y, 14, 0, 11, 7, 128, 128);
	}
	
	@Override
	public boolean doesGuiPauseGame () {
		return false;
	}
	
	@Override
	public void initGui () {
		super.initGui();
		
		this.guiLeft = Math.round((width / 2) / scale) - (xSize / 2); //(this.width - this.xSize) / 2;
		this.guiTop = Math.round((height / 2) / scale) - (ySize / 2); //(this.height - this.ySize) / 2;
		
		if (useCoins())
			this.buttonList.add(insertCoin = new GuiSoundButton(0, (guiLeft + buttonX), (guiTop + buttonY), buttonWidth, buttonHeight, scale, I18n.format("button.arcademod:insert.locale"), ArcadeSoundRegistry.INSERT_COIN));
	}
	
	// TODO: Use it stop all sounds
	@Override
	public void onGuiClosed () {
	}
	
	// UNUSED
	// TODO: Leaderboard
	// TODO: Use packet instead of direct access (Check if needed before doing that)
	@Deprecated
	public void updateLeaderboard (String name, int score, String difficulty) {
		boolean stopChecking = false;
		ArcadeLeaderboard[] temp = getLeaderboard();
		ArcadeLeaderboard[] leaderboard = getLeaderboard();
		int place = 11;
		
		// TODO: Move check here
		
		tileEntity.saveLeaderboard(leaderboard);
	}
	
	/**
	 * Get separated RGB values of Color
	 *
	 * @return separated RGB values in float array
	 */
	public float[] colorToFloat (Color color) {
		float red = Math.round((color.getRed() / 255.0F) * 100.0F) / 100.0F;
		float green = Math.round((color.getGreen() / 255.0F) * 100.0F) / 100.0F;
		float blue = Math.round((color.getBlue() / 255.0F) * 100.0F) / 100.0F;
		
		return new float[] { red, green, blue };
	}
	
	/**
	 * Set color of GUI modal with Color rather than RGB values
	 */
	public void glColor (Color color) {
		float red = Math.round((color.getRed() / 255.0F) * 100.0F) / 100.0F;
		float green = Math.round((color.getGreen() / 255.0F) * 100.0F) / 100.0F;
		float blue = Math.round((color.getBlue() / 255.0F) * 100.0F) / 100.0F;
		
		GlStateManager.color(red, green, blue);
	}
	
	/**
	 * Re-map a number from one range to another
	 */
	@Deprecated
	public long map (long in, long inMin, long inMax, long outMin, long outMax) {
		return (in - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
	}
	
	public double rand (int min, int max) {
		return (Math.random() * (max - min)) + min;
	}

    /* Leaderboard code
    static boolean stopChecking = false;
  static int place = 11;
  static int score = 1200;
  static int[] scores = { 5000, 3000, 1200, 1200, 900, 700, 500, 400, 300, 200 };
  static int[] temp = { 5000, 3000, 1200, 1200, 900, 700, 500, 400, 300, 200 };

  public static void main(String[] args) {
    	for (int i = 9; i >= 0; i--) {
    		System.out.println("Current Leaderboard: [" + i +"]: [" + scores[i] + "]");
    		if (!stopChecking) {
    			if (score > scores[i]) {
    				System.out.println("Score is greater than [" + i + "]");
    				if (i < place || place == 11) {
    					//System.out.println("Setting Place to [" + i + "]");
    					place = i;
    				}
    			} else if (score == scores[i]) {
    				//System.out.println("Score is equal to [" + i + "]");
    				if (i != 9) {
    					place = i + 1;
    					stopChecking = true;
    				}
    			} else if (score < scores[i]) {
    				stopChecking = true;
    			}
    		}
  	}

  	if (place != 11) {
  		scores[place] = score;
  		System.out.println("Place: [" + place + "]");
  		//for (int i = 9; i >= 0; i--) {
  		//	System.out.println("Temp Leaderboard: [" + i +"]: [" + temp[i] + "]");
  		//}
  		for (int i = place + 1; i < 10; i++) {
  			//System.out.println("Setting [" + (i - 1) + "] to [" + i + "]");
  			scores[i] = temp[i - 1];
  		}
  	}

  	for (int i = 9; i >= 0; i--) {
  		System.out.println("New Leaderboard: [" + i +"]: [" + scores[i] + "]");
  	}
  }
     */
	
	@Deprecated
	public ArcadeLeaderboard[] getLeaderboard () {
		return tileEntity.getLeaderboard();
	}
	
	@Deprecated
	public ArcadeLeaderboard getHighscore () {
		return tileEntity.getLeaderboard()[0];
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
	 * @param keyCode   Key Code
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
	 * @param item   Item to give player
	 * @param amount How much of the item to give
	 */
	public void giveReward (Item item, int amount) {
		ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, 0));
	}
	
	/**
	 * Gives player item
	 *
	 * @param item   Item to give player
	 * @param amount How much of the item to give
	 * @param meta   Item Metadata
	 */
	public void giveReward (Item item, int amount, int meta) {
		ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(item, amount, meta));
	}
	
	/**
	 * Give player item
	 *
	 * @param item     Item to give player
	 * @param amount   How much of the item to give
	 * @param meta     Item Metadata
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
