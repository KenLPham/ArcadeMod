package superhb.arcademod.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import net.minecraft.world.World;
import superhb.arcademod.tileentity.TileEntityArcade;
import superhb.arcademod.util.KeyHandler;

import java.io.IOException;

// TODO: Redo all of this
public class GuiTetrominoes extends GuiArcade {
    // 10x18 Blocks
    // Each shape placed is 17 pts
    // Final shape that makes row is worth 58 pts
    // Board Size: 130x234
    // Next Piece Area Size: 50x28
    // Side Text: Level (1-10), Rows, Score

    // UV - GUI, Play Block, Preview Block, Down Arrow, Up Arrow
    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/tetrominoes.png");

    private static final int GUI_X = 210;
    private static final int GUI_Y = 254;
    private static final int PLAY_BLOCK = 13;
    private static final int PREVIEW_BLOCK = 11;
    private static final int ARROW_X = 11;
    private static final int ARROW_Y = 7;

    private TetrominoPos[] shapePos = new TetrominoPos[45];
    private int score = 0, row = 0, level = 1;
    private boolean start = true, gameOver = false;
    private int rotation = 0;
    private int playX, playY, nextX, nextY;
    private char nextShape = 'Z', curShape;
    private boolean giveNextPiece = true;
    private int pieceID = 0, curPieceID;
    private int[] startPos = { 5, 0 };

    public GuiTetrominoes (World world, TileEntityArcade tileEntity, EntityPlayer player) {
        super(world, tileEntity, player);
        setGuiSize(GUI_X, GUI_Y);
        setTexture(texture);

        nextShape = randChar();
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        playX = (width / 2) - (GUI_X / 2) + 10;
        playY = (height / 2) - (GUI_Y / 2) + 10;

        nextX = playX + 140;
        nextY = playY + 8;

        inMenu = false;

        super.drawScreen(mouseX, mouseY, partialTicks);
        /*
        if (inMenu) {
            switch (menu) {
                case 0: // Main Menu
                    break;
                case 1: // Level Select
                    break;
                case 2: // Controls
                    break;
                case 3: // Game Over
                    break;
            }
        }

        if (menu == 3) {
            tickCounter++;
            if (tickCounter == 500) {
                tickCounter = 0;
                checkMenuAfterGameOver(0);

            }
        }
        */

        // If y = 17 or about to intersect set Shape pos, increase pieceID

        // TODO: after one piece hits bottom this is called until array max is reached. BAD
        if (giveNextPiece) {
            curPieceID = pieceID;
            pieceID++;
            curShape = nextShape;
            nextShape = randChar();
            rotation = 0;
            shapePos[curPieceID] = new TetrominoPos();
            shapePos[curPieceID].addTetrominoPos(curShape, startPos, rotation);
            giveNextPiece = false;
        }

        /*
        tick = 20; // TODO: Move
        tickCounter++;
        if (tickCounter == tick) {
            tickCounter = 0;
            // TODO: Proper check
            if (shapePos[curPieceID].getPosY() != 17) {
                shapePos[curPieceID].setPosY(shapePos[curPieceID].getPosY() + 1);
            }
        }


        // TODO: proper check
        if (shapePos[curPieceID].getPosY() == 17) {
            giveNextPiece = true;
        }
        */

        drawTetromino(curShape, shapePos[curPieceID].getPosX(), shapePos[curPieceID].getPosY(), rotation); // Max 17
        drawPreview(nextShape);

        // Next
        fontRendererObj.drawString(I18n.format("text.arcademod:next.tetrominoes.locale") + ":", (width / 2) + (GUI_X / 2) - 60, playY, 4210752);

        // Level (1-10)
        fontRendererObj.drawSplitString(I18n.format("text.arcademod:level.tetrominoes.locale") + ": " + level, (width / 2) + (GUI_X / 2) - 60, nextY + 38, 50, 4210752);

        // Row
        fontRendererObj.drawSplitString(I18n.format("text.arcademod:row.tetrominoes.locale") + ": " + row, (width / 2) + (GUI_X / 2) - 60, nextY + 63, 50, 4210752);

        // Score
        fontRendererObj.drawSplitString(I18n.format("text.arcademod:score.locale") + ": " + score, (width / 2) + (GUI_X / 2) - 60, nextY + 88, 50, 4210752);
    }

    @Override
    protected void keyTyped (char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == KeyHandler.up.getKeyCode()) { // Up/Rotate Forward
            if (inMenu) {
            } else {
                if (canRotate(0, 0)) {
                    if (rotation == 3) rotation = 0;
                    else rotation += 1;
                    shapePos[curPieceID].setRotation(rotation);
                }
            }
        }
        if (keyCode == KeyHandler.down.getKeyCode()) { // Down/Speed Up
        }
        if (keyCode == KeyHandler.left.getKeyCode()) { // Left/Back
            if (canMoveLeft()) shapePos[curPieceID].setPosX(shapePos[curPieceID].getPosX() - 1);
        }
        if (keyCode == KeyHandler.right.getKeyCode()) { // Right
            if (canMoveRight()) shapePos[0].setPosX(shapePos[0].getPosX() + 1);
        }
        if (keyCode == KeyHandler.select.getKeyCode()) { // Select
            // TODO: Remove
            nextShape = randChar();
        }
    }

    // TODO: Redo Rotation Mechanic
    private void drawTetromino (char shape, int x, int y, int rotation) {
        if (shape == 'I') GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F); // Cyan -- I
        else if (shape == 'O') GlStateManager.color(1.0F, 1.0F, 0.0F, 1.0F); // Yellow -- O
        else if (shape == 'T') GlStateManager.color(1.0F, 0.078F, 0.576F, 1.0F); // Pink -- T
        else if (shape == 'J') GlStateManager.color(0.0F, 0.0F, 1.0F, 1.0F); // Blue -- J
        else if (shape == 'L') GlStateManager.color(1.0F, 0.549F, 0.0F, 1.0F); // Orange -- L
        else if (shape == 'S') GlStateManager.color(0.0F, 1.0F, 0.0F, 1.0F); // Green -- S
        else if (shape == 'Z') GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F); // Red -- Z
        this.mc.getTextureManager().bindTexture(texture);

        switch (shape) {
            case 'I':
                if (rotation == 0 || rotation == 2) { // OG
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK) - PLAY_BLOCK, playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 1 || rotation == 3) {
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), (PLAY_BLOCK * 2) + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                }
                break;
            case 'O':
                this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                break;
            case 'T':
                if (rotation == 0) {
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK) - PLAY_BLOCK, PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), (PLAY_BLOCK * 2) + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 1) {
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), (PLAY_BLOCK * 2) + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK) - PLAY_BLOCK, PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 2) { // OG
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK) - PLAY_BLOCK, PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 3) {
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), (PLAY_BLOCK * 2) + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                }
                break;
            case 'J':
                if (rotation == 0) {
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 1) {
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 2) { // OG
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 3) {
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                }
                break;
            case 'L':
                if (rotation == 0) {
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 1) {
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 2) { // OG
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 3) {
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                }
                break;
            case 'S':
                if (rotation == 0 || rotation == 2) { // OG
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 1 || rotation == 3) {
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), (PLAY_BLOCK * 2) + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                }
                break;
            case 'Z':
                if (rotation == 0 || rotation == 2) { // OG
                    this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                } else if (rotation == 1 || rotation == 3) {
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect(PLAY_BLOCK + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), PLAY_BLOCK + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                    this.drawTexturedModalRect((PLAY_BLOCK * 2) + playX + (x * PLAY_BLOCK), (PLAY_BLOCK * 2) + playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
                }
                break;
        }
    }

    private void drawPreview (char shape) {
        if (shape == 'I') GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F); // Cyan -- I
        else if (shape == 'O') GlStateManager.color(1.0F, 1.0F, 0.0F, 1.0F); // Yellow -- O
        else if (shape == 'T') GlStateManager.color(1.0F, 0.078F, 0.576F, 1.0F); // Pink -- T
        else if (shape == 'J') GlStateManager.color(0.0F, 0.0F, 1.0F, 1.0F); // Blue -- J
        else if (shape == 'L') GlStateManager.color(1.0F, 0.549F, 0.0F, 1.0F); // Orange -- L
        else if (shape == 'S') GlStateManager.color(0.0F, 1.0F, 0.0F, 1.0F); // Green -- S
        else if (shape == 'Z') GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F); // Red -- Z
        this.mc.getTextureManager().bindTexture(texture);

        switch (shape) {
            case 'I':
                this.drawTexturedModalRect(nextX + 3, nextY + 8, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 3, nextY + 8, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 3, nextY + 8, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 3) + nextX + 3, nextY + 8, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
            case 'O':
                this.drawTexturedModalRect(nextX + 14, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 14, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(nextX + 14, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 14, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
            case 'T':
                this.drawTexturedModalRect(nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
            case 'J':
                this.drawTexturedModalRect(nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
            case 'L':
                this.drawTexturedModalRect(nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
            case 'S':
                this.drawTexturedModalRect(nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
            case 'Z':
                this.drawTexturedModalRect(nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, PREVIEW_BLOCK + nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect(PREVIEW_BLOCK + nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                this.drawTexturedModalRect((PREVIEW_BLOCK * 2) + nextX + 8, nextY + 3, (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
                break;
        }
    }

    private char randChar () {
        char[] shapes = { 'I', 'O', 'T', 'J', 'L', 'S', 'Z' };
        return shapes[getWorld().rand.nextInt(7)];
    }

    // TODO remove args
    private boolean canRotate (int x, int y) {
        TetrominoPos pos = shapePos[curPieceID];
        switch (pos.getShape()) {
            case 'I':
                if (pos.getPosY() != 0) {
                    if (pos.getRotation() == 0 || pos.getRotation() == 2) { // 4x1
                        return true;
                    } else if (pos.getRotation() == 1 || pos.getRotation() == 3) { // 1x4
                        if (pos.getPosX() <= 0) return false;
                        else if (pos.getPosX() >= 8) return false;
                        else return true;
                    }
                }
            case 'O':
                break;
            case 'T':
                break;
            case 'J':
                break;
            case 'L':
                break;
            case 'S':
                break;
            case 'Z':
                break;
        }
        return false;
    }

    private boolean canMoveLeft () {
        TetrominoPos pos = shapePos[curPieceID];
        switch (pos.getShape()) {
            case 'I':
                if (pos.getRotation() == 0 || pos.getRotation() == 2) { // 4x1
                    if (pos.getPosX() <= 1) return false;
                    else return true;
                } else if (pos.getRotation() == 1 || pos.getRotation() == 3) { // 1x4
                    if (pos.getPosX() <= 0) return false;
                    else return true;
                }
            case 'O':
                break;
            case 'T':
                break;
            case 'J':
                break;
            case 'L':
                break;
            case 'S':
                break;
            case 'Z':
                if (pos.getRotation() == 0 || pos.getRotation() == 2) {
                    if (pos.getPosX() >= 7) return false;
                    else return true;
                } else if (pos.getRotation() == 1 || pos.getRotation() == 3) {
                    if (pos.getPosX() >= 10) return false;
                    else return true;
                }
        }
        return false;
    }

    // TODO: Remove
    private boolean canMoveLeft (int x, int rotation) {
        if (x == 0) return false;
        else return true;
    }

    private boolean canMoveRight () {
        TetrominoPos pos = shapePos[curPieceID];
        switch (pos.getShape()) {
            case 'I':
                if (pos.getRotation() == 0 || pos.getRotation() == 2) { // 4x1
                    if (pos.getPosX() >= 7) return false;
                    else return true;
                } else if (pos.getRotation() == 1 || pos.getRotation() == 3) { // 1x4
                    if (pos.getPosX() >= 9) return false;
                    else return true;
                } else if (pos.getRotation() == 4) {
                    if (pos.getPosX() == 8) return false;
                    else return true;
                }
            case 'O':
                break;
            case 'T':
                break;
            case 'J':
                break;
            case 'L':
                break;
            case 'S':
                break;
            case 'Z':
                if (pos.getRotation() == 0 || pos.getRotation() == 2) {
                    if (pos.getPosX() >= 8) return false;
                    else return true;
                } else if (pos.getRotation() == 1 || pos.getRotation() == 3) {
                    if (pos.getPosX() >= 6) return false;
                    else return true;
                }
        }
        return false;
    }

    // TODO: Remove
    private boolean canMoveRight (int x, int rotation) {
        return false;
    }

    private boolean canMoveDown () {
        return false;
    }

    private class TetrominoPos {
        private char shape;
        private int[] pos = new int[2];
        private int rotation;

        public void addTetrominoPos (char shape, int[] pos, int rotation) {
            this.shape = shape;
            this.pos = pos;
            this.rotation = rotation;
        }

        public char getShape () {
            return shape;
        }

        public int[] getPos () {
            return pos;
        }

        public int getPosX () {
            return pos[0];
        }

        public int getPosY () {
            return pos[1];
        }

        public int getRotation () {
            return rotation;
        }

        public int[] getShapePos () {
            return new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        }

        public void setPos (int x, int y) {
            pos[0] = x;
            pos[1] = y;
        }

        public void setShape (char shape) {
            this.shape = shape;
        }

        public void setPosX (int x) {
            pos[0] = x;
        }

        public void setPosY (int y) {
            pos[1] = y;
        }

        public void setRotation (int rotation) {
            this.rotation = rotation;
        }
    }
}
