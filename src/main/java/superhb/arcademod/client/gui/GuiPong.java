package superhb.arcademod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.tileentity.TileEntityArcade;

import javax.annotation.Nullable;
import java.io.IOException;

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
	
	// Board Variables
	private int boardX, boardY;
	
	// Game Variables
	private int playerScore, opponentScore;
	
	public GuiPong (World world, TileEntityArcade tileEntity, @Nullable BlockPos pos, EntityPlayer player) {
		super(world, tileEntity, pos, player);
		setGuiSize(GUI_X, GUI_Y, 1);
		setTexture(texture, 512, 512);
		setCost(1);
		//setOffset(0, 0);
		//setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
		setStartMenu(0);
		
		inMenu = false; // TODO: Remove
	}
	
	@Override
	public void updateScreen () {
		super.updateScreen();
	}
	
	@Override
	public void drawScreen (int mouseX, int mouseY, float partialTicks) {
		boardX = xScaled - (GUI_X / 2) + 8;
		boardY = yScaled - (GUI_Y / 2) + 8;
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (inMenu) {
		} else {
			this.drawModalRectWithCustomSizedTexture(boardX, boardY, GUI_X, 0, OUTLINE_X, OUTLINE_Y, 512, 512);
			//drawScore();
		}
	}
	
	// TODO: Redo tp support double digit numbers
	private void drawScore () {
		this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) - 30, boardY + 7, (12 * playerScore), GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
		this.drawModalRectWithCustomSizedTexture(boardX + (BOARD_X / 2) + 14, boardY + 7, (12 * opponentScore), GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
	}
	
	private void drawPaddle() {
	}
	
	private void drawBall () {
	}
	
	@Override
	protected void keyTyped (char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
	}
}
