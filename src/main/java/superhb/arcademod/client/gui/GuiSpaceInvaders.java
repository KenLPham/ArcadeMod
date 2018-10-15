package superhb.arcademod.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.audio.ArcadeSounds;
import superhb.arcademod.client.audio.LoopingSound;
import superhb.arcademod.client.tileentity.ArcadeLeaderboard;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.KeyHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class GuiSpaceInvaders extends GuiArcade {
	
	private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/spaceinvaders.png");
	
	// Texture Variables
	private static final int GUI_X = 234, GUI_Y = 284;
	private static final int MAZE_X = 224, MAZE_Y = 248;
	
	private static final int ALIEN = 14;
	private static final int TANK = 15;
	
	// Audio Variables
	private float volume = 1f; // TODO: Create volume slider in game settings (separate volume sliders for different sounds?)
	
	// Board Variables
	private int boardX, boardY;
	private int score;
	private Tile[][] tiles = new Tile[31][28]; // 28x31
	private byte level;
	private boolean gameOver, mazeBlink, nextLevel;
	private int mazeBlinkTick = 0;
	private int mazeBlinks = 0;
	private boolean playing, updatePos, allDead;
	private int startTick = 0;
	private int bonusTick, bonusTime;
	private boolean showBonus;
	private int backTick;
	private int activeBombs;
	private int ufoScore = 0;
	LoopingSound theme;
	int topScore;
	String topName;
	ArcadeLeaderboard[] leaderboard =  this.getTileEntity().getLeaderboard();
	
	// Character Variables
	private Player tank;
	private Alien[] aliens = new Alien[32];
	private Bullet bullet;
	private Bomb[] bombs = new Bomb[8];
	private UFO ufo;
	private Direction lastDirection, desiredDirection = Direction.LEFT;
	private int deathTick = 0, gameOverTick = 0;
	
	public GuiSpaceInvaders (World world, TileEntityArcade tile, @Nullable BlockPos pos, EntityPlayer player) {
		super(world, tile, pos, player);
		setGuiSize(GUI_X, GUI_Y, 0.8F);
		setTexture(texture, 512, 512);
		setCost(4);
		setOffset(0, 0);
		setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
		setStartMenu(0);
	}
	
	@Override
	public void updateScreen () {
		super.updateScreen();
		
		if (inMenu) {
			if (mc.getSoundHandler().isSoundPlaying(theme)) mc.getSoundHandler().stopSound(theme);
			if (menu == 3) {
				if ((tickCounter - backTick) == 60) {
					tickCounter = score = mazeBlinks = mazeBlinkTick = deathTick = gameOverTick = level = 0;
					checkMenuAfterGameOver();
				}
			}
		} else {
			if (playing) {
				if (theme == null) {
					theme = new LoopingSound(this.getTileEntity(), ArcadeSounds.SPACEINVADERS, SoundCategory.RECORDS, volume - 0.5f);
				}
				if (!mc.getSoundHandler().isSoundPlaying(theme)) {
					mc.getSoundHandler().playSound(theme);
				}
				// Tank Logic
				for (int i = 0; i < 3; i++) tank.move().updatePosition(boardX, boardY);
				tank.update();
				
				if (desiredDirection == Direction.DOWN) {
					for (int i = 0; i < aliens.length; i++) {
						if (aliens[i].isVisible) {
							aliens[i].ai().move().updatePosition(boardX, boardY);
						}
					}
					if (lastDirection == Direction.LEFT) {
						desiredDirection = Direction.RIGHT; 
						lastDirection = Direction.RIGHT;
					} else {
						desiredDirection = Direction.LEFT;
						lastDirection = Direction.LEFT;
					}
				} else {
					for (int i = 0; i < aliens.length; i++) {
						if (aliens[i].isVisible) aliens[i].ai().move().updatePosition(boardX, boardY);
					}
				}
				for (int i = 0; i < aliens.length; i++) {
					aliens[i].current = desiredDirection;
					aliens[i].update();
				}
				
				// Alien bombing logic
				Random random = new Random();
				activeBombs = 0;
				for (int i = 0; i < bombs.length; i++) {
					if (bombs[i].isVisible) activeBombs++;
				}
					
				for (int i=0;i<level;i++) {
					if (activeBombs < level) {
						int id = random.nextInt(32);
						if (aliens[id].isVisible) {
							for (int j=0;j<bombs.length;j++) {
								if (!bombs[j].isVisible) {
									bombs[j] = new Bomb(j, aliens[id].x, aliens[id].y, aliens[id].aliencolor, true);
									bombs[j].canMove = true;	
									activeBombs++;
									break;
								}	
							}
								
						}
					}
				}
				
				if (bullet != null) bullet.move().updatePosition(boardX, boardY);
				
				for (int i=0;i<bombs.length;i++) {
					if (bombs[i].isVisible) {
						bombs[i].move().updatePosition(boardX, boardY);
						bombs[i].update();
					}
				}
				
				if (score >= (ufoScore + 100)) {
					// Send a UFO
					ufoScore = score;
					if (ufo == null) {
						ufo = new UFO(0, 26, 4, new Color(100,random.nextInt(256),random.nextInt(256)),true);
						ufo.canMove = true;
					}
				}
				
				if (ufo != null) {
					ufo.move().updatePosition(boardX, boardY);
					if (ufo != null) ufo.update();
				}
				
				collisionDetection();
				
				allDead = true;
				for (int i = 0; i < aliens.length; i++) {
					if (aliens[i].isVisible) {
						allDead = false;
					}
				}
				
				// Level Logic
				if (allDead) {
					if (!nextLevel) {
						tank.canMove = false;
						for (int i = 0; i < aliens.length; i++) aliens[i].canMove = false;
						nextLevel = true;
						mazeBlinkTick = tickCounter;
					}
					if (((tickCounter - mazeBlinkTick) == 40 && mazeBlinks == 0) || ((tickCounter - mazeBlinkTick) == 5 && mazeBlinks < 5)) {
						mazeBlinkTick = tickCounter;
						if (!mazeBlink) mazeBlink = true;
						else mazeBlink = false;
						mazeBlinks++;
					}
					if (mazeBlinks == 5) { // Next Game Logic
						level++;
						mazeBlink = false;
						mazeBlinks = 0;
						allDead = false;
						tickCounter = deathTick = gameOverTick = 0;
						setupTiles();
						setupGame();
					}
				}
				
				if (showBonus && (tickCounter - bonusTick) == bonusTime) showBonus = false;
			} else {
				if ((tickCounter - startTick) == 35) {
					playing = true;
					tank.canMove = true;
					for (int i = 0; i < aliens.length; i++) aliens[i].canMove = true;
				}
				if (!updatePos) {
					updatePos = true;
					tank.updatePosition(boardX, boardY);
					for (int i = 0; i < aliens.length; i++) aliens[i].updatePosition(boardX, boardY);
				}
			}
		}
	}
	
	@Override
	public void drawScreen (int mouseX, int mouseY, float partialTick) {
		boardX = xScaled - (GUI_X / 2) + 5;
		boardY = yScaled - (GUI_Y / 2) + 14;
		super.drawScreen(mouseX, mouseY, partialTick);
		
		int controlWidth = this.fontRenderer.getStringWidth(I18n.format("option.arcademod:control.locale"));
		int settingWidth = this.fontRenderer.getStringWidth(I18n.format("option.arcademod:setting.locale"));
		
		if (inMenu) {
			switch (menu) {
				case 0: // Main Menu
					int titleWidth = this.fontRenderer.getStringWidth(I18n.format("game.arcademod:spaceinvaders.name"));
					int startWidth = this.fontRenderer.getStringWidth(I18n.format("option.arcademod:start.locale"));
					
					this.fontRenderer.drawString(I18n.format("game.arcademod:spaceinvaders.name"), boardX + (GUI_X / 2) - (titleWidth / 2), boardY + 2, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:start.locale"), boardX + (GUI_X / 2) - (startWidth / 2), boardY + (GUI_Y / 2) - 30, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:control.locale"), boardX + (GUI_X / 2) - (controlWidth / 2), boardY + (GUI_Y / 2) - 20, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:setting.locale"), boardX + (GUI_X / 2) - (settingWidth / 2), boardY + (GUI_Y / 2) - 10, Color.WHITE.getRGB());
					
					if (menuOption == 0) drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 32, true); // Start
					else if (menuOption == 1) drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 22, true); // Controls
					else if (menuOption == 2) drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 12, true); // Settings
					break;
				case 1: // Controls
					this.fontRenderer.drawString(I18n.format("option.arcademod:control.locale"), boardX + (GUI_X / 2) - (controlWidth / 2), boardY + 2, Color.WHITE.getRGB());
					
					// Controls
					this.fontRenderer.drawString("[" + KeyHandler.up.getDisplayName() + "] " + I18n.format("control.arcademod:up.spaceinvaders.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 20, Color.WHITE.getRGB());
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("control.arcademod:left.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2), Color.WHITE.getRGB());
					this.fontRenderer.drawString("[" + KeyHandler.right.getDisplayName() + "] " + I18n.format("control.arcademod:right.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) + 10, Color.WHITE.getRGB());
					
					// Back
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), boardX + 2, boardY + (GUI_Y) - 30, Color.white.getRGB());
					break;
				case 2: // Settings
					// Back
					this.fontRenderer.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), boardX + 2, boardY + (GUI_Y) - 30, Color.white.getRGB());
					break;
				case 3: // Game Over
					this.fontRenderer.drawString(I18n.format("text.arcademod:gameover.locale"), boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("text.arcademod:gameover.locale")) / 2), boardY + (GUI_Y / 2) - 20, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("text.arcademod:score.locale") + ": " + score, boardX + (GUI_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("text.arcademod:score.locale=Score") + ": " + score)), boardY + (GUI_Y / 2), Color.WHITE.getRGB());
					/*if (score > topScore) {
						if (leaderboard[0] == null) {
							leaderboard[0] = new ArcadeLeaderboard(getPlayer().getName(), score);
							this.getTileEntity().saveLeaderboard(leaderboard);
						
						} else {
							leaderboard[0].setPlayerName(getPlayer().getName());
							leaderboard[0].setScore(score);
						}
						this.getTileEntity().setTopName(getPlayer().getName());
						this.getTileEntity().setTopScore(score);
						this.getTileEntity().readFromNBT(compound);
						TileEntityArcade machine = (TileEntityArcade) this.getWorld().getTileEntity(this.getPos());
						machine.saveLeaderboard(leaderboard);
					}*/
					break;
			}
		} else {
			drawMaze();
			
			for (int y = 0; y < 31; y++) {
				for (int x = 0; x < 28; x++) {
					if (tiles[y][x] != null) tiles[y][x].updatePosition(boardX, boardY).drawTile();
				}
			}
			
			// Aliens
			if (playing) {
				for (int i = 0; i < aliens.length; i++) {
					if (aliens[i].isVisible) aliens[i].drawAlien();
				}
			}
			
			// Tank (Player)
			tank.drawPlayer().drawLives();
			
			// Bullet
			if (bullet != null) bullet.drawBullet();
			
			// Bombs
			for (int i=0;i<bombs.length;i++) {
				if (bombs[i].isVisible) bombs[i].drawBomb();
			}
			
			// UFO
			if (ufo != null) ufo.drawUFO();
			
			// Text
			if (!playing) this.fontRenderer.drawString(I18n.format("text.arcademod:ready.spaceinvaders.locale"), boardX + (MAZE_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("text.arcademod:ready.spaceinvaders.locale")) / 2), boardY + (MAZE_Y / 2) + 13, Color.yellow.getRGB());
			
			this.fontRenderer.drawString(String.format(I18n.format("text.arcademod:score.locale") + " %d", score), boardX + 10, boardY + 6, Color.white.getRGB());
			//this.fontRenderer.drawString(String.format(I18n.format("text.arcademod:topscore.locale") + " %s %d", topName, topScore), boardX + 45, boardY - 8, Color.white.getRGB());
			this.fontRenderer.drawString(String.format(I18n.format("text.arcademod:level.locale") + " %d", level), boardX + 170, boardY + 6, Color.white.getRGB());
		}
	}
		
	private void drawMaze () {
		if (mazeBlink) glColor(Color.WHITE);
		else glColor(new Color(33, 33, 222));
		drawModalRectWithCustomSizedTexture(boardX, boardY, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);
	}
	
	private void collisionDetection () {
		for (int i = 3; i >= 0; i--) {
			for (int j = 0; j < 8; j++) {
				int id = i * 8 + j;
				if (bullet != null) {
					if (bullet.extendedX >= (aliens[id].extendedX - 7) &&
							bullet.extendedX <= (aliens[id].extendedX + 7) &&
							bullet.extendedY >= (aliens[id].extendedY - 7) &&
							bullet.extendedY <= (aliens[id].extendedY + 7) &&
							aliens[id].isVisible) {
						aliens[id].isVisible = false;
						bullet = null;
						score += 10;
						getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.SPACEINVADERS_DESTROYED, SoundCategory.BLOCKS, volume - 0.2f, 1.0f);
					}
				}
			}
		}
		for(int i = 0; i < bombs.length; i++) {
			if (bombs[i].isVisible) {
				if (bombs[i].extendedX >= (tank.extendedX - 7) &&
					bombs[i].extendedX <= (tank.extendedX + 7) &&
					bombs[i].extendedY + 7 >= (tank.extendedY - 7) &&
					bombs[i].extendedY <= (tank.extendedY + 7)) {
					endGame();
				}
			}
		}
		
		// Check if UFO gets killed
		if (bullet != null && ufo != null) {
			if (bullet.extendedX >= (ufo.extendedX - 7) &&
					bullet.extendedX <= (ufo.extendedX + 7) &&
					bullet.extendedY >= (ufo.extendedY - 7) &&
					bullet.extendedY <= (ufo.extendedY + 7) &&
					ufo.isVisible) {
				bullet = null;
				ufo = null;
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.SPACEINVADERS_DESTROYED, SoundCategory.BLOCKS, volume - 0.2f, 1.0f);
				score += 50;
			}
		}
	}
	
	private void endGame () {
		if (!gameOver) {
			gameOver = true;
			tank.canMove = false;
			for (int i = 0; i < aliens.length; i++) aliens[i].canMove = false;
			tank.kill();
		}
		this.fontRenderer.drawString(I18n.format("text.arcademod:gameover.locale"), boardX + (MAZE_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("text.arcademod:gameover.locale")) / 2), boardY + (MAZE_Y / 2) + 13, Color.red.getRGB());
	}
	  	        	    	
	private void setupGame () {
		/*if (leaderboard[0] != null) {
			topName = leaderboard[0].getPlayerName();
			topScore = leaderboard[0].getScore();
		}
		else
		{
			leaderboard[0] = new ArcadeLeaderboard("", 0);
			topName = "";
			topScore = 0;
		}*/
		resetGame();
		
		if (gameOver) {
			tank.reset();
			tickCounter = deathTick = gameOverTick = 0;
			gameOver = false;
		}
		startTick = tickCounter;
		playing = false;
	}
	
	private void respawn () {
		if (gameOver) {
			tank.reset();
			tickCounter = deathTick = gameOverTick = 0;
			gameOver = false;
		}
		startTick = tickCounter;
		playing = false;
		activeBombs = 0;
	}
	
	private void resetGame () {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 8; j++) {
				int id = i * 8 + j;
				if (i == 0) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(29, 226, 255), true);
				if (i == 1) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(30, 180, 50), true);
				if (i == 2) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(255, 180, 0), true);
				if (i == 3) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(180, 30, 180), true);
			}
		}
		allDead = false;
		nextLevel = false;
		for (int i = 0; i < bombs.length; i++) bombs[i] = new Bomb(i, 1, 1, new Color(0,0,0),false);
		activeBombs = 0;
	}
	
	private void startGame () {
		score = 0;
		level = 1;
		inMenu = false;
		tank = new Player();
		canGetCoinBack = false;
		
		setupTiles();
		setupGame();
	}
	
	@Override
	protected void keyTyped (char typedChar, int keyCode) throws IOException {
		if (inMenu) {
			if (menu == 0) {
				if (keyCode == KeyHandler.down.getKeyCode()) {
					if (menuOption == 2) menuOption = 0;
					else menuOption++;
				}
				if (keyCode == KeyHandler.up.getKeyCode()) {
					if (menuOption == 0) menuOption = 2;
					else menuOption--;
				}
				if (keyCode == KeyHandler.select.getKeyCode()) {
					if (menuOption == 0) startGame();
					else menu = menuOption;
				}
			}
			if (keyCode == KeyHandler.left.getKeyCode())
				menu = 0;
		} else {
			if (keyCode == KeyHandler.left.getKeyCode()) tank.current = Direction.LEFT;
			else if (keyCode == KeyHandler.right.getKeyCode()) tank.current = Direction.RIGHT;
			else if (keyCode == KeyHandler.up.getKeyCode()) {
				if (bullet == null) {
					bullet = new Bullet(0, tank.x, tank.y, new Color(255,0,0), true);
					bullet.canMove = true;
					getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.SPACEINVADERS_SHOOT, SoundCategory.BLOCKS, volume, 1.0f);
				}
			}
			if (keyCode == 1) giveReward(ArcadeItems.TICKET, (score / 200)); // Esc
		}
		super.keyTyped(typedChar, keyCode);
	}
	
	private enum Direction {
		STAND(0, 0, 0),
		UP(1, 2, 1),
		DOWN(2, 1, 1),
		LEFT(3, 4, 2),
		RIGHT(4, 3, 2);
		
		private int direction;
		private int opposite;
		
		Direction (int direction, int opposite, int axis) {
			this.direction = direction;
			this.opposite = opposite;
		}
	}
	
	private enum EnumTile {
		PLAY(0, Color.GREEN),
		WALL(1, Color.RED),
		TELE(2, Color.YELLOW),
		TELE_ZONE(3, Color.ORANGE),
		GHOST_ONLY(4, Color.WHITE),
		GHOST_LIMIT(5, Color.MAGENTA);
		
		private int id;
		private Color color;
		
		EnumTile (int id, Color color) {
			this.id = id;
			this.color = color;
		}
	}
	
	private class Tile {
		int x, y, extendedX, extendedY;
		int edible = 0; // 0 = none;
		EnumTile type;
		
		Tile (int x, int y, EnumTile type) {
			this(x, y, type, 0);
		}
		
		Tile (int x, int y, EnumTile type, int edible) {
			this.x = x;
			this.y = y;
			this.extendedX = x * 8 + boardX;
			this.extendedY = y * 8 + boardY;
			this.type = type;
			this.edible = edible;
		}
		
		Tile drawTile () {
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			return this;
		}
		
		Tile updatePosition (int x, int y) {
			extendedX = this.x * 8 + x;
			extendedY = this.y * 8 + y;
			
			return this;
		}
	}
	
	private class Player extends Mover {
		int lives = 3, deathAnimation;
		boolean playDeathAnimation;
		
		int STATE = 0;
		
		int prevX, prevY;
		
		public Player () {
			super(14, 29);
			
			prevX = 14;
			prevY = 29;
			
			current = Direction.LEFT;
		}
		
		private Mover move () {
			if (canMove) {
				switch (current) {
					case LEFT:
						if (!isBlockedLeft()) {
							if (isKeyDown(KeyHandler.left.getKeyCode())) {
								moveX -= getSpeed();
							}
						}
						else current = Direction.STAND;
						return this;
					case RIGHT:
						if (!isBlockedRight()) {
							if (isKeyDown(KeyHandler.right.getKeyCode())) {
								moveX += getSpeed();
							}
						}
						else current = Direction.STAND;
						return this;
					case UP:
						return this;
					case DOWN:
						return this;
					case STAND:
						return this;
				}
			}
			return this;
		}
		
		@Override
		public void update () {
			// Death Logic
			if (playDeathAnimation) {
				if (deathAnimation <= 12) { // TODO: offset death animation?
					if ((tickCounter - deathTick) == 2) {
						deathTick = tickCounter;
						deathAnimation++;
					}
				}
				if (deathAnimation >= 12 && gameOverTick == 0) {
					moveX = moveY = 0;
					setStartPos(14, 29);
					prevX = 14;
					prevY = 29;
					gameOverTick = tickCounter;
				}
				if ((tickCounter - gameOverTick) == 20) {
					if (lives == 0) { // Go back to main menu logic
						giveReward(ArcadeItems.TICKET, (score / 200));
						inMenu = true;
						menu = 3;
						playDeathAnimation = false;
						backTick = tickCounter;
						gameOver = mazeBlink = nextLevel = updatePos = false;
					} else {
						lives--;
						respawn();
						deathAnimation = 0;
						playDeathAnimation = false;
						showBonus = false;
						bonusTick = 0;
					}
				}
			}
		}
		
		public void kill () {
			playDeathAnimation = true;
			deathTick = tickCounter;
			getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.SPACEINVADERS_EXPLODE, SoundCategory.BLOCKS, volume, 1.0f);
		}
		
		private void reset () {
			setStartPos(14, 29);
			
			prevX = 14;
			prevY = 29;
			moveX = moveY = 0;
			current = Direction.LEFT;
		}
			
		private float getSpeed () {
			if (level == 0) { // Level 1
				return 0.8f;
			}
			if (level >= 1 && level <= 3) { // Level 2-4
				return 0.9f; // Regular Speed
			}
			if (level >= 4 && level <= 19) { // Level 5-20
				return 1; // Regular Speed
			}
			if (level >= 20) { // Level 21+
				return 0.9f; // Regular Speed
			}
			return 0.8f;
		}
		
		@Override
		public Player updatePosition (int x, int y) {
			super.updatePosition(x, y);
			
			// Animation
			if ((this.x != prevX) || (this.y != prevY)) {
				prevX = this.x;
				prevY = this.y;
				if (STATE == 0) STATE = 1;
				else STATE = 0;
			}
			return this;
		}
		
		private Player drawPlayer () {
			if (!gameOver) {
				GlStateManager.color(1.0F, 1.0F, 1.0F);
				switch (current) {
					case STAND:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
						return this;
					case LEFT:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, TANK * 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
						return this;
					case RIGHT:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, TANK * 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
						return this;
					case DOWN:
						return this;
					case UP:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, TANK * 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
						return this;
				}
			} else {
				glColor(Color.WHITE);
				if (playDeathAnimation && deathAnimation <= 12) {
					drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, TANK * deathAnimation, GUI_Y + (TANK * 2) + (300 - GUI_Y), TANK, TANK, 512, 512);
				}
			}
			return this;
		}
		
		private Player drawLives () {
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			for (int i = 0; i < lives; i++) {
				drawModalRectWithCustomSizedTexture(offsetX + (i * 14), offsetY + 248, TANK * 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
			}
			return this;
		}
	}
	
	private class Alien extends Mover {
		private int alienid;
		private Color aliencolor;
		private boolean isVisible;
		int BODY_STATE = 0;
		
		public Alien (int id, int startx, int starty, Color color, boolean isVisible) {
			super(startx,starty);
			alienid = id;
			aliencolor = color;
			this.isVisible = isVisible;
			current = Direction.LEFT;
		}
		
		private Mover move () {
			// Movement Logic
			if (canMove) {
				switch (current) {
					case LEFT:
						if (!isBlockedLeft()) moveX -= getSpeed();
						return this;
					case RIGHT:
						if (!isBlockedRight()) moveX += getSpeed();
						return this;
					case UP:
						if (!isBlocked()) moveY -= getSpeed();
						return this;
					case DOWN:
						if (!isBlockedDown()) moveY += 3;
						return this;
					case STAND:
						return this;
				}
			}
			return this;
		}
		
		private float getSpeed () {
			if (level == 1 || level == 2) { // Level 1-2
				return 0.75f;
			}
			if (level == 3 || level == 4) { // Level 2-3
				return 1.0f;
			}
			if (level >= 5) { // Level 5+
				return 2.0f;
			}
			return 0.75f;
		}
		
		@Override
		public void update () {
			// Animation
			if ((extendedX - offsetX) % 4 == 0) {
				if (BODY_STATE == 0) BODY_STATE = 1;
				else BODY_STATE = 0;
			}
		}
				
		private Alien ai () {
			if (current == Direction.LEFT) { // Can't Move right
				if (isBlockedLeft(x - 1, y)) { // is blocked left
					desiredDirection = Direction.DOWN;
				}
			}
			if (current == Direction.RIGHT) { // Can't Move right
				if (isBlockedRight(x + 1, y)) { // is blocked left
					desiredDirection = Direction.DOWN;
				}
			}
			if (current == Direction.DOWN) {
				if (isBlockedDown(x, y + 2)) { // is blocked down
						// Game Over is Alien is alive and reaches the ground
					if (isVisible) {
						tank.lives = 0;
						endGame();
					}
				}
			}
			return this;
		}
				
		private boolean isBlockedDown (int x, int y) {
			return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
		}
		
		private boolean isBlockedLeft (int x, int y) {
			return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL;
		}
		
		private boolean isBlockedRight (int x, int y) {
			return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL;
		}
		
		@Override
		public boolean isBlockedDown () {
			return isBlockedDown(x, y);
		}
		
		private Alien drawAlien () {
			if (!isVisible) {
				return this;
			}
			glColor(aliencolor);
			int startX = GUI_X + 10;
			
			if (alienid < 32) startX = GUI_X + 10;
			if (alienid < 24) startX = GUI_X + 10 + (ALIEN * 6);
			if (alienid < 16) startX = GUI_X + 10 + (ALIEN * 4);
			if (alienid < 8) startX = GUI_X + 10 + (ALIEN * 2);
			if (BODY_STATE == 0) drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, startX, MAZE_Y, ALIEN, ALIEN, 512, 512);
			else drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, startX + ALIEN, MAZE_Y, ALIEN, ALIEN, 512, 512);
			return this;
		}
	}
	
	private class Bullet extends Mover {
		private int id;
		private Color color;
		private boolean isVisible;
		int BODY_STATE = 0;
		
		public Bullet (int id, int startx, int starty, Color color, boolean isVisible) {
			super(startx,starty);
			this.id = id;
			this.color = color;
			this.isVisible = isVisible;
		}

		private Mover move () {
			// Movement Logic
			if (canMove) {
				if (!isBlocked(x,y - 4)) {
					if (isVisible) {
						moveY -= getSpeed();
					}
				} else {
					bullet = null;
					return this;
				}
				return this;
			}
			return this;
		}
		
		private float getSpeed () {
			return 5.0f;
		}
		
		@Override
		public void update () {
			// Animation
			if ((extendedX - offsetX) % 4 == 0) {
				if (BODY_STATE == 0) BODY_STATE = 1;
				else BODY_STATE = 0;
			}
		}
		
		private boolean isBlocked (int x, int y) {
			return tiles[Math.max(0, y - 1)][Math.max(0, x)].type == EnumTile.WALL;
		}
		
		private boolean isBlockedDown (int x, int y) {
			return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
		}
		
		@Override
		public boolean isBlockedDown () {
			return isBlockedDown(x, y);
		}
		
		private Bullet drawBullet () {
			if (!isVisible) {
				return this;
			}
			glColor(color);
			drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, 30, 301, 14, 4, 512, 512);		
			return this;
		}
	}
	
	private class Bomb extends Mover {
		int pauseTick;
		private int id;
		private Color color;
		private boolean isVisible;
		int BODY_STATE = 0;
		
		public Bomb (int id, int startx, int starty, Color color, boolean isVisible) {
			super(startx,starty);
			this.id = id;
			this.color = color;
			this.isVisible = isVisible;
		}

		private Mover move () {
			// Movement Logic
			if (canMove) {
				if (!isBlockedDown(x, y + 1)) {
					if (isVisible) moveY += getSpeed();
				} else {
					canMove = false;
					isVisible = false;
					return this;
				}
				return this;
			}
			return this;
		}
		
		private float getSpeed () {
			return 4.0f;
		}
		
		@Override
		public void update () {
			// Animation
			if ((extendedY - offsetY) % 4 == 0) {
				if (BODY_STATE == 0) BODY_STATE = 1;
				else BODY_STATE = 0;
			}
		}
		
		private boolean isBlockedDown (int x, int y) {
			return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
		}
		
		@Override
		public boolean isBlockedDown () {
			return isBlockedDown(x, y);
		}
		
		private Bomb drawBomb () {
			if (!isVisible) return this;
			glColor(color);
			// Body
			if (BODY_STATE == 0) drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, 240, 270, 14, 14, 512, 512);
			else drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, 257, 270, 14, 14, 512, 512);
		
			return this;
		}
	}
	
	private class UFO extends Mover {
		int pauseTick;
		private int id;
		private Color color;
		private boolean isVisible;
		int BODY_STATE = 0;
		
		public UFO (int id, int startx, int starty, Color color, boolean isVisible) {
			super(startx,starty);
			this.id = id;
			this.color = color;
			this.isVisible = isVisible;
		}

		private Mover move () {
			// Movement Logic
			if (canMove) {
				if (!isBlockedDown(x - 1, y)) {
					if (isVisible) moveX -= getSpeed();
				} else {
					canMove = false;
					isVisible = false;
					ufo = null;
					return this;
				}
				return this;
			}
			return this;
		}
		
		private float getSpeed () {
			return 2.0f;
		}
		
		@Override
		public void update () {
			// Animation
			if ((extendedY - offsetY) % 4 == 0) {
				if (BODY_STATE == 0) BODY_STATE = 1;
				else BODY_STATE = 0;
			}
		}
		
		/*private boolean isBlocked (int x, int y) {
			return tiles[Math.max(0, y - 1)][Math.max(0, x)].type == EnumTile.WALL;
		}*/
		
		private boolean isBlockedDown (int x, int y) {
			return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
		}
		
		@Override
		public boolean isBlockedDown () {
			return isBlockedDown(x, y);
		}
		
		private UFO drawUFO () {
			if (!isVisible) return this;
			glColor(color);
			// Body
			int startX = GUI_X + 10 + (ALIEN * 8);
			if (BODY_STATE == 0) drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, startX, MAZE_Y, ALIEN, ALIEN, 512, 512);
			else {
				glColor(color.brighter());
				drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, startX, MAZE_Y, ALIEN, ALIEN, 512, 512);
			}
		
			return this;
		}
	}
	
	private class Mover {
		Direction current;
		
		// Position
		int x, y;
		int x1, y1;
		float moveX, moveY;
		int extendedX, extendedY;
		int offsetX, offsetY;
		
		public boolean canMove;
		
		private Mover (int x, int y) {
			setStartPos(x, y);
		}
		
		public Mover updatePosition (int x, int y) {
			extendedX = (int)(x1 + x + moveX);
			extendedY = (int)(y1 + y + moveY);
			
			offsetX = x;
			offsetY = y;
			
			if ((extendedX - x) % 8 == 0) this.x = (extendedX - x) / 8;
			if ((extendedY - y) % 8 == 0) this.y = (extendedY - y) / 8;
			return this;
		}
		
		public void setStartPos (int x, int y) {
			this.x1 = (x * 8) - 4;
			this.y1 = y * 8;
			this.x = x;
			this.y = y;
		}
		public boolean isBlocked () {
			return tiles[Math.max(0, y - 1)][x].type == EnumTile.WALL;
		}
		
		public boolean isBlockedDown () {
			return tiles[y + 1][x].type == EnumTile.WALL;
		}
		
		boolean isBlockedLeft () {
			return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL;
		}
		
		boolean isBlockedRight () {
			return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL;
		}
		
		/*boolean onTile () {
			return (extendedX - offsetX) % 8 == 0 && (extendedY - offsetY) % 8 == 0;
		}*/
		
		/*EnumTile checkTile () {
			return tiles[y][x].type;
		}*/
		
		/*public Point getPosition () {
			return new Point(x, y);
		}
		
		public Tile getTile () {
			return tiles[y][x];
		}*/
		
		/**
		 * Called every tick
		 */
		public void update () {}
	}
	
	private void setupTiles () {
		// Row 0
		for (int i = 0; i < 28; i++) tiles[0][i] = new Tile(i, 0, EnumTile.WALL);
		// Row 1 - 29
		for (int j = 1; j <= 29; j++) {
			for (int i = 0; i < 28; i++) {
				if (i == 0 || i == 27) tiles[j][i] = new Tile(i, j, EnumTile.WALL);
				else tiles[j][i] = new Tile(i, j,EnumTile.PLAY);
			}
		}
		// Row 30
		for (int i = 0; i < 28; i++) tiles[30][i] = new Tile(i, 30, EnumTile.WALL);
	}
}
