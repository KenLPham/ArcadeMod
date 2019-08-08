package superhb.arcademod.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.lwjgl.input.Mouse;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.audio.ArcadeSounds;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.network.pong.GetPlayerMessage;
import superhb.arcademod.util.ArcadePacketHandler;
import superhb.arcademod.util.KeyHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;

// TODO: Add sounds?
public class GuiPong extends GuiArcade {
	// Board: 240x154
	// GUI: 252x166
	// Paddle: 5x28
	// Board_Outline: 236x150 (offset by 2,2)
	// Ball: 4x4
	// Numbers: 12x20
	
	private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/pong.png");
	
	// Texture Variables
	private static final int BOARD_X = 240;
	private static final int BOARD_Y = 154;
	
	private static final int GUI_X = 252;
	private static final int GUI_Y = 166;
	
	private static final int PADDLE_X = 5;
	private static final int PADDLE_Y = 28;
	
	private static final int OUTLINE_X = 236;
	private static final int OUTLINE_Y = 150;
	
	private static final int BALL = 4;
	
	private static final int NUMBER_X = 12;
	private static final int NUMBER_Y = 20;
	
	private static final int WINNER_X = 86;
	private static final int WINNER_Y = 17;
	
	private static final int LOSER_X = 65;
	private static final int LOSER_Y = 17;
	
	// Board Variables
	private int boardX, boardY;
	private int mouseY;
	private Rectangle[] outlineBoundingBox = new Rectangle[2];
	private Rectangle[] outBoundingBox = new Rectangle[2];
	private boolean canMultiplayer, isPlayingMultiplayer, inMultiplayerMenu, canStartMultiplayer;
	private int startOption;
	private int endGame;
	
	// Game Variable
	private Ball ball;
	private Paddle[] paddles = new Paddle[2];
	
	// Multiplayer Variable
	private EntityPlayer opponent;
	
	public GuiPong (World world, TileEntityArcade tileEntity, @Nullable BlockPos pos, EntityPlayer player) {
		super(world, tileEntity, pos, player);
		setGuiSize(GUI_X, GUI_Y);
		setCost(1);
		setOffset(0, 0);
		setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
		setTexture(texture, 512, 512);
		setStartMenu(0);
		
		addPlayer(player.getName());
		
		// TODO: Multiplayer
		//if (!mc.getMinecraft().isSingleplayer() || mc.getMinecraft().getConnection().getPlayerInfoMap().size() > 1) canMultiplayer = true;
	}
	
	@Override
	public void updateScreen () {
		super.updateScreen();
		if (!inMenu) {
			if (endGame == 0) {
				for (int i = 0; i < 3; i++) {
					outlineBoundingBox[0].setLocation(boardX, boardY);
					outlineBoundingBox[1].setLocation(boardX, boardY + (OUTLINE_Y - 5));
					outBoundingBox[0].setLocation(boardX - 5, boardY + 5);
					outBoundingBox[1].setLocation(boardX + OUTLINE_X, boardY + 5);
					paddles[0].updatePosition(boardX, boardY);
					paddles[0].ai();
					paddles[1].updatePosition(boardX, boardY, mouseY);
					ball.updatePosition(boardX, boardY);
				}
				for (int i = 0; i < 3; i++) ball.collisionDetection();
				ball.updateAngle();
				
				// Score Logic
				if ((paddles[0].score / 3) == 10) { // Game Over
					endGame = 1;
				} else if ((paddles[1].score / 3) == 10) { // Winner
					endGame = 2;
					giveReward(ArcadeItems.TICKET, 10);
				}
			}
		}
	}

	@Override
	public void drawScreen (int mouseX, int mouseY, float partialTicks) {
		boardX = xScaled - (GUI_X / 2) + 8;
		boardY = yScaled - (GUI_Y / 2) + 8;
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (inMenu) {
			if (Mouse.isGrabbed()) Mouse.setGrabbed(false);

			int settingWidth = this.fontRenderer.getStringWidth(I18n.format("option.arcademod:setting.locale"));

			switch (menu) {
				case 0: // Main Menu
					this.fontRenderer.drawString(I18n.format("game.arcademod:pong.name"), boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("game.arcademod:pong.name")) / 2), boardY + 2, Color.WHITE.getRGB());
					
					this.fontRenderer.drawString(I18n.format("option.arcademod:start.locale"), boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("option.arcademod:start.locale")) / 2), boardY + (GUI_Y / 2) - 10, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:control.locale"), boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("option.arcademod:control.locale")) / 2), boardY + (GUI_Y / 2), Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:setting.locale"), boardX + (GUI_X / 2) - (settingWidth / 2), boardY + (GUI_Y / 2) + 10, Color.WHITE.getRGB());

					if (menuOption == 0) drawRightArrow(boardX + (GUI_X / 2) - 30,  boardY + (GUI_Y / 2) - 12, true);
					else if (menuOption == 1) drawRightArrow(boardX + (GUI_X / 2) - 30,  boardY + (GUI_Y / 2) - 2, true);
					else drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) + 8, true);
					break;
				case 1: // Start Menu
					this.fontRenderer.drawString(I18n.format("option.arcademod:start.locale"), boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("option.arcademod:start.locale")) / 2), boardY + 2, Color.WHITE.getRGB());
					
					this.fontRenderer.drawString(I18n.format("option.arcademod:singleplayer.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 10, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:multiplayer.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2), Color.WHITE.getRGB());
					
					if (startOption == 0) drawRightArrow(boardX + (GUI_X / 2) - 40, boardY + (GUI_Y / 2) - 12, true);
					else drawRightArrow(boardX + (GUI_X / 2) - 40, boardY + (GUI_Y / 2) - 2, true);
					
					// Back
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), boardX + 2, boardY + (GUI_Y) - 23, Color.WHITE.getRGB());
					break;
				case 2: // Control Menu
					this.fontRenderer.drawString(I18n.format("control.arcademod:mouse.pong.name"), boardX + (this.fontRenderer.getStringWidth(I18n.format("control.arcademod:mouse.pong.name")) / 2), boardY + (GUI_Y / 2), Color.WHITE.getRGB());
					
					// Back
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), boardX + 2, boardY + (GUI_Y) - 23, Color.WHITE.getRGB());
					break;
				case 3: // Multiplayer Menu
					this.fontRenderer.drawString(I18n.format("option.arcademod:multiplayer.name"), boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("option.arcademod:multiplayer.name")) / 2), boardY + 2, Color.WHITE.getRGB());
					
					this.fontRenderer.drawString("[" + I18n.format("text.arcademod:left_paddle.pong.name") + "]", boardX + (GUI_X / 2) - 70, boardY + (GUI_Y / 2) - 10, Color.WHITE.getRGB());
					this.fontRenderer.drawString("[" + I18n.format("text.arcademod:right_paddle.pong.name") + "]", boardX + (GUI_X / 2) - 70, boardY + (GUI_Y / 2), Color.WHITE.getRGB());
					
					// Ready
					this.fontRenderer.drawString("[" + KeyHandler.select.getDisplayName() + "]" + I18n.format("option.arcademod:start.locale"), boardX + 2, boardY + (GUI_Y) - 33, canStartMultiplayer ? Color.WHITE.getRGB() : Color.GRAY.getRGB());
					
					// Back
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), boardX + 2, boardY + (GUI_Y) - 23, Color.WHITE.getRGB());
					break;
				case 4: // Settings Menu
					this.fontRenderer.drawString(I18n.format("option.arcademod:setting.locale"), boardX + (GUI_X / 2) - (settingWidth / 2), boardY + 2, Color.WHITE.getRGB());

					int volumeWidth = this.fontRenderer.getStringWidth(I18n.format("text.arcademod:volume.locale"));
					this.fontRenderer.drawString(I18n.format("text.arcademod:volume.locale"), boardX + (GUI_X / 2) - (volumeWidth / 2), boardY + (GUI_Y / 2) - 30, Color.white.getRGB());
					drawVolumeBar(boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 20);

					// Back
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), boardX + 2, boardY + (GUI_Y) - 23, Color.WHITE.getRGB());
					// Edit/Save
					this.fontRenderer.drawString("[" + KeyHandler.select.getDisplayName() + "] " + (editVolume ? I18n.format("text.arcademod:save.locale") : I18n.format("text.arcademod:edit.locale")), boardX + 2, boardY + (GUI_Y) - 33, Color.white.getRGB());
					break;
			}
		} else {
			Mouse.setGrabbed(true); // Hides Mouse
			this.drawModalRectWithCustomSizedTexture(boardX, boardY, GUI_X, 0, OUTLINE_X, OUTLINE_Y, 512, 512);
			drawScore();
			ball.draw();
			if (mouseY <= (boardY + 5)) this.mouseY = boardY + 5;
			else if (mouseY >= (boardY + 117)) this.mouseY = boardY + 117;
			else this.mouseY = mouseY;
			for (Paddle paddle : paddles) paddle.draw();
			
			if (endGame > 0) {
				if (endGame == 2) this.drawModalRectWithCustomSizedTexture(boardX + 10, boardY + 30, 0, GUI_Y + PADDLE_Y + NUMBER_Y, WINNER_X, WINNER_Y, 512, 512);
				else if (endGame == 1) this.drawModalRectWithCustomSizedTexture(boardX + 10, boardY + 30, 0, GUI_Y + PADDLE_Y + NUMBER_Y + WINNER_Y, LOSER_X, LOSER_Y, 512, 512);
				this.fontRenderer.drawString("[" + KeyHandler.select.getDisplayName() + "]", boardX + 2, boardY + GUI_Y - 40, Color.WHITE.getRGB());
				this.fontRenderer.drawString(I18n.format("text.arcademod:return_menu.name"), boardX + 2, boardY + GUI_Y - 30, Color.WHITE.getRGB());
			}
		}
	}
	
	private void drawScore () {
		glColor(Color.GRAY);
		if (paddles[0].score / 3 < 10) this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) - 30, boardY + 7, (12 * (paddles[0].score / 3)), GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
		else {
			this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) - 30, boardY + 7, 0, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
			this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) - 40, boardY + 7, 12, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
		}
		if (paddles[1].score / 3 < 10) this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) + 14, boardY + 7, (12 * (paddles[1].score / 3)), GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
		else {
			this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) + 28, boardY + 7, 0, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
			this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) + 14, boardY + 7, 12, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
		}
	}
	
	private void startGame () {
		inMenu = false;
		outlineBoundingBox[0] = new Rectangle(boardX, boardY, OUTLINE_X, 5);
		outlineBoundingBox[1] = new Rectangle(boardX, boardY + (OUTLINE_Y - 5), OUTLINE_X, 5);
		outBoundingBox[0] = new Rectangle(boardX - 5, boardY + 5, 5, OUTLINE_Y - 10);
		outBoundingBox[1] = new Rectangle(boardX + OUTLINE_X, boardY + 5, 5, OUTLINE_Y - 10);
		paddles[0] = new Paddle(0, 5);
		paddles[1] = new Paddle(BOARD_X - 9, this.mouseY);
		ball = new Ball(116, (int)rand(5, 117), (int)rand(0, 2));
		canGetCoinBack = false;
	}
	
	@Override
	protected void keyTyped (char typedChar, int keyCode) throws IOException {
		if (inMenu) {
			if (menu == 0) { // Main Menu
				if (keyCode == KeyHandler.up.getKeyCode()) {
					if (menuOption == 0) menuOption = 2;
					else menuOption--;
				}
				if (keyCode == KeyHandler.down.getKeyCode()) {
					if (menuOption == 2) menuOption = 0;
					else menuOption++;
				}
				if (keyCode == KeyHandler.select.getKeyCode()) {
					if (menuOption == 0) { // Start Option
						if (canMultiplayer) { // TODO: Send packet
							menu = 1;
						}
						else startGame();
					}
					if (menuOption == 1) menu = 2;
					if (menuOption == 2) menu = 4;
				}
			} else if (menu == 1) { // Start Menu
				if (keyCode == KeyHandler.up.getKeyCode()) {
					if (startOption == 0) startOption = 1;
					else startOption--;
				}
				if (keyCode == KeyHandler.down.getKeyCode()) {
					if (startOption == 1) startOption = 0;
					else startOption++;
				}
				if (keyCode == KeyHandler.select.getKeyCode()) {
					if (startOption == 0) startGame();
					else menu = 3;
				}
				if (keyCode == KeyHandler.left.getKeyCode()) menu = 0;
			} else if (menu == 2) { // Control Menu
				if (keyCode == KeyHandler.left.getKeyCode()) menu = 0;
			} else if (menu == 3) { // Multiplayer Menu
				if (keyCode == KeyHandler.select.getKeyCode()) {
					//if (canStartMultiplayer) startMultiplayer();
				}
				
				// TODO: Send disconnect packet
				if (keyCode == KeyHandler.left.getKeyCode()) menu = 1;
			} else if (menu == 4) { // Settings Menu
				if (keyCode == KeyHandler.left.getKeyCode()) {
					if (editVolume) decreaseVolume();
					else menu = 0;
				}
				if (keyCode == KeyHandler.right.getKeyCode()) {
					if (editVolume) increaseVolume();
				}
				if (keyCode == KeyHandler.select.getKeyCode()) {
					if (editVolume) {
						editVolume = false;
						saveVolume(true);
					} else editVolume = true;
				}
				if (keyCode == 1 && editVolume) {
					editVolume = false;
					saveVolume(false);
					return;
				}
			}
		} else {
			if (endGame > 0) {
				if (keyCode == KeyHandler.select.getKeyCode()) {
					inMenu = true;
					checkMenuAfterGameOver();
					endGame = 0;
				}
			} else {
				if (keyCode == 1) {
					if (paddles[1].score > paddles[0].score) giveReward(ArcadeItems.TICKET, (paddles[1].score / 3));
				}
			}
		}
		if (keyCode == 1) { // Esc
			if (Mouse.isGrabbed()) Mouse.setGrabbed(false);
			removePlayer(getPlayer().getName());
		}
		super.keyTyped(typedChar, keyCode);
	}
	
	public void addPlayer (String name) {
		getTileEntity().addPlayer(name);
	}
	
	public void removePlayer (String name) {
		getTileEntity().removePlayer(name);
	}
	
	public void joinGame () {
		ArcadePacketHandler.INSTANCE.sendToAllAround(new GetPlayerMessage(), new NetworkRegistry.TargetPoint(0, getPos().getX(), getPos().getY(), getPos().getZ(), 15));
	}
	
	private class Paddle {
		int x, y, extendedX, extendedY;
		Rectangle boundingBox;
		int score = 0;
		
		private Paddle (int x, int y) {
			this.x = x;
			this.y = y;
			this.boundingBox = new Rectangle(x, y, PADDLE_X, PADDLE_Y);
		}
		
		public void updatePosition (int x, int y) {
			this.extendedX = x + this.x;
			this.extendedY = y + this.y;
			this.boundingBox.setLocation(extendedX, extendedY);
		}
		
		public void updatePosition (int x, int y, int mouseY) {
			this.extendedX = x + this.x;
			this.extendedY = mouseY;
			this.boundingBox.setLocation(extendedX, extendedY);
		}
		
		public void ai () {
			if (extendedY > ball.extendedY && y != 5) y -= 1;
			else if (extendedY < ball.extendedY && y != 117) y += 1;
		}
		
		public void draw () {
			drawModalRectWithCustomSizedTexture(extendedX, extendedY, 0, GUI_Y, PADDLE_X, PADDLE_Y, 512, 512);
		}
	}
	
	private class Ball {
		int x, y, extendedX, extendedY;
		int xV = 1, yV = 1, angle;
		int xA = 3, yA = 3;
		Rectangle boundingBox;
		
		private Ball (int x, int y, int angle) {
			this.x = x;
			this.y = y;
			this.angle = angle;
			this.boundingBox = new Rectangle(x, y, BALL, BALL);
		}
		
		public void draw () {
			glColor(Color.WHITE);
			drawModalRectWithCustomSizedTexture(extendedX, extendedY, PADDLE_X, GUI_Y, BALL, BALL, 512, 512);
		}
		
		public void updatePosition (int x, int y) {
			this.extendedX = x + this.x;
			this.extendedY = y + this.y;
			this.boundingBox.setLocation(extendedX, extendedY);
		}
		
		public void updateAngle () {
			for (int i = 0; i < xA; i++) x += xV;
			for (int i = 0; i < yA; i++) y += yV;
		}
		
		public void collisionDetection () {
			// Outline Collision
			for (Rectangle box : outlineBoundingBox) {
				if (boundingBox.intersects(box)) {
					yV = -yV;
					getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PONG_WALL, SoundCategory.BLOCKS, getVolume(), 1.0f);
				}
			}
			
			// Out Collision
			if (boundingBox.intersects(outBoundingBox[0])) { // Out on left paddle, right gets point
				xV = 1;
				angle = (int)rand(0, 2);
				x = 116;
				y = (int)rand(5, 117);
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PONG_MISS, SoundCategory.BLOCKS, getVolume(), 1.0f);
				paddles[1].score++;
			} else if (boundingBox.intersects(outBoundingBox[1])) { // Out on right paddle, left gets point
				xV = -1;
				angle = (int)rand(0, 2);
				x = 116;
				y = (int)rand(5, 117);
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PONG_MISS, SoundCategory.BLOCKS, getVolume(), 1.0f);
				paddles[0].score++;
			}
			
			// Paddle Collision
			for (Paddle paddle : paddles) {
				if (boundingBox.intersects(paddle.boundingBox)) {
					xV = -xV;
					getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PONG_HIT, SoundCategory.BLOCKS, getVolume(), 1.0f);
					int intersection = (paddle.extendedY + PADDLE_Y) - extendedY; // Higher = TOP
					if (intersection > 14) { // Top
						if (intersection > (28 - 5)) {
							angle = 1;
						} else if (intersection < (14 + 5)) {
							angle = 2;
						} else xA = yA = 3;
					} else if (intersection < 14) { // Bottom
						if (intersection > (14 - 5)) {
							angle = 2;
						} else if (intersection < (14 - 9)) {
							angle = 1;
						} else angle = 0;
					}
				}
			}
			calculateAngle();
		}
		
		public void calculateAngle () {
			switch (angle) {
				case 0:
					xA = yA = 3;
				case 1:
					yA = 3;
					xA = 6;
					break;
				case 2:
					xA = 3;
					yA = 6;
					break;
			}
		}
	}
}
