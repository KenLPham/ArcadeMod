package superhb.arcademod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.KeyHandler;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class GuiKong extends GuiArcade {
	private static ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/kong.png");
	
	// Texture Variables
	private static int GUI_WIDTH = 236; // 224
	private static int GUI_HEIGHT = 268; // 256
	
	private static int PLATFORM_WIDTH = 16;
	private static int PLATFORM_HEIGHT = 8;
	
	private static int LADDER_WIDTH = 8;
	private static int LADDER_HEIGHT = 52;
	
	private static int KONG_WIDTH = 48;
	private static int KONG_HEIGHT = 36;
	
	private static int GIRL_WIDTH = 15;
	private static int GIRL_HEIGHT = 22;
	
	private static int BARREL_TOP_WIDTH = 12;
	private static int BARREL_TOP_HIEGHT = 10;
	
	private static int BARREL_LENGTH = 16;
	private static int BARREL_WIDTH = 10;
	
	private static int OIL = 16;
	
	private static int FLAME_WIDTH = 16;
	private static int FLAME_HIEGHT = 15;
	
	private int boardX, boardY;
	private int barrelTick, barrelAnim;
	private ArrayList<Point> collisionBoxes = new ArrayList<>();
	private ArrayList<Ladder> ladders = new ArrayList<>();
	private Player player;
	
	public GuiKong (World world, TileEntityArcade tileEntity, EntityPlayer player) {
		super(world, tileEntity, null, player);
		setGuiSize(GUI_WIDTH, GUI_HEIGHT);
		setTexture(texture, 512, 512);
		setOffset(0, 0);
		setButtonPos((GUI_WIDTH / 2) - (buttonWidth / 2) - 30, GUI_HEIGHT - 32);
		setStartMenu(0);
		setCost(2);
		inMenu = false; // TODO: Remove
		createCollisionBoxes();
		createLadderCollisionBoxes();
		
		this.player = new Player();
	}
	
	@Override
	public void updateScreen () {
		super.updateScreen();
		if (inMenu) {
		} else {
			barrelTick++;
			
			if (barrelTick == 10) {
				if (barrelAnim == 3) barrelAnim = 0;
				else barrelAnim++;
				barrelTick = 0;
			}
			
			player.move();
		}
	}
	
	@Override
	public void drawScreen (int mouseX, int mouseY, float partialTick) {
		boardX = xScaled - (GUI_WIDTH / 2) + 6;
		boardY = yScaled - (GUI_HEIGHT / 2) + 6;
		
		super.drawScreen(mouseX, mouseY, partialTick);
		
		if (inMenu) {
		} else {
			drawPlatforms();
			drawLadders();
			drawOil();
			
			// Platform Collision
			for (Point p : collisionBoxes) this.drawModalRectWithCustomSizedTexture(boardX + p.x, boardY + p.y, 496, 510, 16, 2, 512, 512);
			for (Ladder l : ladders) {
				this.drawModalRectWithCustomSizedTexture(boardX + l.x, boardY + l.y, 504, 506, LADDER_WIDTH, 2, 512, 512);
				this.drawModalRectWithCustomSizedTexture(boardX + l.x, boardY + l.y + l.length - 2, 504, 508, LADDER_WIDTH, 2, 512, 512);
			}
			
			// Donkey Kong
			this.drawModalRectWithCustomSizedTexture(boardX + 24, boardY + 52, 0, GUI_HEIGHT, KONG_WIDTH, KONG_HEIGHT, 512, 512);
			
			// Girl
			this.drawModalRectWithCustomSizedTexture(boardX + 88, boardY + 34, GUI_WIDTH + LADDER_WIDTH, BARREL_TOP_HIEGHT, GIRL_WIDTH, GIRL_HEIGHT, 512, 512);
			
			// Mario
			player.draw();
		}
	}
	
	protected void keyTyped (char typedChar, int keyCode) throws IOException {
		if (inMenu) {
		
		} else {
			if (keyCode == KeyHandler.left.getKeyCode()) player.setDirection(Direction.LEFT);
			if (keyCode == KeyHandler.right.getKeyCode()) player.setDirection(Direction.RIGHT);
			if (keyCode == KeyHandler.jump.getKeyCode()) player.setDirection(Direction.JUMP);
			if (keyCode == KeyHandler.up.getKeyCode()) player.setDirection(Direction.UP);
		}
		super.keyTyped(typedChar, keyCode);
	}
	
	private void drawPlatforms () {
		for (int i = 0; i < 7; i++) { // Plat 0
			drawPlatform(i * PLATFORM_WIDTH, 248);
			drawPlatform(PLATFORM_WIDTH * 7 + i * PLATFORM_WIDTH, 247 - i);
		}
		
		for (int i = 0; i < 13; i++) {
			drawPlatform(i * PLATFORM_WIDTH, 208 + i); // Plat 1
			drawPlatform(i * PLATFORM_WIDTH + PLATFORM_WIDTH, 187 - i); // Plat 2
			drawPlatform(i * PLATFORM_WIDTH, 142 + i); // Plat 3
			drawPlatform(i * PLATFORM_WIDTH + PLATFORM_WIDTH, 121 - i); // Plat 4
		}
		
		// Plat 5
		for (int i = 0; i < 9; i++) drawPlatform(i * PLATFORM_WIDTH, 84);
		for (int i = 0; i < 4; i++) drawPlatform(i * PLATFORM_WIDTH + PLATFORM_WIDTH * 9, 85 + i);
		
		// Plat 6
		for (int i = 0; i < 3; i++) drawPlatform(88 + i * PLATFORM_WIDTH, 56);
	}
	
	private void drawLadders () {
		drawLadder(64, 32);
		drawLadder(80, 32);
		drawLadder(128, 64, 0, 20);
		drawLadder(88, 92, 0, 4);
		drawLadder(88, 104, 0, 13);
		drawLadder(184, 95, 3, 16);
		drawLadder(32, 128, 0, 16);
		drawLadder(72, 126, 2, 20);
		drawLadder(168, 120, 0, 8);
		drawLadder(168, 144, 0, 8);
		drawLadder(64, 154, 2, 6);
		drawLadder(64, 176, 0, 8);
		drawLadder(112, 157, 1, 24);
		drawLadder(184, 161, 1, 16);
		drawLadder(32, 194, 0, 16);
		drawLadder(96, 190, 2, 24);
		drawLadder(80, 221, 1, 3);
		drawLadder(80, 240, 0, 8);
		drawLadder(184, 227, 3, 16);
	}
	
	private void drawLadder (int x, int y) {
		drawLadder(x, y, 0, LADDER_HEIGHT);
	}
	
	private void drawLadder (int x, int y, int offset, int length) {
		this.drawModalRectWithCustomSizedTexture(boardX + x, boardY + y, GUI_WIDTH, BARREL_TOP_HIEGHT + offset, LADDER_WIDTH, length, 512, 512);
	}
	
	private void drawPlatform (int x, int y) {
		this.drawModalRectWithCustomSizedTexture(boardX + x, boardY + y, GUI_WIDTH, 0, PLATFORM_WIDTH, PLATFORM_HEIGHT, 512, 512);
	}
	
	private void drawOil () {
		// Oil Barrel
		this.drawModalRectWithCustomSizedTexture(boardX + 16, boardY + 232, GUI_WIDTH + LADDER_WIDTH + GIRL_WIDTH * 2, BARREL_TOP_HIEGHT, OIL, OIL, 512, 512);
		// Flame
		this.drawModalRectWithCustomSizedTexture(boardX + 17, boardY + 217, GUI_WIDTH + LADDER_WIDTH + GIRL_WIDTH * 2 + OIL + BARREL_WIDTH + barrelAnim * FLAME_WIDTH, BARREL_TOP_HIEGHT, FLAME_WIDTH, FLAME_HIEGHT, 512, 512);
	}
	
	//16x2, 496x510
	//8x2, 504x506-top 504x508-bottom
	private void createCollisionBoxes () {
		for (int i = 0; i < 7; i++) {
			collisionBoxes.add(new Point(i * PLATFORM_WIDTH, 248));
			collisionBoxes.add(new Point(PLATFORM_WIDTH * 7 + i * PLATFORM_WIDTH, 247 - i));
		}
		
		for (int i = 0; i < 13; i++) {
			collisionBoxes.add(new Point(i * PLATFORM_WIDTH, 208 + i));
			collisionBoxes.add(new Point(i * PLATFORM_WIDTH + PLATFORM_WIDTH, 187 - i));
			collisionBoxes.add(new Point(i * PLATFORM_WIDTH, 142 + i));
			collisionBoxes.add(new Point(i * PLATFORM_WIDTH + PLATFORM_WIDTH, 121 - i));
		}
		
		for (int i = 0; i < 9; i++) collisionBoxes.add(new Point(i * PLATFORM_WIDTH, 84));
		for (int i = 0; i < 4; i++) collisionBoxes.add(new Point(i * PLATFORM_WIDTH + PLATFORM_WIDTH * 9, 85 + i));
		
		for (int i = 0; i < 3; i++) collisionBoxes.add(new Point(88 + i * PLATFORM_WIDTH, 56));
	}
	
	private void createLadderCollisionBoxes () {
		ladders.add(new Ladder(64, 32));
		ladders.add(new Ladder(80, 32));
		ladders.add(new Ladder(128, 64, 20));
		ladders.add(new Ladder(88, 92, 4));
		ladders.add(new Ladder(88, 104, 13));
		ladders.add(new Ladder(184, 95, 16));
		ladders.add(new Ladder(32, 128, 16));
		ladders.add(new Ladder(72, 126, 20));
		ladders.add(new Ladder(168, 120, 8));
		ladders.add(new Ladder(168, 144, 8));
		ladders.add(new Ladder(64, 154, 6));
		ladders.add(new Ladder(64, 176, 8));
		ladders.add(new Ladder(112, 157, 24));
		ladders.add(new Ladder(184, 161, 16));
		ladders.add(new Ladder(32, 194, 16));
		ladders.add(new Ladder(96, 190, 24));
		ladders.add(new Ladder(80, 221, 3));
		ladders.add(new Ladder(80, 240, 8));
		ladders.add(new Ladder(184, 227, 16));
	}
	
	private class Player {
		private int lives = 3, x, y, jumpTick = 0;
		private boolean canClimb, isClimbing, isJumping;
		private Direction direction = Direction.STAND;
		
		private Player (){
			this.x = 20;
			this.y = 232;
		}
		
		private Player (int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void move () {
			if (!isOnGround() && !isClimbing) y += 1;
			if (isOnGround()) isClimbing = false;
			
			switch (direction) {
				case RIGHT:
					isClimbing = false;
					if (canMoveRight() && Keyboard.isKeyDown(KeyHandler.right.getKeyCode())) {
						x += 1;
						for (Point p : collisionBoxes) {
							if (x + 8 == p.x && y + 15 == p.y) y -= 1;
						}
					} else setDirection(Direction.STAND);
					break;
				case LEFT:
					isClimbing = false;
					if (canMoveLeft() && Keyboard.isKeyDown(KeyHandler.left.getKeyCode())) {
						x -= 1;
						for (Point p : collisionBoxes) {
							if (x == p.x && y + 15 == p.y) y -= 1;
						}
					} else setDirection(Direction.STAND);
					break;
				case JUMP:
					isClimbing = false;
					if (!canJump() && !isJumping) break;
					isJumping = true;
					if (jumpTick == 0) jumpTick = tickCounter;
					if (tickCounter - jumpTick <= 10) y -= 2;
					else {
						setDirection(Direction.STAND);
						isJumping = false;
						jumpTick = 0;
					}
					break;
				case UP:
					if (canClimbUp() && Keyboard.isKeyDown(KeyHandler.up.getKeyCode())) {
						isClimbing = true;
						y -= 1;
					} else {
						//isClimbing = false;
						setDirection(Direction.STAND);
					}
					break;
				case DOWN:
					if (canClimbDown() && Keyboard.isKeyDown(KeyHandler.down.getKeyCode())) {
						isClimbing = true;
						y += 1;
					} else {
						//isClimbing = false;
						setDirection(Direction.STAND);
					}
					break;
				case STAND:
					break;
			}
		}
		
		public void setDirection (Direction direction) {
			this.direction = direction;
		}
		
		public void draw () {
			drawModalRectWithCustomSizedTexture(boardX + x, boardY + y, 0, GUI_HEIGHT + 3 * KONG_HEIGHT, 16, 16, 512, 512);
		}
		
		public boolean canMoveLeft () {
			return (boardX + x != boardX);
		}
		
		public boolean canMoveRight () {
			return (boardX + x != boardX + 212);
		}
		
		public boolean isOnGround () {
			for (Point p : collisionBoxes) {
				if (x + 8 >= p.x && y + 16 == p.y) return true;
			}
			return false;
		}
		
		public boolean canClimbUp () {
			for (Ladder l : ladders) {
				if ((x + 4 >= l.x && x + 4 <= l.x + LADDER_WIDTH) && (y + 15 >= l.y && y + 15 <= l.y + l.length)) return true;
			}
			return false;
		}
		
		public boolean canClimbDown () {
			for (Ladder l : ladders) {
				if ((x + 4 >= l.x && x + 4 <= l.x + LADDER_WIDTH) && (y + 15 >= l.y && y + 15 <= l.y + l.length)) return true;
			}
			return false;
		}
		
		public boolean canJump () {
			return isOnGround();
		}
		
		public int getX () {
			return x;
		}
		
		public int getY () {
			return y;
		}
	}
	
	private class Barrel {
		private boolean isFlaming;
		private int x, y, rollState;
		
		private Barrel (int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void move () {
		
		}
		
		public void draw () {
		
		}
		
		public boolean canMoveLeft () {
			return false;
		}
		
		public boolean canMoveRight () {
			return false;
		}
		
		public boolean canFall () {
			return false;
		}
		
		public int getX () {
			return x;
		}
		
		public int getY () {
			return y;
		}
	}
	
	private enum Direction {
		STAND(0, 0, 0),
		UP(1, 2, 1),
		DOWN(2, 1, 1),
		LEFT(3, 4, 2),
		RIGHT(4, 3, 2),
		JUMP(5, 0, 1);
		
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
	
	private class Ladder {
		int x, y, length;
		
		public Ladder (int x, int y) {
			this.x = x;
			this.y = y;
			this.length = LADDER_HEIGHT;
		}
		
		public Ladder (int x, int y, int length) {
			this.x = x;
			this.y = y;
			this.length = length;
		}
	}
}
