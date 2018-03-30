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
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.KeyHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class GuiPacMan extends GuiArcade {
	// 1 dot = 10 pt (240 total)
	// 1 energizer = 50 pt (4 total)
	// Captured Ghost: 1 = 200; 2 = 400; 3 = 800; 4 = 1600
	// Blue time decreases as level goes up
	// Level 19, Ghosts can no longer be eaten
    /* Audio Info
        - Siren plays continuously (unless ghosts are frightened)
        - Waka only plays when eating dots / energizer
        - Frightened Sound plays with ghosts are frightened
     */
	
	private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/pacman.png");
	
	// Texture Variables
	private static final int GUI_X = 234, GUI_Y = 284;
	private static final int MAZE_X = 224, MAZE_Y = 248;
	
	private static final int GHOST = 14;
	private static final int PUPIL = 2;
	private static final int EYE_X = 4, EYE_Y = 5;
	private static final int MOUTH_X = 12, MOUTH_Y = 2;
	private static final int BONUS = 14;
	private static final int PACMAN = 15;
	private static final int DOT = 2;
	private static final int ENERGIZER = 8;
	
	// Audio Variables
	private float volume = 1f; // TODO: Create volume slider in game settings (separate volume sliders for different sounds?)
	private int waka;
	private boolean playSiren = true;
	
	// ==Audio==
	// Siren is played normally. When Energizer is eaten fast siren is
	// played and normal siren stops. When ghost is eaten, even faster
	// siren is played (stops when ghost is not longer eaten or when
	// ghosts aren't scared anymore)
	
	// Board Variables
	private int boardX, boardY;
	private int score;
	private Tile[][] tiles = new Tile[31][28]; // 28x31
	private int ENERGIZER_STATE = 0;
	private byte level;
	private boolean gameOver, mazeBlink, nextLevel;
	private int mazeBlinkTick = 0;
	private int mazeBlinks = 0;
	private boolean playing, updatePos;
	private int startTick = 0;
	private EnumBonus bonus;
	private int bonusTick, bonusTime;
	private boolean showBonus;
	private int backTick;
	
	// Character Variables
	private Player pacman;
	private Ghost[] ghosts = new Ghost[4];
	private int deathTick = 0, gameOverTick = 0;
	private int energizerTick = 0, scatterTick = 0;//, scaredTick = 0;
	private int scaredTime = 0, scaredFlash = 0;
	private ArrayList<Integer> houseQueue = new ArrayList<Integer>();
	private boolean useGlobalCounter;
	private int globalCounter; // Global dot counter
	private int dotTick, dotTimeLimit = 80; // Releases ghost if pacman doesnt eat
	
	public GuiPacMan (World world, TileEntityArcade tile, @Nullable BlockPos pos, EntityPlayer player) {
		super(world, tile, pos, player);
		setGuiSize(GUI_X, GUI_Y, 0.8F);
		setTexture(texture, 512, 512);
		setCost(4);
		setOffset(0, 0);
		setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
		setStartMenu(0);
	}
	
	// TODO: Move game logic here (gl with that)
	@Override
	public void updateScreen () {
		super.updateScreen();
		
		if (inMenu) {
			if (menu == 3) {
				if ((tickCounter - backTick) == 60) {
					tickCounter = score = ENERGIZER_STATE = mazeBlinks = mazeBlinkTick = deathTick = gameOverTick = energizerTick = scatterTick = scaredTime = scaredFlash = level = 0;
					checkMenuAfterGameOver();
				}
			}
		} else {
			if (playing) {
				// Pac-Man Logic
				for (int i = 0; i < 3; i++) pacman.move().updatePosition(boardX, boardY);
				pacman.update();
				
				// Ghost Logic
				for (int i = 0; i < ghosts.length; i++) {
					for (int j = 0; j < (ghosts[i].eaten ? 6 : 3); j++) ghosts[i].ai().move().updatePosition(boardX, boardY);
					ghosts[i].update();
				}
				
				collisionDetection();
				
				// Edibles
				if ((tickCounter - energizerTick) >= 10) {
					energizerTick = tickCounter;
					
					if (ENERGIZER_STATE == 0) ENERGIZER_STATE = 1;
					else ENERGIZER_STATE = 0;
				}
				
				// Level Logic
				if (pacman.foodEaten == (244 + (244 * level))) {
					if (!nextLevel) {
						pacman.canMove = false;
						for (int i = 0; i < ghosts.length; i++) ghosts[i].canMove = false;
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
						waka = mazeBlinks = 0;
						tickCounter = deathTick = energizerTick = scatterTick = gameOverTick = mazeBlinkTick = 0;
						pacman.reset();
						setupTiles();
						setupGame();
					}
				}
				
				// Bonus Logic
				if (pacman.foodEaten == 70 || pacman.foodEaten == 170) {
					bonusTime = (int)(rand(9, 10) * 20);
					bonusTick = tickCounter;
					showBonus = true;
				}
				
				if (showBonus && (tickCounter - bonusTick) == bonusTime) showBonus = false;
			} else {
				if ((tickCounter - startTick) == 35) {
					playing = true;
					pacman.canMove = true;
					for (int i = 0; i < ghosts.length; i++) ghosts[i].canMove = true;
					dotTick = tickCounter;
				}
				if (!updatePos) {
					updatePos = true;
					pacman.updatePosition(boardX, boardY);
					for (int i = 0; i < ghosts.length; i++) ghosts[i].updatePosition(boardX, boardY);
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
					int titleWidth = this.fontRenderer.getStringWidth(I18n.format("game.arcademod:pacman.name"));
					int startWidth = this.fontRenderer.getStringWidth(I18n.format("option.arcademod:start.locale"));
					
					this.fontRenderer.drawString(I18n.format("game.arcademod:pacman.name"), boardX + (GUI_X / 2) - (titleWidth / 2), boardY + 2, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:start.locale"), boardX + (GUI_X / 2) - (startWidth / 2), boardY + (GUI_Y / 2) - 30, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:control.locale"), boardX + (GUI_X / 2) - (controlWidth / 2), boardY + (GUI_Y / 2) - 20, Color.WHITE.getRGB());
					this.fontRenderer.drawString(I18n.format("option.arcademod:setting.locale"), boardX + (GUI_X / 2) - (settingWidth / 2), boardY + (GUI_Y / 2) - 10, Color.WHITE.getRGB());
					
					if (menuOption == 0)
						drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 32, true); // Start
					else if (menuOption == 1)
						drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 22, true); // Controls
					else if (menuOption == 2)
						drawRightArrow(boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 12, true); // Settings
					break;
				case 1: // Controls
					this.fontRenderer.drawString(I18n.format("option.arcademod:control.locale"), boardX + (GUI_X / 2) - (controlWidth / 2), boardY + 2, Color.WHITE.getRGB());
					
					// Controls
					this.fontRenderer.drawString("[" + KeyHandler.up.getDisplayName() + "] " + I18n.format("control.arcademod:up.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 20, Color.WHITE.getRGB());
					this.fontRenderer.drawString("[" + KeyHandler.down.getDisplayName() + "] " + I18n.format("control.arcademod:down.name"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 10, Color.WHITE.getRGB());
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
					break;
			}
		} else {
			//getTileEntity().playSound(ArcadeSounds.PACMAN_SIREN, true);
			
			drawMaze();
			
			for (int y = 0; y < 31; y++) {
				for (int x = 0; x < 28; x++) {
					if (tiles[y][x] != null) tiles[y][x].updatePosition(boardX, boardY).drawTile();
				}
			}
			
			// Ghosts
			for (int i = 0; i < ghosts.length; i++) ghosts[i].drawGhost();
			
			// Pac-Man
			pacman.drawPlayer().drawLives();
			
			// Bonus
			drawBonus();
			
			// Text
			if (!playing) this.fontRenderer.drawString(I18n.format("text.arcademod:ready.pacman.locale"), boardX + (MAZE_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("text.arcademod:ready.pacman.locale")) / 2), boardY + (MAZE_Y / 2) + 13, Color.yellow.getRGB());
			
			this.fontRenderer.drawString(String.format("%d", score), boardX + 30, boardY - 8, Color.white.getRGB());
		}
	}
	
	// First one shows up after 70 dots eaten
	// Second one shows up after 170 dots eaten
	// Stays on screen between 9-10 seconds
	private void drawBonus () {
		if (showBonus) {
			glColor(Color.WHITE);
			drawModalRectWithCustomSizedTexture(boardX + (13 * 8) + 2, boardY + (17 * 8) - 3, GUI_X + ENERGIZER + DOT + (GHOST * 2) + (BONUS * bonus.getId()), MAZE_Y, BONUS, BONUS, 512, 512);
		}
	}
	
	private void drawMaze () {
		if (mazeBlink) glColor(Color.WHITE);
		else glColor(new Color(33, 33, 222));
		drawModalRectWithCustomSizedTexture(boardX, boardY, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);
		glColor(Color.WHITE);
		drawModalRectWithCustomSizedTexture(boardX + 104, boardY + 101, GUI_X + 8, 264, 16, 2, 512, 512);
	}
	
	private void collisionDetection () {
		for (int i = 0; i < ghosts.length; i++) {
			if ((pacman.getTile() == ghosts[i].getTile()) && !ghosts[i].scared && !ghosts[i].eaten) endGame();
		}
	}
	
	private void pause () {
		pacman.canMove = false;
		for (int i = 0; i < ghosts.length; i++) ghosts[i].canMove = false;
	}
	
	private void unpause () {
		pacman.canMove = true;
		for (int i = 0; i < ghosts.length; i++) ghosts[i].canMove = true;
	}
	
	private void endGame () {
		// TODO: play game over sound
		if (!gameOver) {
			gameOver = true;
			pacman.canMove = false;
			for (int i = 0; i < ghosts.length; i++) ghosts[i].canMove = false;
			//getTileEntity().playSound(ArcadeSounds.PACMAN_DEATH, false);
			pacman.kill();
		}
		this.fontRenderer.drawString(I18n.format("text.arcademod:gameover.locale"), boardX + (MAZE_X / 2) - (this.fontRenderer.getStringWidth(I18n.format("text.arcademod:gameover.locale")) / 2), boardY + (MAZE_Y / 2) + 13, Color.red.getRGB());
	}
	
	private void setupGame () {
		// TODO: Sound
		//getTileEntity().playSound(ArcadeSounds.PACMAN_SIREN, true);
		getLevelData();
		resetGame();
		
		if (gameOver) {
			pacman.reset();
			waka = 0;
			tickCounter = deathTick = energizerTick = scatterTick = gameOverTick = 0;
			gameOver = false;
		}
		startTick = tickCounter;
		playing = false;
	}
	
	private void resetGame () {
		ghosts[0] = new Ghost(EnumGhost.BLINKY); // TODO: Elroy
		ghosts[1] = new Ghost(EnumGhost.PINKY);
		ghosts[2] = new Ghost(EnumGhost.INKY);
		ghosts[3] = new Ghost(EnumGhost.CLYDE);
	}
	
	private void startGame () {
		score = 0;
		level = 0;
		inMenu = false;
		pacman = new Player();
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
				menu = 0; // This will have to change when volume slider is made
		} else {
			if (keyCode == KeyHandler.left.getKeyCode()) pacman.desired = Direction.LEFT;
			else if (keyCode == KeyHandler.right.getKeyCode()) pacman.desired = Direction.RIGHT;
			else if (keyCode == KeyHandler.down.getKeyCode()) pacman.desired = Direction.DOWN;
			else if (keyCode == KeyHandler.up.getKeyCode()) pacman.desired = Direction.UP;
			
			if (keyCode == 1) giveReward(ArcadeItems.TICKET, (score / 80)); // Esc
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
		private int axis;
		
		Direction (int direction, int opposite, int axis) {
			this.direction = direction;
			this.opposite = opposite;
			this.axis = axis;
		}
		
		public int getDirection () {
			return direction;
		}
		
		public Direction getOpposite () {
			return values()[opposite];
		}
		
		public int getAxis () {
			return axis;
		}
	}
	
	private enum EnumGhost {
		BLINKY(0, "Blinky", 14, 11, new Color(255, 7, 7), 0),
		PINKY(1, "Pinky", 14, 14, new Color(255, 184, 222), 7),
		INKY(2, "Inky", 12, 14, new Color(7, 255, 255), 17, new int[] { 30, 0, 0 }),
		CLYDE(3, "Clyde", 16, 14, new Color(255, 159, 7), 32, new int[] { 60, 50, 0 });
		
		private int id;
		private String name;
		private int x, y;
		private Color color;
		private int globalLimit;
		private int[] dotLimit;
		
		EnumGhost (int id, String name, int x, int y, Color color, int globalLimit) {
			this.id = id;
			this.name = name;
			this.x = x;
			this.y = y;
			this.color = color;
			this.globalLimit = globalLimit;
			this.dotLimit = new int[] { 0, 0, 0 };
		}
		
		EnumGhost (int id, String name, int x, int y, Color color, int globalLimit, int[] dotLimit) {
			this.id = id;
			this.name = name;
			this.x = x;
			this.y = y;
			this.color = color;
			this.globalLimit = globalLimit;
			this.dotLimit = dotLimit;
		}
		
		public int getId () {
			return id;
		}
		
		public String getName () {
			return name;
		}
		
		public int getX () {
			return x;
		}
		
		public int getY () {
			return y;
		}
		
		public Color getColor () {
			return color;
		}
		
		public int getGlobalLimit () {
			return globalLimit;
		}
		
		public int[] getDotLimit () {
			return dotLimit;
		}
		
		public int getDotLimit (byte level) {
			if (level > 2) return dotLimit[2];
			return dotLimit[level];
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
		
		public int getId () {
			return id;
		}
		
		public Color getColor () {
			return color;
		}
	}
	
	private enum EnumBonus {
		CHERRY (0, 100),
		STRAWBERRY (1, 300),
		PEACH (2, 500),
		APPLE (3, 700),
		GRAPES (4, 1000),
		GALAXIAN (5, 2000),
		BELL (6, 3000),
		KEY (8, 5000);
		
		private int id;
		private int points;
		
		EnumBonus (int id, int points) {
			this.id = id;
			this.points = points;
		}
		
		public int getId () {
			return id;
		}
		
		public int getPoints () {
			return points;
		}
		
		public int getPoints (int id) {
			return values()[id].points;
		}
	}
	
	private class Tile {
		int x, y, extendedX, extendedY;
		int edible = 0; // 0 = none; 1 = dot; 2 = energizer
		EnumTile type;
		
		Tile (int x, int y) {
			this(x, y, EnumTile.PLAY, 1);
		}
		
		Tile (int x, int y, int edible) {
			this(x, y, EnumTile.PLAY, edible);
		}
		
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
			// TODO: Remove commented out code
			//            if (type == EnumTile.WALL) {
			//                glColor(type.getColor());
			//                drawModalRectWithCustomSizedTexture(extendedX, extendedY, 234, 264, 8, 8, 512, 512);
			//            }
			
			//            if (type == EnumTile.GHOST_LIMIT || type == EnumTile.TELE_ZONE) {
			//                glColor(Color.orange);
			//                drawModalRectWithCustomSizedTexture(extendedX, extendedY, 234, 264, 8, 8, 512, 512);
			//            }
			
			// Draw Food
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			if (edible == 1) { // Dot
				drawModalRectWithCustomSizedTexture(extendedX + 3, extendedY + 3, GUI_X, MAZE_Y, DOT, DOT, 512, 512);
			} else if (edible == 2) { // Energizer
				if (ENERGIZER_STATE == 0)
					drawModalRectWithCustomSizedTexture(extendedX, extendedY, GUI_X + DOT, MAZE_Y, ENERGIZER, ENERGIZER, 512, 512);
			}
			return this;
		}
		
		public boolean hasEdible () {
			return edible != 0;
		}
		
		Tile updatePosition (int x, int y) {
			extendedX = this.x * 8 + x;
			extendedY = this.y * 8 + y;
			
			return this;
		}
		
		public Point getPosition () {
			return new Point(x, y);
		}
	}
	
	private class Player extends Mover {
		int lives = 3, foodEaten, ghostsEaten, deathAnimation; // When scareTime is reached, ghostsEaten must be reset.
		boolean energizerMode, canEatGhost, playDeathAnimation;
		
		int STATE = 0;
		
		int prevX, prevY;
		
		boolean teleport;
		boolean stopped = false;
		
		public Player () {
			super(14, 23);
			
			prevX = 14;
			prevY = 23;
			
			teleport = false; // TODO: Remove?
			current = desired = Direction.LEFT;
		}
		
		// TODO: cornering
		private Mover move () {
			if (desired != current) changeDirection(desired);
			
			// Teleport
			if (checkTile() == EnumTile.TELE) {
				if (extendedX <= (offsetX + 2)) moveX = 103;
				if (extendedX >= (offsetX + (27 * 8))) moveX = -103;
			}
			
			if (canMove) {
				switch (current) {
					case LEFT:
						if (!isBlockedLeft()) moveX -= getSpeed();
						else current = Direction.STAND;
						return this;
					case RIGHT:
						if (!isBlockedRight()) moveX += getSpeed();
						else current = Direction.STAND;
						return this;
					case UP:
						if (!isBlocked()) moveY -= getSpeed();
						else current = Direction.STAND;
						return this;
					case DOWN:
						if (!isBlockedDown()) moveY += getSpeed();
						else current = Direction.STAND;
						return this;
				}
			}
			return this;
		}
		
		@Override
		public void update () {
			// Dot Counter Logic
			if (tiles[y][x].hasEdible()) {
				if (useGlobalCounter) globalCounter++;
				else {
					if (!houseQueue.isEmpty()) ghosts[houseQueue.get(0)].dotCounter++;
				}
				dotTick = tickCounter;
			}
			
			// Eating Logic
			// Eat Dots
			if (tiles[y][x].edible == 1) {
				tiles[y][x].edible = 0;
				foodEaten++;
				score += 10;
				playWaka();
			}
			
			// Eat Energizer
			if (tiles[y][x].edible == 2) {
				tiles[y][x].edible = 0;
				foodEaten++;
				score += 50;
				energizerMode = true;
				for (int i = 0; i < ghosts.length; i++) ghosts[i].beScared = true;
				canEatGhost = true;
			}
			
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
					setStartPos(14, 23);
					prevX = 14;
					prevY = 23;
					gameOverTick = tickCounter;
				}
				if ((tickCounter - gameOverTick) == 20) {
					if (lives == 0) { // Go back to main menu logic
						giveReward(ArcadeItems.TICKET, (score / 80));
						inMenu = true;
						menu = 3;
						playDeathAnimation = false;
						backTick = tickCounter;
						houseQueue.clear();
						gameOver = mazeBlink = nextLevel = updatePos = false;
					} else {
						lives--;
						setupGame();
						deathAnimation = 0;
						useGlobalCounter = true;
						globalCounter = 0;
						playDeathAnimation = false;
						showBonus = false;
						bonusTick = 0;
					}
				}
			}
			
			// Bonus Logic
			if (showBonus) {
				if (extendedX == offsetX + (13 * 8) && extendedY == offsetY + (17 * 8)) {
					showBonus = false;
					bonusTick = 0;
					score += bonus.getPoints();
				}
			}
		}
		
		public void kill () {
			playDeathAnimation = true;
			deathTick = tickCounter;
		}
		
		private void reset () {
			setStartPos(14, 23);
			
			prevX = 14;
			prevY = 23;
			moveX = moveY = foodEaten = 0;
			teleport = false; // TODO: Remove?
			useGlobalCounter = false;
			current = desired = Direction.LEFT;
		}
		
		// TODO: Play through tileEntity?
		private void playWaka () {
			if (waka == 0) {
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PACMAN_WAKA_1, SoundCategory.BLOCKS, volume, 1.0f);
				waka++;
				return;
			}
			if (waka == 1) {
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PACMAN_WAKA_2, SoundCategory.BLOCKS, volume, 1.0f);
				waka++;
				return;
			}
			if (waka == 2) {
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PACMAN_WAKA_3, SoundCategory.BLOCKS, volume, 1.0f);
				waka++;
				return;
			}
			if (waka == 3) {
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PACMAN_WAKA_4, SoundCategory.BLOCKS, volume, 1.0f);
				waka++;
				return;
			}
			if (waka == 4) {
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PACMAN_WAKA_5, SoundCategory.BLOCKS, volume, 1.0f);
				waka++;
				return;
			}
			if (waka == 5) {
				getWorld().playSound(getPlayer(), getPos(), ArcadeSounds.PACMAN_WAKA_6, SoundCategory.BLOCKS, volume, 1.0f);
				waka = 0;
				return;
			}
		}
		
		private float getSpeed () {
			if (level == 0) { // Level 1
				if (!canEatGhost) {
					if (tiles[y][x].edible != 0) return 0.71f;
				} else {
					if (tiles[y][x].edible != 0) return 0.79f;
					return 0.9f;
				}
				return 0.8f;
			}
			if (level >= 1 && level <= 3) { // Level 2-4
				if (!canEatGhost) {
					if (tiles[y][x].edible != 0) return 0.79f; // Dot Speed
				} else {
					if (tiles[y][x].edible != 0) return 0.83f; // Fright Dot Speed
					return 0.95f; // Fright Speed
				}
				return 0.9f; // Regular Speed
			}
			if (level >= 4 && level <= 19) { // Level 5-20
				if (!canEatGhost) {
					if (tiles[y][x].edible != 0) return 0.87f; // Dot Speed
				} else {
					if (tiles[y][x].edible != 0) return 0.87f; // Fright Dot Speed
					return 1; // Fright Speed
				}
				return 1; // Regular Speed
			}
			if (level >= 20) { // Level 21+
				if (tiles[y][x].edible != 0) return 0.79f; // Dot Speed
				return 0.9f; // Regular Speed
			}
			return 0.8f;
		}
		
		private Mover changeDirection (Direction newDirection) {
			switch (newDirection) {
				case LEFT:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlockedLeft()) current = newDirection;
						}
					} else {
						if (!isBlockedLeft()) current = newDirection;
					}
					return this;
				case RIGHT:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlockedRight()) current = newDirection;
						}
					} else {
						if (!isBlockedRight()) current = newDirection;
					}
					return this;
				case UP:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlocked()) current = newDirection;
						}
					} else {
						if (!isBlocked()) current = newDirection;
					}
					return this;
				case DOWN:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlockedDown()) current = newDirection;
						}
					} else {
						if (!isBlockedDown()) current = newDirection;
					}
					return this;
			}
			return this;
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
					case STAND: // TODO: prevState so STAND can have mouth open in correct direction
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, 0, GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
						return this;
					case LEFT:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
						return this;
					case RIGHT:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
						return this;
					case DOWN:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + (PACMAN * 3) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
						return this;
					case UP:
						drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + (PACMAN * 2) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
						return this;
				}
			} else {
				glColor(Color.WHITE);
				if (playDeathAnimation && deathAnimation <= 12) drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, PACMAN * deathAnimation, GUI_Y + (PACMAN * 2) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
			}
			return this;
		}
		
		private Player drawLives () {
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			for (int i = 0; i < lives; i++) drawModalRectWithCustomSizedTexture(offsetX + (i * 14), offsetY + 248, PACMAN, GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
			return this;
		}
	}
	
	private class Ghost extends Mover {
		EnumGhost info;
		boolean scared = false, eaten = false, scatter = true, inHouse, blink, beScared, isScared;
		int SCARED_STATE = 0, BODY_STATE = 0;
		int modeTime, mode, blinks;
		int scaredTick, pauseTick, scaredDuration;
		int dotCounter = 0;
		boolean remove, beRemoved; // Remove from house
		
		public Ghost (EnumGhost ghost) {
			super(ghost.getX(), ghost.getY());
			info = ghost;
			
			if (ghost != EnumGhost.BLINKY) {
				inHouse = true;
				current = desired = Direction.UP;
				houseQueue.add(info.id);
			} else current = desired = Direction.LEFT;
		}
		
		private boolean isEven (int n) {
			return n % 2 == 0;
		}
		
		private Mover move () {
			if (inHouse) {
				if (current == Direction.UP) moveY -= 0.5f;
				if (current == Direction.DOWN) moveY += 0.5f;
				if (extendedY == (offsetY + (13 * 8) + 4)) current = Direction.DOWN;
				if (extendedY == (offsetY + (15 * 8) - 4)) current = Direction.UP;
			} else if (beRemoved) {
				if (current == Direction.RIGHT) moveX += 0.5f;
				if (current == Direction.LEFT) moveX -= 0.5f;
				if (current == Direction.UP) moveY -= 0.5f;
				if (extendedX == (offsetX + (11 * 8) + 4)) current = Direction.RIGHT;
				if (extendedX == (offsetX + (15 * 8) + 4)) current = Direction.LEFT;
				if (extendedX == (offsetX + 14 * 8) - 4) current = Direction.UP;
				if (extendedY == (offsetY + 11 * 8)) {
					beRemoved = false;
					remove = true;
					if (info == EnumGhost.BLINKY || info == EnumGhost.INKY) current = Direction.RIGHT;
					else current = Direction.LEFT;
				}
			} else {
				
				if (desired != current) changeDirection(desired);
				
				// Teleport
				if (checkTile() == EnumTile.TELE_ZONE || checkTile() == EnumTile.TELE) {
					if (info == EnumGhost.BLINKY || info == EnumGhost.PINKY) {
						if ((extendedX <= (offsetX + 2)) && y == 14) moveX = 100;
						if ((extendedX >= (offsetX + (26 * 8))) && y == 14) moveX = -103;
					} else if (info == EnumGhost.INKY) {
						if (extendedX <= (offsetX + 2) && y == 14) moveX = 116;
						if (extendedX >= (offsetX + (26 * 8)) && y == 14) moveX = -85;
					} else if (info == EnumGhost.CLYDE) {
						if (extendedX <= (offsetX + 2) && y == 14) moveX = 84;
						if (extendedX >= (offsetX + (26 * 8)) && y == 14) moveX = -120;
					}
				}
				
				// Scared direction
				if (beScared) {
					beScared = false;
					isScared = true;
					if (!eaten) {
						switch (current) { // TODO: Simplify?
							case LEFT:
								if (!isBlockedRight()) desired = current = Direction.RIGHT;
								else {
									if (!isBlocked()) desired = current = Direction.UP;
									if (!isBlockedDown()) desired = current = Direction.DOWN;
									if (!isBlockedLeft()) desired = current = Direction.LEFT;
								}
								break;
							case RIGHT:
								if (!isBlockedLeft()) desired = current = Direction.LEFT;
								else {
									if (!isBlocked()) desired = current = Direction.UP;
									if (!isBlockedDown()) desired = current = Direction.DOWN;
									if (!isBlockedRight()) desired = current = Direction.RIGHT;
								}
								break;
							case UP:
								if (!isBlockedDown()) desired = current = Direction.DOWN;
								else {
									if (!isBlockedLeft()) desired = current = Direction.LEFT;
									if (!isBlockedRight()) desired = current = Direction.RIGHT;
									if (!isBlocked()) desired = current = Direction.UP;
								}
								break;
							case DOWN:
								if (!isBlocked()) desired = current = Direction.UP;
								else {
									if (!isBlockedLeft()) desired = current = Direction.LEFT;
									if (!isBlockedRight()) desired = current = Direction.RIGHT;
									if (!isBlockedDown()) desired = current = Direction.DOWN;
								}
								break;
						}
					}
				}
				
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
							if (!isBlockedDown()) moveY += getSpeed();
							return this;
					}
				}
			}
			return this;
		}
		
		// TODO: Elroy Speed
		private float getSpeed () {
			if (level == 0) { // Level 1
				if (!eaten) {
					if (scared) {
						if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.4f; // Tunnel Speed
						else return 0.5f; // Scared Speed
					}
					if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.4f; // Tunnel Speed
					return 0.75f; // Normal Speed
				} else return 1; // Eaten Speed
			}
			if (level >= 1 && level <= 3) { // Level 2-4
				if (!eaten) {
					if (scared) {
						if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.45f; // Tunnel Speed
						else return 0.55f; // Scared Speed
					}
					if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.45f; // Tunnel Speed
					return 0.85f; // Normal Speed
				} else return 1; // Eaten Speed
			}
			if ((level >= 4 && level <= 15) || level == 17) { // Level 5-16, 18
				if (!eaten) {
					if (scared) {
						if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.5f; // Tunnel Speed
						else return 0.6f; // Scared Speed
					}
					if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.5f; // Tunnel Speed
					return 0.95f; // Normal Speed
				} else return 1; // Eaten Speed
			}
			if (level == 16 || level >= 18) { // Level 17, 19+
				if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.5f; // Tunnel Speed
				return 0.95f; // Normal Speed
			}
			return 0.75f;
		}
		
		// TODO: Elroy
		@Override
		public void update () {
			// Collision Detection
			//if (((x == pacman.x) && (y == pacman.y)) && ((extendedX == pacman.extendedX) && (extendedY == pacman.extendedY))) {
			//if ((extendedX == pacman.extendedX) && (extendedY == pacman.extendedY)) {
			//if ((extendedX >= (pacman.extendedX - 4) && extendedX <= (pacman.extendedX + 4)) && (extendedY >= (pacman.extendedY - 4) && extendedY <= (pacman.extendedY + 4)) && (((extendedX - offsetX) % 8) == 0 && ((extendedY - offsetY) % 8) == 0)) {
			//if ((x == pacman.x) && (y == pacman.y)) {
			// TODO: Add text saying how many points received?
			// TODO: Figure out how to make it so when ghosts are ontop of one another their both eaten instead of one getting away.
			if ((((extendedX - offsetX) / 8) == ((pacman.extendedX - offsetX) / 8)) && (((extendedY - offsetY) / 8) == ((pacman.extendedY - offsetY) / 8))) {
				if (isScared) {
					isScared = false;
					pauseTick = tickCounter;
					scaredDuration = tickCounter - scaredTick; // I think this works. idk tho
					eaten = true;
					pause();
				}
				if (scared) {
					if ((tickCounter - pauseTick) == 20) {
						unpause();
						pauseTick = 0;
						scaredTick = tickCounter + scaredDuration;
						scared = false;
						switch (pacman.ghostsEaten) {
							case 0:
								score += 200;
								break;
							case 1:
								score += 400;
								break;
							case 2:
								score += 800;
								break;
							case 3:
								score += 1600;
								break;
						}
						pacman.ghostsEaten++;
					}
				}
				// TODO: move death logic here?
			}
			
			// House Logic
			if (eaten) {
				//if ((x >= 11 && x <= 16) && (y >= 13 && y <= 15)) {
				if (x == 13 && y == 15) {
					eaten = scared = isScared = false;
					remove = true;
				}
			}
			if (inHouse) {
				if (useGlobalCounter) {
					if (globalCounter == info.getGlobalLimit()) {
						inHouse = false;
						//remove = true;
						beRemoved = true;
						if (houseQueue.get(0) == 3)
							useGlobalCounter = false; // Disable global counter if it is releasing Clyde
						houseQueue.remove(0);
					}
				} else {
					if (dotCounter == info.getDotLimit(level)) {
						inHouse = false;
						//remove = true;
						beRemoved = true;
						houseQueue.remove(0);
					}
				}
				if ((tickCounter - dotTick) == dotTimeLimit) {
					dotTick = tickCounter;
					inHouse = false;
					//remove = true;
					beRemoved = true;
					houseQueue.remove(0);
				}
			}
			if (remove && ((x == 13 || x == 14) && y == 11)) remove = false;
			
			// Mode switching logic
			if (level == 0) { // Level 1
				if (mode == 0) { // Scatter
					modeTime = 7;
				} else if (mode == 1) { // Chase
					modeTime = 20;
				} else if (mode == 2) { // Scatter
					modeTime = 7;
				} else if (mode == 3) { // Chase
					modeTime = 20;
				} else if (mode == 4) { // Scatter
					modeTime = 5;
				} else if (mode == 5) { // Chase
					modeTime = 20;
				} else if (mode == 6) { // Scatter
					modeTime = 5;
				} else if (mode >= 7) { // Chase
					modeTime = 0;
				}
			}
			
			// TODO: If the ghosts enter frightened mode, the scatter/chase timer is paused
			// Timer for switching between scatter and chase (i think)
			if (modeTime == 0) scatter = false;
			else {
				if ((tickCounter - scatterTick) == (modeTime * 20)) {
					scatterTick = tickCounter;
					mode++;
					scatter = !scatter;
				}
			}
			
			// Makes ghost scared when pacman is energized
			if (!eaten) {
				if ((pacman.energizerMode && !scared) || (pacman.energizerMode && isScared)) {
					scared = true;
					if (info == EnumGhost.CLYDE) pacman.energizerMode = false;
					scaredTick = tickCounter;
				}
				
				// Scared Logic
				if (scared) {
					if ((tickCounter - scaredTick) == Math.max((scaredTime - 1), 0) * 20) {
						scaredTick = tickCounter;
						blink = true;
					}
				}
				
				// Blink Animation
				if (blink) {
					if ((tickCounter - scaredTick) == (20 / scaredFlash)) {
						scaredTick = tickCounter;
						if (SCARED_STATE == 0) SCARED_STATE = 1;
						else SCARED_STATE = 0;
						blinks++;
					}
					if (blinks == scaredFlash) {
						SCARED_STATE = blinks = pacman.ghostsEaten = scaredTick = 0;
						scared = pacman.canEatGhost = blink = isScared = false;
					}
				}
			}
			
			// Animation
			// TODO: Fix jittering
			if ((extendedX - offsetX) % 4 == 0 && (extendedY - offsetY) % 4 == 0) {
				if (BODY_STATE == 0) BODY_STATE = 1;
				else BODY_STATE = 0;
			}
		}
		
		private Mover changeDirection (Direction newDirection) {
			switch (newDirection) {
				case LEFT:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlockedLeft()) current = newDirection;
						}
					}
					return this;
				case RIGHT:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlockedRight()) current = newDirection;
						}
					}
					return this;
				case UP:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlocked()) current = newDirection;
						}
					}
					return this;
				case DOWN:
					if (current.getAxis() != newDirection.getAxis()) {
						if (onTile()) {
							if (!isBlockedDown()) current = newDirection;
						}
					}
					return this;
			}
			return this;
		}
		
		private double calculateDistance (int x, int y) {
			//return Math.sqrt(Math.pow((Math.min((27 * 8) + offsetX, Math.max(0, x)) - getTarget().getX()), 2) + Math.pow((Math.max((30 * 8) + offsetY, Math.min(0, y) - getTarget().getY())), 2));
			return Math.sqrt(Math.pow((x - getTarget().getX()), 2) + Math.pow((y - getTarget().getY()), 2));
		}
		
		// TODO: make the ghost less stupid with Ghost Limit Tiles
		// TODO: make ghosts want to go thru tunnel if pacman is on opposite side and it will be quicker
		private Ghost ai () {
			if (current == Direction.LEFT) { // Can't Move right
				if (isBlockedLeft(x - 1, y)) { // is blocked left
					if (!isBlockedDown(x - 1, y) && !isBlocked(x - 1, y)) {
						if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY + 8))
							desired = Direction.UP;
						else desired = Direction.DOWN;
					}
					if (!isBlocked(x - 1, y) && isBlockedDown(x - 1, y)) desired = Direction.UP;
					if (!isBlockedDown(x - 1, y) && isBlocked(x - 1, y)) desired = Direction.DOWN;
				} else { // is not blocked left
					if (!isBlockedDown(x - 1, y) && !isBlocked(x - 1, y)) {
						if (calculateDistance(extendedX - 16, extendedY) < calculateDistance(extendedX - 8, extendedY - 8) && calculateDistance(extendedX - 16, extendedY) < calculateDistance(extendedX - 8, extendedY + 8))
							desired = Direction.LEFT;
						else if ((calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 16, extendedY)))
							desired = Direction.UP;
						else if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 16, extendedY) && calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 8, extendedY - 8))
							desired = Direction.DOWN;
						else if (calculateDistance(extendedX - 8, extendedY + 8) == calculateDistance(extendedX - 16, extendedY) && calculateDistance(extendedX - 16, extendedY) == calculateDistance(extendedX - 8, extendedY - 8))
							desired = Direction.DOWN;
					}
					if (!isBlocked(x - 1, y)) {
						if ((calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 16, extendedY)))
							desired = Direction.UP;
					}
					if (!isBlockedDown(x - 1, y)) {
						if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 16, extendedY))
							desired = Direction.DOWN;
					}
					//if (isBlocked(x - 1, y) && isBlockedDown(x - 1, y)) desired = Direction.LEFT;
				}
			}
			if (current == Direction.RIGHT) { // Can't Move left
				if (isBlockedRight(x + 1, y)) {
					if (!isBlockedDown(x + 1, y) && !isBlocked(x + 1, y)) {
						if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY + 8))
							desired = Direction.UP;
						else desired = Direction.DOWN;
					}
					if (!isBlocked(x + 1, y) && isBlockedDown(x + 1, y)) desired = Direction.UP;
					if (!isBlockedDown(x + 1, y) && isBlocked(x + 1, y)) desired = Direction.DOWN;
				} else {
					if (!isBlockedDown(x + 1, y) && !isBlocked(x + 1, y)) {
						if ((calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 16, extendedY)))
							desired = Direction.UP;
						else if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY - 8) && calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 16, extendedY))
							desired = Direction.DOWN;
						else if (calculateDistance(extendedX + 16, extendedY) < calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX + 16, extendedY) < calculateDistance(extendedX + 8, extendedY - 8))
							desired = Direction.RIGHT;
						else if ((calculateDistance(extendedX + 16, extendedY) == calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX + 8, extendedY + 8) == calculateDistance(extendedX + 8, extendedY - 8)))
							desired = Direction.UP;
					}
					if (!isBlocked(x + 1, y)) {
						if ((calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 16, extendedY)))
							desired = Direction.UP;
					}
					if (!isBlockedDown(x + 1, y)) {
						if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 16, extendedY))
							desired = Direction.DOWN;
					}
					//if (isBlocked(x + 1, y) && isBlockedDown(x + 1, y)) desired = Direction.RIGHT;
				}
			}
			if (current == Direction.UP) { // Can't move down
				if (isBlocked(x, y - 1)) {
					if (!isBlockedLeft(x, y - 1) && !isBlockedRight(x, y - 1)) {
						if (tiles[y - 1][x - 1].type == EnumTile.GHOST_LIMIT) desired = Direction.RIGHT;
						else if (tiles[y - 1][x + 1].type == EnumTile.GHOST_LIMIT) desired = Direction.LEFT;
						else {
							if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY - 8))
								desired = Direction.LEFT;
							else desired = Direction.RIGHT;
						}
					}
					if (!isBlockedLeft(x, y - 1) && isBlockedRight(x, y - 1)) desired = Direction.LEFT;
					if (!isBlockedRight(x, y - 1) && isBlockedLeft(x, y - 1)) desired = Direction.RIGHT;
				} else {
					if (!isBlockedRight(x, y - 1) && !isBlockedLeft(x, y - 1)) {
						if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY - 8) && calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16))
							desired = Direction.LEFT;
						else if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY - 8) && calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16))
							desired = Direction.RIGHT;
						else if (calculateDistance(extendedX, extendedY - 16) < calculateDistance(extendedX - 8, extendedY - 8) && calculateDistance(extendedX, extendedY - 16) < calculateDistance(extendedX + 8, extendedY - 8))
							desired = Direction.UP;
						else if (calculateDistance(extendedX - 8, extendedY - 8) == calculateDistance(extendedX + 8, extendedY - 8) && calculateDistance(extendedX + 8, extendedY - 8) == calculateDistance(extendedX, extendedY - 16))
							desired = Direction.LEFT;
					}
					if (!isBlockedLeft(x, y - 1)) {
						if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16))
							desired = Direction.LEFT;
					}
					if (!isBlockedRight(x, y - 1)) {
						if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16))
							desired = Direction.RIGHT;
					}
					//if (isBlockedLeft(x, y - 1) && isBlockedRight(x, y - 1)) desired = Direction.UP;
				}
			}
			if (current == Direction.DOWN) { // Can't move up
				if (isBlockedDown(x, y + 1)) { // is blocked down
					if (!isBlockedLeft(x, y + 1) && !isBlockedRight(x, y + 1)) {
						if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY + 8))
							desired = Direction.LEFT;
						else desired = Direction.RIGHT;
					}
					if (!isBlockedRight(x, y + 1) && isBlockedLeft(x, y + 1)) desired = Direction.RIGHT;
					if (!isBlockedLeft(x, y + 1) && isBlockedRight(x, y + 1)) desired = Direction.LEFT;
				} else {
					if (!isBlockedLeft(x, y + 1) && !isBlockedRight(x, y + 1)) {
						if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16))
							desired = Direction.LEFT;
						else if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16))
							desired = Direction.RIGHT;
						else if (calculateDistance(extendedX, extendedY + 16) < calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX, extendedY + 16) < calculateDistance(extendedX + 8, extendedY + 8))
							desired = Direction.DOWN;
						else if (calculateDistance(extendedX, extendedY + 16) == calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX - 8, extendedY + 8) == calculateDistance(extendedX + 8, extendedY + 8))
							desired = Direction.RIGHT;
					}
					if (!isBlockedLeft(x, y + 1)) {
						if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16))
							desired = Direction.LEFT;
					}
					if (!isBlockedRight(x, y + 1)) {
						if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16))
							desired = Direction.RIGHT;
					}
					//if (isBlockedLeft(x, y + 1) && isBlockedRight(x, y + 1)) desired = Direction.DOWN;
				}
			}
			return this;
		}
		
		private boolean isBlocked (int x, int y) {
			return tiles[Math.max(0, y - 1)][Math.max(0, x)].type == EnumTile.WALL;
		}
		
		private boolean isBlockedDown (int x, int y) {
			return (eaten || inHouse) ? tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL : tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL || tiles[Math.min(30, y + 1)][x].type == EnumTile.GHOST_ONLY;
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
		
		private Point getTarget () {
			if (scared) {
				// Ghosts can be scared in house, but they still move up and down
				if (inHouse)
					return new Point((13 * 8) + offsetX, (15 * 8) + offsetY);
				return new Point((int)(Math.random() * 28) * 8 + offsetX, (int)(Math.random() * 30) * 8 + offsetY); // Not the best option
			} else if (eaten) {
				return new Point((13 * 8) + offsetX, (15 * 8) + offsetY);
			} else if (inHouse) {
				return new Point((13 * 8) + offsetX, (15 * 8) + offsetY);
			} else if (remove) {
				return new Point((13 * 8) + offsetX, (11 * 8) + offsetY);
			} else {
				switch (info) {
					case BLINKY:
						return scatter ? new Point((23 * 8) + offsetX, offsetY) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
					case INKY:
						switch (pacman.current) { // Scatter Bottom Right
							case STAND:
								return scatter ? new Point((27 * 8) + offsetX, (30 * 8) + offsetY) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
							case LEFT:
								return scatter ? new Point((27 * 8) + offsetX, (30 * 8) + offsetY) : new Point(((pacman.getPosition().x - 3) * 8) + offsetX, ((pacman.getPosition().y + 1) * 8) + offsetY);
							case RIGHT:
								return scatter ? new Point((27 * 8) + offsetX, (30 * 8) + offsetY) : new Point(((pacman.getPosition().x + 3) * 8) + offsetX, ((pacman.getPosition().y - 1) * 8) + offsetY);
							case UP:
								return scatter ? new Point((27 * 8) + offsetX, (30 * 8) + offsetY) : new Point(((pacman.getPosition().x - 1) * 8) + offsetX, ((pacman.getPosition().y - 7) * 8) + offsetY);
							case DOWN:
								return scatter ? new Point((27 * 8) + offsetX, (30 * 8) + offsetY) : new Point(((pacman.getPosition().x - 4) * 8) + offsetX, ((pacman.getPosition().y + 2) * 8) + offsetY);
						}
					case PINKY:
						switch (pacman.current) { // Scatter Top Left (4,0)
							case STAND:
								return scatter ? new Point((4 * 8) + offsetX, offsetY) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
							case LEFT:
								return scatter ? new Point((4 * 8) + offsetX, offsetY) : new Point((pacman.getPosition().x * 8) + offsetX - (4 * 8), (pacman.getPosition().y * 8) + offsetY);
							case RIGHT:
								return scatter ? new Point((4 * 8) + offsetX, offsetY) : new Point((pacman.getPosition().x * 8) + offsetX + (4 * 8), (pacman.getPosition().y * 8) + offsetY);
							case UP:
								return scatter ? new Point((4 * 8) + offsetX, offsetY) : new Point((pacman.getPosition().x * 8) + offsetX - (4 * 8), (pacman.getPosition().y * 8) + offsetY - (4 * 8));
							case DOWN:
								return scatter ? new Point((4 * 8) + offsetX, offsetY) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY + (4 * 8));
						}
					case CLYDE:
						// Scatter Bottom Left
						// if pac man is more 8 tiles away from him. target is pacman's local
						// if pac man is less than 8 tiles away. target is his scatter target
						// diameter = 19
						
						// TODO: Something is wrong. I think if x goes to negative it thinks ghost is in range
						if (scatter) return new Point(offsetX, (30 * 8) + offsetY);
						else {
							// Clyde proximity
							for (int i = 0; i < 4; i++) {
								if ((extendedX <= (pacman.extendedX + (8 * 8)) && extendedX >= (pacman.extendedX - (8 * 8))) && (extendedY <= (pacman.extendedY - (8 * i))))
									return new Point(offsetX, (30 * 8) + offsetY);
							}
							for (int i = 1; i < 4; i++) {
								if ((extendedX <= (pacman.extendedX + (8 * 8)) && extendedX >= (pacman.extendedX - (8 * 8))) && (extendedY <= (pacman.extendedY + (8 * i))))
									return new Point(offsetX, (30 * 8) + offsetY);
							}
							for (int i = 4; i < 6; i++) {
								if ((extendedX <= (pacman.extendedX + (8 * 7)) && extendedX >= (pacman.extendedX - (8 * 7))) && (extendedY <= (pacman.extendedY - (8 * i))))
									return new Point(offsetX, (30 * 8) + offsetY);
							}
							for (int i = 4; i < 6; i++) {
								if ((extendedX <= (pacman.extendedX + (8 * 7)) && extendedX >= (pacman.extendedX - (8 * 7))) && (extendedY <= (pacman.extendedY + (8 * i))))
									return new Point(offsetX, (30 * 8) + offsetY);
							}
							if ((extendedX <= (pacman.extendedX + (8 * 6)) && extendedX >= (pacman.extendedX - (8 * 6))) && (extendedY <= (pacman.extendedY - (8 * 6))))
								return new Point(offsetX, (30 * 8) + offsetY);
							if ((extendedX <= (pacman.extendedX + (8 * 6)) && extendedX >= (pacman.extendedX - (8 * 6))) && (extendedY <= (pacman.extendedY + (8 * 6))))
								return new Point(offsetX, (30 * 8) + offsetY);
							if ((extendedX <= (pacman.extendedX + (8 * 5)) && extendedX >= (pacman.extendedX - (8 * 5))) && (extendedY <= (pacman.extendedY - (8 * 7))))
								return new Point(offsetX, (30 * 8) + offsetY);
							if ((extendedX <= (pacman.extendedX + (8 * 5)) && extendedX >= (pacman.extendedX - (8 * 5))) && (extendedY <= (pacman.extendedY + (8 * 7))))
								return new Point(offsetX, (30 * 8) + offsetY);
							if ((extendedX <= (pacman.extendedX + (8 * 3)) && extendedX >= (pacman.extendedX - (8 * 3))) && (extendedY <= (pacman.extendedY - (8 * 8))))
								return new Point(offsetX, (30 * 8) + offsetY);
							if ((extendedX <= (pacman.extendedX + (8 * 3)) && extendedX >= (pacman.extendedX - (8 * 3))) && (extendedY <= (pacman.extendedY + (8 * 8))))
								return new Point(offsetX, (30 * 8) + offsetY);
							
							return new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
						}
				}
			}
			return null;
		}
		
		private Ghost drawGhost () {
			// Target TODO: Remove
//			glColor(Color.green);
//			drawModalRectWithCustomSizedTexture(getTarget().x, getTarget().y, 234, 264, 8, 8, 512, 512);
			
			if (scared) {
				if (SCARED_STATE == 0) glColor(new Color(33, 33, 222));
				else glColor(new Color(245, 245, 255));
			} else glColor(info.getColor());
			if (!eaten) {
				// Body
				if (BODY_STATE == 0) drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
				else drawModalRectWithCustomSizedTexture(extendedX - 3, extendedY - 3, GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			if (!scared) {
				switch (current) {
					case LEFT:
						// Eye
						drawModalRectWithCustomSizedTexture(extendedX - 2, extendedY, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X, extendedY, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						
						// Pupil
						glColor(new Color(33, 33, 222));
						drawModalRectWithCustomSizedTexture(extendedX - 2, extendedY + 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X, extendedY + 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						break;
					case RIGHT:
						// Eye
						drawModalRectWithCustomSizedTexture(extendedX, extendedY, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 2, extendedY, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						
						// Pupil
						glColor(new Color(33, 33, 222));
						drawModalRectWithCustomSizedTexture(extendedX + 2, extendedY + 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 4, extendedY + 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						break;
					case UP:
						// Eye
						drawModalRectWithCustomSizedTexture(extendedX - 1, extendedY - 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 1, extendedY - 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						
						// Pupil
						glColor(new Color(33, 33, 222));
						drawModalRectWithCustomSizedTexture(extendedX, extendedY - 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 2, extendedY - 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						break;
					case DOWN:
						// Eye
						drawModalRectWithCustomSizedTexture(extendedX - 1, extendedY + 1, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 1, extendedY + 1, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
						
						// Pupil
						glColor(new Color(33, 33, 222));
						drawModalRectWithCustomSizedTexture(extendedX, extendedY + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 2, extendedY + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
						break;
				}
			} else { // Is Scared
				if (SCARED_STATE == 0) glColor(new Color(245, 245, 255));
				else glColor(new Color(255, 15, 15));
				// Pupil
				drawModalRectWithCustomSizedTexture(extendedX + 1, extendedY + 1, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
				drawModalRectWithCustomSizedTexture(extendedX + EYE_X + 1, extendedY + 1, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
				// Mouth
				drawModalRectWithCustomSizedTexture(extendedX - 2, extendedY + 5, GUI_X, MAZE_Y + GHOST, MOUTH_X, MOUTH_Y, 512, 512);
			}
			return this;
		}
	}
	
	private class Mover {
		Direction current, desired;
		
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
			return tiles[y + 1][x].type == EnumTile.WALL || tiles[y + 1][x].type == EnumTile.GHOST_ONLY;
		}
		
		boolean isBlockedLeft () {
			return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL;
		}
		
		boolean isBlockedRight () {
			return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL;
		}
		
		boolean onTile () {
			return (extendedX - offsetX) % 8 == 0 && (extendedY - offsetY) % 8 == 0;
		}
		
		EnumTile checkTile () {
			return tiles[y][x].type;
		}
		
		public Point getPosition () {
			return new Point(x, y);
		}
		
		public Tile getTile () {
			return tiles[y][x];
		}
		
		/**
		 * Called every tick
		 */
		public void update () {}
	}
	
	private void getLevelData () {
		// Set dot eating time limit
		if (level < 4) dotTimeLimit = (4 * 20);
		else dotTimeLimit = (3 * 20);
		// Set bonus fruit
		if (level == 0) bonus = EnumBonus.CHERRY; // Level 1
		if (level == 1) bonus = EnumBonus.STRAWBERRY; // Level 2
		if (level == 2 || level == 3) bonus = EnumBonus.PEACH; // Level 3 - 4
		if (level == 4 || level == 5) bonus = EnumBonus.APPLE; // Level 5 - 6
		if (level == 6 || level == 7) bonus = EnumBonus.GRAPES; // Level 7 - 8
		if (level == 8 || level == 9) bonus = EnumBonus.GALAXIAN; // Level 9 - 10
		if (level == 10 || level == 11) bonus = EnumBonus.BELL; // Level 11 - 12
		if (level > 11) bonus = EnumBonus.KEY; // Level 13+
		// Set speeds
		if (level == 0) { // Level 1
			scaredTime = 6;
			scaredFlash = 5;
		}
		if (level == 1) { // Level 2
			scaredTime = 5;
			scaredFlash = 5;
		}
		if (level == 2) { // Level 3
			scaredTime = 4;
			scaredFlash = 5;
		}
		if (level == 3) { // Level 4
			scaredTime = 3;
			scaredFlash = 5;
		}
		if (level == 4) { // Level 5
			scaredTime = 2;
			scaredFlash = 5;
		}
		if (level == 5) { // Level 6
			scaredTime = 5;
			scaredFlash = 5;
		}
		if (level == 6) { // Level 7
			scaredTime = 2;
			scaredFlash = 5;
		}
		if (level == 7) { // Level 8
			scaredTime = 2;
			scaredFlash = 5;
		}
		if (level == 8) { // Level 9
			scaredTime = 1;
			scaredFlash = 3;
		}
		if (level == 9) { // Level 10
			scaredTime = 5;
			scaredFlash = 5;
		}
		if (level == 10) { // Level 11
			scaredTime = 2;
			scaredFlash = 5;
		}
		if (level == 11) { // Level 12
			scaredTime = 1;
			scaredFlash = 3;
		}
		if (level == 12) { // Level 13
			scaredTime = 1;
			scaredFlash = 3;
		}
		if (level == 13) { // Level 14
			scaredTime = 3;
			scaredFlash = 5;
		}
		if (level == 14) { // Level 15
			scaredTime = 1;
			scaredFlash = 3;
		}
		if (level == 15) { // Level 16
			scaredTime = 1;
			scaredFlash = 3;
		}
		if (level == 16) { // Level 17
			scaredTime = 0;
			scaredFlash = 0;
		}
		if (level == 17) { // Level 18
			scaredTime = 1;
			scaredFlash = 3;
		}
		if (level == 18) { // Level 19
			scaredTime = 0;
			scaredFlash = 0;
		}
		if (level == 19) { // Level 20
			scaredTime = 0;
			scaredFlash = 0;
		}
		if (level >= 20) { // Level 21+
			scaredTime = 0;
			scaredFlash = 0;
		}
	}
	
	private void setupTiles () {
		// Row 0
		for (int i = 0; i < 28; i++) tiles[0][i] = new Tile(i, 0, EnumTile.WALL);
		// Row 1
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 12 && i < 15) || i == 27) tiles[1][i] = new Tile(i, 1, EnumTile.WALL);
			else tiles[1][i] = new Tile(i, 1);
		}
		// Row 2
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[2][i] = new Tile(i, 2, EnumTile.WALL);
			else tiles[2][i] = new Tile(i, 2);
		}
		// Row 3
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[3][i] = new Tile(i, 3, EnumTile.WALL);
			else if (i == 1 || i == 26) tiles[3][i] = new Tile(i, 3, 2);
			else tiles[3][i] = new Tile(i, 3);
		}
		// Row 4
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[4][i] = new Tile(i, 4, EnumTile.WALL);
			else tiles[4][i] = new Tile(i, 4);
		}
		// Row 5
		for (int i = 0; i < 28; i++) {
			if (i == 0 || i == 27) tiles[5][i] = new Tile(i, 5, EnumTile.WALL);
			else tiles[5][i] = new Tile(i, 5);
		}
		// Row 6
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[6][i] = new Tile(i, 6, EnumTile.WALL);
			else tiles[6][i] = new Tile(i, 6);
		}
		// Row 7
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[7][i] = new Tile(i, 7, EnumTile.WALL);
			else tiles[7][i] = new Tile(i, 7);
		}
		// Row 8
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 6 && i < 9) || (i > 12 && i < 15) || (i > 18 && i < 21) || i == 27)
				tiles[8][i] = new Tile(i, 8, EnumTile.WALL);
			else tiles[8][i] = new Tile(i, 8);
		}
		// Row 9
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 28))
				tiles[9][i] = new Tile(i, 9, EnumTile.WALL);
			else if (i == 12 || i == 15) tiles[9][i] = new Tile(i, 9, 0);
			else tiles[9][i] = new Tile(i, 9);
		}
		// Row 10
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 28))
				tiles[10][i] = new Tile(i, 10, EnumTile.WALL);
			else if (i == 12 || i == 15) tiles[10][i] = new Tile(i, 10, 0);
			else tiles[10][i] = new Tile(i, 10);
		}
		// Row 11
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[11][i] = new Tile(i, 11, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[11][i] = new Tile(i, 11, 0);
			else if (i > 9 && i < 18) tiles[11][i] = new Tile(i, 11, EnumTile.GHOST_LIMIT);
			else tiles[11][i] = new Tile(i, 11);
		}
		// Row 12
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 13) || (i > 14 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[12][i] = new Tile(i, 12, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[12][i] = new Tile(i, 12, 0);
			else if (i == 13 || i == 14) tiles[12][i] = new Tile(i, 12, EnumTile.GHOST_ONLY);
			else tiles[12][i] = new Tile(i, 12);
		}
		// Row 13
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || i == 10 || i == 17 || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[13][i] = new Tile(i, 13, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[13][i] = new Tile(i, 13, 0);
			else if (i > 10 && i < 17) tiles[13][i] = new Tile(i, 13, EnumTile.GHOST_ONLY);
			else tiles[13][i] = new Tile(i, 13);
		}
		// Row 14
		for (int i = 0; i < 28; i++) {
			if (i == 0 || i == 27) tiles[14][i] = new Tile(i, 14, EnumTile.TELE);
			else if ((i > 0 && i < 6) || (i > 21 && i < 27)) tiles[14][i] = new Tile(i, 14, EnumTile.TELE_ZONE);
			else if (i == 10 || i == 17) tiles[14][i] = new Tile(i, 14, EnumTile.WALL);
			else if (i > 10 && i < 17) tiles[14][i] = new Tile(i, 14, EnumTile.GHOST_ONLY);
			else if (i == 5 || (i > 6 && i < 10) || (i > 17 && i < 21) || i == 22) tiles[14][i] = new Tile(i, 14, 0);
			else tiles[14][i] = new Tile(i, 14);
		}
		// Row 15
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || i == 10 || i == 17 || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[15][i] = new Tile(i, 15, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[15][i] = new Tile(i, 15, 0);
			else if (i > 10 && i < 17) tiles[15][i] = new Tile(i, 15, EnumTile.GHOST_ONLY);
			else tiles[15][i] = new Tile(i, 15);
		}
		// Row 16
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[16][i] = new Tile(i, 16, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[16][i] = new Tile(i, 16, 0);
			else tiles[16][i] = new Tile(i, 16);
		}
		// Row 17
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[17][i] = new Tile(i, 17, EnumTile.WALL);
			else if (i > 8 && i < 19) tiles[17][i] = new Tile(i, 17, 0);
			else tiles[17][i] = new Tile(i, 17);
		}
		// Row 18
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[18][i] = new Tile(i, 18, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[18][i] = new Tile(i, 18, 0);
			else tiles[18][i] = new Tile(i, 18);
		}
		// Row 19
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28))
				tiles[19][i] = new Tile(i, 19, EnumTile.WALL);
			else if (i == 9 || i == 18) tiles[19][i] = new Tile(i, 19, 0);
			else tiles[19][i] = new Tile(i, 19);
		}
		// Row 20
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 12 && i < 15) || i == 27) tiles[20][i] = new Tile(i, 20, EnumTile.WALL);
			else tiles[20][i] = new Tile(i, 20);
		}
		// Row 21
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[21][i] = new Tile(i, 21, EnumTile.WALL);
			else tiles[21][i] = new Tile(i, 21);
		}
		// Row 22
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27)
				tiles[22][i] = new Tile(i, 22, EnumTile.WALL);
			else tiles[22][i] = new Tile(i, 22);
		}
		// Row 23
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 3 && i < 6) || (i > 21 && i < 24) || i == 27)
				tiles[23][i] = new Tile(i, 23, EnumTile.WALL);
			else if (i == 1 || i == 26) tiles[23][i] = new Tile(i, 23, 2);
			else if ((i > 9 && i < 13) || (i > 14 & i < 18)) tiles[23][i] = new Tile(i, 23, EnumTile.GHOST_LIMIT, 1);
			else if (i > 12 && i < 15) tiles[23][i] = new Tile(i, 23, EnumTile.GHOST_LIMIT);
			else if (i == 13 || i == 14) tiles[23][i] = new Tile(i, 23, 0);
			else tiles[23][i] = new Tile(i, 23);
		}
		// Row 24
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 3) || (i > 3 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 24) || i > 24)
				tiles[24][i] = new Tile(i, 24, EnumTile.WALL);
			else tiles[24][i] = new Tile(i, 24);
		}
		// Row 25
		for (int i = 0; i < 28; i++) {
			if ((i >= 0 && i < 3) || (i > 3 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 24) || i > 24)
				tiles[25][i] = new Tile(i, 25, EnumTile.WALL);
			else tiles[25][i] = new Tile(i, 25);
		}
		// Row 26
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 6 && i < 9) || (i > 12 && i < 15) || (i > 18 && i < 21) || i == 27)
				tiles[26][i] = new Tile(i, 26, EnumTile.WALL);
			else tiles[26][i] = new Tile(i, 26);
		}
		// Row 27
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 26) || i == 27)
				tiles[27][i] = new Tile(i, 27, EnumTile.WALL);
			else tiles[27][i] = new Tile(i, 27);
		}
		// Row 28
		for (int i = 0; i < 28; i++) {
			if (i == 0 || (i > 1 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 26) || i == 27)
				tiles[28][i] = new Tile(i, 28, EnumTile.WALL);
			else tiles[28][i] = new Tile(i, 28);
		}
		// Row 29
		for (int i = 0; i < 28; i++) {
			if (i == 0 || i == 27) tiles[29][i] = new Tile(i, 29, EnumTile.WALL);
			else tiles[29][i] = new Tile(i, 29);
		}
		// Row 30
		for (int i = 0; i < 28; i++) tiles[30][i] = new Tile(i, 30, EnumTile.WALL);
	}
}
