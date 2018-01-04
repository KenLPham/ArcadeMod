package superhb.arcademod.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import org.lwjgl.input.Keyboard;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import net.minecraft.world.World;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.KeyHandler;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

// TODO: Add sounds?
public class GuiTetrominoes extends GuiArcade {
    // 10x18 Blocks
    // Each shape placed is 17 pts
    // Final shape that makes row is worth 58 pts (41 pts per row)
    // Every 10 rows level goes up 1 (maxing out at 10)
    // Board Size: 130x234
    // Next Piece Area Size: 50x28
    // Side Text: Level (1-10), Rows, Score
    // Reward 1 Ticket per row
    // Cost 2 Coins

    // UV - GUI, Play Block, Preview Block, Down Arrow, Up Arrow, Right Arrow (Below Play Block)
    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/tetrominoes.png");

    // Texture Variables
    private static final int GUI_X = 210;
    private static final int GUI_Y = 254;
    private static final int PLAY_BLOCK = 13;
    private static final int PREVIEW_BLOCK = 11;
    private static final int ARROW_VERTICAL_X = 11;
    private static final int ARROW_VERTICAL_Y = 7;
    private static final int ARROW_HORIZONTAL_X = 7;
    private static final int ARROW_HORIZONTAL_Y = 11;

    // Music Variables
    private boolean playMusic = true;

    // Game Variables
    private int score = 0, row = 0, level = 1;
    private boolean gameOver = false;
    private int rotation = 0;
    private int playX, playY, nextX, nextY;
    private int nextShape = 0, curShape;
    private boolean giveNextPiece = true;
    private Point piecePoint = new Point(3, 0);
    private int[][] board;
    private int[] speed = { 1, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

    private int prevControlTick = 0, controlSpeed = 2;
    private int prevGameTick = 0;

    private final Point[][][] pieces = { // [shape][rotation][block]
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) }, // I - 0
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) }, // J - 1
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) }, // L - 2
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) }
            },
            {
                    { new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2) }, // S - 3
                    { new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(1, 3) },
                    { new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(1, 3) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2) }, // Z - 4
                    { new Point(1, 1), new Point(0, 2), new Point(1, 2), new Point(0, 3) },
                    { new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                    { new Point(1, 1), new Point(0, 2), new Point(1, 2), new Point(0, 3) }
            },
            {
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) }, // O - 5
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) }, // T - 6
                    { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }
            }
    };

    private final float[][] colors = {
            { 0.0F, 1.0F, 1.0F }, // Cyan
            { 0.0F, 0.0F, 1.0F }, // Blue
            { 1.0F, 0.549F, 0.0F }, // Orange
            { 0.0F, 1.0F, 0.0F }, // Green
            { 1.0F, 0.0F, 0.0F }, // Red
            { 1.0F, 1.0F, 0.0F }, // Yellow
            { 1.0F, 0.078F, 0.576F }, // Pink
    };

    public GuiTetrominoes (World world, TileEntityArcade tileEntity, EntityPlayer player) {
        super(world, tileEntity, null, player);
        setGuiSize(GUI_X, GUI_Y, 0.9F);
        setTexture(texture);
        setOffset(-30, 0);
        setButtonPos((GUI_X / 2) - (buttonWidth / 2) - 30, GUI_Y - 32);
        setStartMenu(0);
        setCost(2);

        nextShape = getWorld().rand.nextInt(7);

        board = new int[10][18]; // [x][y]
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 18; y++) {
                board[x][y] = -1;
            }
        }
    }

    // TODO: Make leaderboard menu
    // TODO: Save high score to nbt
    // TODO: Save leaderboard (Top 10) to nbt
    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        playX = xScaled - (GUI_X / 2) + 10;
        playY = yScaled - (GUI_Y / 2) + 10;

        nextX = playX + 140;
        nextY = playY + 8;

        super.drawScreen(mouseX, mouseY, partialTicks);

        int controlWidth = this.fontRendererObj.getStringWidth(I18n.format("option.arcademod:control.locale"));

        // TODO: Make it so that song plays on loop
        // TODO: Make it so you can change the volume through GUI
        if (playMusic && !inMenu) {
            playMusic = false;
            //mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation(Reference.MODID, "theme.tetris")), 1.0F));
        }

        if (inMenu) {
            // TODO: Volume Menu
            switch (menu) {
                case 0: // Main Menu
                    int titleWidth = this.fontRendererObj.getStringWidth(I18n.format("game.arcademod:tetrominoes.name"));
                    int startWidth = this.fontRendererObj.getStringWidth(I18n.format("option.arcademod:start.locale"));

                    this.fontRendererObj.drawString(I18n.format("game.arcademod:tetrominoes.name"), playX + (130 / 2) - (titleWidth / 2), playY + 2, Color.white.getRGB());
                    this.fontRendererObj.drawString(I18n.format("option.arcademod:start.locale"), playX + (130 / 2) - (startWidth / 2), (height / 2), Color.white.getRGB());
                    this.fontRendererObj.drawString(I18n.format("option.arcademod:control.locale"), playX + (130 / 2) - (controlWidth / 2), (height / 2) + 10, Color.white.getRGB());

                    this.mc.getTextureManager().bindTexture(texture);
                    switch (menuOption) {
                        case 0: // Start
                            this.drawTexturedModalRect(playX + (130 / 2) - 40, (height / 2) - 2, GUI_X, PLAY_BLOCK, ARROW_HORIZONTAL_X, ARROW_HORIZONTAL_Y);
                            break;
                        case 1: // Controls
                            this.drawTexturedModalRect(playX + (130 / 2) - 40, (height / 2) + 8, GUI_X, PLAY_BLOCK, ARROW_HORIZONTAL_X, ARROW_HORIZONTAL_Y);
                            break;
                    }

                    // TODO: Leaderboard
                    break;
                case 1: // Level Select
                    int levelWidth = this.fontRendererObj.getStringWidth(String.format("[%d]", level));
                    this.fontRendererObj.drawString(I18n.format("text.arcademod:level_select.tetrominoes.locale"), playX + (130 / 2) - 40, yScaled, Color.white.getRGB());
                    this.fontRendererObj.drawString(String.format("[%d]", level), playX + (130 / 2) + 35 - (levelWidth / 2), yScaled, Color.white.getRGB());

                    this.mc.getTextureManager().bindTexture(texture);
                    this.drawTexturedModalRect(playX + (130 / 2) + 29, yScaled - 10, GUI_X + PLAY_BLOCK + PREVIEW_BLOCK + ARROW_VERTICAL_X, 0, ARROW_VERTICAL_X, ARROW_VERTICAL_Y); // Up Arrow
                    this.drawTexturedModalRect(playX + (130 / 2) + 29, yScaled + 10, GUI_X + PLAY_BLOCK + PREVIEW_BLOCK, 0, ARROW_VERTICAL_X, ARROW_VERTICAL_Y); // Down Arrow

                    // Back
                    this.fontRendererObj.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), playX + 2, yScaled + (GUI_Y / 2) - 20, Color.white.getRGB());
                    break;
                case 2: // Controls
                    this.fontRendererObj.drawString(I18n.format("option.arcademod:control.locale"), playX + (130 / 2) - (controlWidth / 2), playY + 2, Color.white.getRGB());

                    // Controls
                    this.fontRendererObj.drawString("[" + KeyHandler.up.getDisplayName() + "] " + I18n.format("control.arcademod:up.tetrominoes.name"), playX + (130 / 2) - 40, yScaled - 10, Color.white.getRGB());
                    this.fontRendererObj.drawString("[" + KeyHandler.down.getDisplayName() + "] " + I18n.format("control.arcademod:down.tetrominoes.name"), playX + (130 / 2) - 40, yScaled, Color.white.getRGB());
                    this.fontRendererObj.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("control.arcademod:left.tetrominoes.name"), playX + (130 / 2) - 40, yScaled + 10, Color.white.getRGB());
                    this.fontRendererObj.drawString("[" + KeyHandler.right.getDisplayName() + "] " + I18n.format("control.arcademod:right.tetrominoes.name"), playX + (130 / 2) - 40, yScaled + 20, Color.white.getRGB());
                    this.fontRendererObj.drawString("[" + KeyHandler.select.getDisplayName() + "] " + I18n.format("control.arcademod:select.tetrominoes.name"), playX + (130 / 2) - 40, yScaled + 30, Color.white.getRGB());

                    // Back
                    this.fontRendererObj.drawString("[" + KeyHandler.left.getDisplayName() + "] " + I18n.format("option.arcademod:back.name"), playX + 2, yScaled + (GUI_Y / 2) - 20, Color.white.getRGB());
                    break;
                case 3: // Game Over
                    int overWidth = this.fontRendererObj.getStringWidth(I18n.format("text.arcademod:gameover.locale"));
                    this.fontRendererObj.drawString(I18n.format("text.arcademod:gameover.locale"), playX + (130 / 2) - (overWidth / 2), yScaled - 20, Color.white.getRGB());
                    int scoreWidth = this.fontRendererObj.getStringWidth(I18n.format("text.arcademod:score.locale") + ": " + score);
                    this.fontRendererObj.drawString(I18n.format("text.arcademod:score.locale") + ": " + score, playX + (130 / 2) - (scoreWidth / 2), yScaled - 10, Color.white.getRGB());
                    // TODO: Highscore
                    break;
            }

            // Game Over Timer
            if (menu == 3) {
                if (tickCounter >= 60) {
                    tickCounter = 0;
                    checkMenuAfterGameOver();
                    nextShape = getWorld().rand.nextInt(7);
                    score = 0;
                    row = 0;
                    level = 1;
                    rotation = 0;
                    for (int x = 0; x < 10; x++) {
                        for (int y = 0; y < 18; y++) {
                            board[x][y] = -1;
                        }
                    }
                }
            }
        } else {
            if (gameOver) {
                menu = 3;
                giveNextPiece = false;
                inMenu = true;
                gameOver = false;
                giveReward(ArcadeItems.ticket, row);
                // TODO: Send Score to NBT
            }

            if (giveNextPiece) {
                rotation = 0;
                curShape = nextShape;
                nextShape = getWorld().rand.nextInt(7);
                piecePoint = new Point(3, 0);
                giveNextPiece = false;
            }

            // Move Current Piece down or place
            if ((tickCounter - prevGameTick) >= (isKeyDown(KeyHandler.down.getKeyCode()) ? speed[0] : speed[level])) {
                prevGameTick = tickCounter;

                if (canMoveDown()) piecePoint.y++;
                else place();
            }

            // Controls
            if ((tickCounter - prevControlTick) >= controlSpeed) {
                prevControlTick = tickCounter;
                if (isKeyDown(KeyHandler.left.getKeyCode())) {
                    if (canMoveLeft()) piecePoint.x--;
                } else if (isKeyDown(KeyHandler.right.getKeyCode())) {
                    if (canMoveRight()) piecePoint.x++;
                }
            }

            // Draw Tetrominos
            drawTetromino(curShape, rotation, piecePoint.x, piecePoint.y); // Max 17
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 18; y++) {
                    if (board[x][y] != -1) drawBlock(board[x][y], x, y);
                }
            }
            drawPreview(nextShape);

            // Next
            fontRendererObj.drawString(I18n.format("text.arcademod:next.tetrominoes.locale") + ":", xScaled + (GUI_X / 2) - 60, playY, 4210752);

            // Level (1-10)
            fontRendererObj.drawSplitString(I18n.format("text.arcademod:level.tetrominoes.locale") + ": " + level, xScaled + (GUI_X / 2) - 60, nextY + 38, 50, 4210752);

            // Row
            fontRendererObj.drawSplitString(I18n.format("text.arcademod:row.tetrominoes.locale") + ": " + row, xScaled + (GUI_X / 2) - 60, nextY + 63, 50, 4210752);

            // Score
            fontRendererObj.drawSplitString(I18n.format("text.arcademod:score.locale") + ": " + score, xScaled + (GUI_X / 2) - 60, nextY + 88, 50, 4210752);
        }
    }

    // TODO: Add Pause button and menu
    @Override
    protected void keyTyped (char typedChar, int keyCode) throws IOException {
        if (keyCode == KeyHandler.up.getKeyCode()) { // Up/Rotate Forward
            if (inMenu) {
                if (menu == 0) { // Start
                    if (menuOption == 0) menuOption = 1;
                    else menuOption--;
                } else if (menu == 1) { // Level Select
                    if (level != 10) level++;
                }
            } else {
                if (canRotate()) {
                    if (rotation == 3) rotation = 0;
                    else rotation++;
                }
            }
        }
        if (keyCode == KeyHandler.down.getKeyCode()) { // Down
            if (inMenu) {
                if (menu == 0) { // Start
                    if (menuOption == 1) menuOption = 0;
                    else menuOption++;
                } else if (menu == 1) { // Level Select
                    if (level != 1) level--;
                }
            }
        }
        if (keyCode == KeyHandler.left.getKeyCode()) { // Left/Back
            if (inMenu) {
                if (menu == 1 || menu == 2) menu = 0; // Level Select or Control Menu
            }
        }
        if (keyCode == KeyHandler.right.getKeyCode()) { // Right
            if (inMenu) {
            }
        }
        if (keyCode == KeyHandler.select.getKeyCode()) { // Select
            if (inMenu) {
                if (menu == 0) { // Start
                    switch (menuOption) {
                        case 0: // Start
                            menu = 1;
                            break;
                        case 1: // Controls
                            menu = 2;
                            break;
                    }
                } else if (menu == 1) {
                    inMenu = false;

                    if ((tickCounter- prevGameTick) >= 1) {
                        prevGameTick = tickCounter;
                        canGetCoinBack = false;
                        giveNextPiece = true;
                    }
                }
            }
        }
        if (keyCode == 1) { // Esc
            if (!inMenu) giveReward(ArcadeItems.ticket, row);
            // TODO: Only Stop Arcade Machine Sounds
            //mc.getSoundHandler().stopSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation(Reference.MODID, "theme.tetris")), 1));
            //mc.getSoundHandler().stopSounds();
        }
        super.keyTyped(typedChar, keyCode);
    }

    // TODO: Better check
    private void checkLevel () {
        if (row >= 10 && level == 1) level = 2;
        else if (row >= 20 && level == 2) level = 3;
        else if (row >= 30 && level == 3) level = 4;
        else if (row >= 40 && level == 4) level = 5;
        else if (row >= 50 && level == 5) level = 6;
        else if (row >= 60 && level == 6) level = 7;
        else if (row >= 70 && level == 7) level = 8;
        else if (row >= 80 && level == 8) level = 9;
        else if (row >= 90 && level == 9) level = 10;
    }

    private void drawTetromino (int shape, int rotation, int x, int y) {
        GlStateManager.color(colors[shape][0], colors[shape][1], colors[shape][2]);
        this.mc.getTextureManager().bindTexture(texture);

        for (int i = 0; i < 4; i++) this.drawTexturedModalRect(playX + (x * PLAY_BLOCK) + (pieces[shape][rotation][i].x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) + (pieces[shape][rotation][i].y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
    }

    private void drawBlock (int shape, int x, int y) {
        GlStateManager.color(colors[shape][0], colors[shape][1], colors[shape][2]);
        this.mc.getTextureManager().bindTexture(texture);

        this.drawTexturedModalRect(playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK);
    }

    private void drawPreview (int shape) {
        int[][] pos = {
                { 3, -2 }, // I
                { 8, -8 }, // J
                { 8, -8 }, // L
                { 8, -8 }, // S
                { 8, -8 }, // Z
                { 14, -8 }, // O
                { 8, -8 } // T
        };

        GlStateManager.color(colors[shape][0], colors[shape][1], colors[shape][2]);
        this.mc.getTextureManager().bindTexture(texture);
        for (int i = 0; i < 4; i++) this.drawTexturedModalRect(nextX + pos[shape][0] + (pieces[shape][0][i].x * PREVIEW_BLOCK), nextY + pos[shape][1] + (pieces[shape][0][i].y * PREVIEW_BLOCK), (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK);
    }

    private boolean canRotate () {
        int nextRot = 0;

        if (rotation == 3) nextRot = 0;
        else nextRot = rotation + 1;

        // Top Check
        if (piecePoint.y == 0) {
            // S, Z, O can rotate at y = 0
            if (curShape < 3 || curShape == 6) return false;
        }
        for (int i = 0; i < 4; i++) {
            // Side Check
            if ((piecePoint.x + pieces[curShape][nextRot][i].x) < 0 || (piecePoint.x + pieces[curShape][nextRot][i].x) > 9) return false;
            // Piece Check
            if (board[piecePoint.x + pieces[curShape][nextRot][i].x][piecePoint.y + pieces[curShape][nextRot][i].y - 1] != -1) return false;
        }
        return true;
    }

    private boolean canMoveLeft () {
        for (int i = 0; i < 4; i++) {
            if ((piecePoint.x + pieces[curShape][rotation][i].x) == 0) return false;
            else {
                if (board[piecePoint.x + pieces[curShape][rotation][i].x - 1][piecePoint.y + pieces[curShape][rotation][i].y - 1] != -1) return false;
            }
        }
        return true;
    }

    private boolean canMoveRight () {
        for (int i = 0; i < 4; i++) {
            if ((piecePoint.x + pieces[curShape][rotation][i].x) == 9) return false;
            else {
                if (board[piecePoint.x + pieces[curShape][rotation][i].x + 1][piecePoint.y + pieces[curShape][rotation][i].y - 1] != -1) return false;
            }
        }
        return true;
    }

    private boolean canMoveDown () {
        for (int i = 0; i < 4; i++) {
            if ((piecePoint.y + pieces[curShape][rotation][i].y - 1) == 17) return false;
            else {
                if (board[piecePoint.x + pieces[curShape][rotation][i].x][piecePoint.y + pieces[curShape][rotation][i].y] != -1) return false;
            }
        }
        return true;
    }

    private void place () {
        for (int i = 0; i < 4; i++) {
            board[piecePoint.x + pieces[curShape][rotation][i].x][piecePoint.y + pieces[curShape][rotation][i].y - 1] = curShape;
        }
        for (int x = 0; x < 10; x++) {
            if (board[x][0] != -1) {
                gameOver = true;
                return;
            }
        }
        score += 17;
        checkForRow();
        giveNextPiece = true;
    }

    private void checkForRow () {
        ArrayList boardList = new ArrayList();
        for (int i = 0; i < 18; i++) {
            boardList.add(i, new int[] { board[0][i], board[1][i], board[2][i], board[3][i], board[4][i], board[5][i], board[6][i], board[7][i], board[8][i], board[9][i]});
        }

        for (int y = 0; y < 18; y++) {
            if (board[0][y] != -1 && board[1][y] != -1 && board[2][y] != -1 && board[3][y] != -1 && board[4][y] != -1 && board[5][y] != -1 && board[6][y] != -1 && board[7][y] != -1 && board[8][y] != -1 && board[9][y] != -1) {
                row++;
                checkLevel();
                score += 41;
                boardList.remove(y);
                boardList.add(0, new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });
            }
        }

        for (int y = 0; y < 18; y++) {
            int[] boardX = (int[])boardList.get(y);
            for (int x = 0; x < 10; x++) {
                board[x][y] = boardX[x];
            }
        }
    }
}
