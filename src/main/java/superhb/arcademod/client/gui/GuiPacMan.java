package superhb.arcademod.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.audio.LoopingSound;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.ArcadeSoundRegistry;
import superhb.arcademod.util.KeyHandler;

import java.awt.*;
import java.io.IOException;

// http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php?print=1
// http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior
public class GuiPacMan extends GuiArcade {
    // 1 dot = 10 pt
    // 1 energizer = 50 pt
    // Captured Ghost: 1 = 200; 2 = 400; 3 = 800; 4 = 1200;
    // All ghosts are captured with one energizer, additional 12000 pts
    // Blue time decreases as level goes up
    // Level 19, Ghosts can no longer be eaten
    // Fruit one shows up after 70 dots eaten, Fruit two shows up after 170 dots eaten
    // Fruit worth 100 or 5000 depending on level
    /* Fruits, Level, Worth
        - Level 1: Cherry 100pt
        - Level 2: Strawberry 300pt
        - Level 3 & 4: Peach 500pt
        - Level 5 & 6: Apple 700pt
        - Level 7 & 8: Grapes 1000pt
        - Level 9 & 10: Galaxian (tulip or thunderbird) 2000pt
        - Level 11 & 12: Bell 3000pt
        - Level 13+: Key 5000pt
     */
    // Fruit shows up between 9-10 seconds
    /* Audio Info
        - Siren plays continuously (unless ghosts are frightened)
        - Waka only plays when eating dots / energizer
        - Frightened Sound plays with ghosts are frightened
     */

    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/gui/pacman.png");

    // Texture Variables
    private static final int GUI_X = 234, GUI_Y = 300;
    private static final int MAZE_X = 224, MAZE_Y = 248;

    private static final int GHOST = 14;
    private static final int PUPIL = 2;
    private static final int EYE_X = 4, EYE_Y = 5;
    private static final int MOUTH_X = 12, MOUTH_Y = 2;

    private static final int CHERRY = 12;
    private static final int BERRY_X = 11, BERRY_Y = 12;
    private static final int PEACH_X = 11, PEACH_Y = 12;
    private static final int APPLE = 12;
    private static final int GRAPE_X = 11, GRAPE_Y = 14;
    private static final int GALAXIAN = 11;
    private static final int BELL_X = 12, BELL_Y = 13;
    private static final int KEY_X = 7, KEY_Y = 13;

    private static final int PACMAN = 15;

    private static final int DOT = 2;
    private static final int ENERGIZER = 8;

    // Animation Variables
    private int PACMAN_STATE = 0;
    private int FRIGHT_STATE = 0;
    private int BLINK_SPEED = 2;
    private int GHOST_STATE = 0;
    private int ENERGIZER_STATE = 0;

    // TODO: Audio
    // Audio Variables
    private int WAKA = 0;
    private int ENERGIZER_SOUND = 0;
    private float volume = 1.0F;
    private boolean playSiren = true;

    // Game Variables
    private boolean died = false;
    private int life = 3;
    private byte level = 0;
    private int score = 0;
    private final int speedMultiplier = 4;
    private final int ghostSpeedMultiplier = 4;
    private boolean blink = false;

    /*
        0 = Pac Man
        1 = Blinky
        2 = Inky
        3 = Pinky
        4 = Clyde
     */
    private int[] direction = { 0, 0, 1, 1, 1 };
    private int nextDirection = 0;

    // Ghost Variables
    private int[] mode = { 0, 0, 0, 0 };
    private int[] period = { 0, 0, 0, 0 };
    private boolean[] frightened = { false, false, false, false };
    private boolean[] eaten = { false, false, false, false };
    private boolean[] inHouse = { false, true, true, true };
    private int[] dotCounter = { 0, 0, 0, 0, 0 }; // dotCounter[4] = Global Dot Counter
    private int curCounter = 0;

    private int playX, playY;
    private int playerX = 25, playerY = 44;
    private int[][] ghostPos = {
            { 25, 20 }, // Blinky
            { 21, 26 }, // Inky
            { 25, 26 }, // Pinky
            { 29, 26 } // Clyde
    };

    private int blinkTick = 0;
    private int frightTick = 0;
    private int animationTick = 0, animationSpeed = 1;
    private int energizerTick = 0;
    private int ghostTick = 0, ghostSpeed = 1;
    private int ghostMovementTick[] = { 0, 0, 0, 0 };
    private int movementTick = 0, movementSpeed = 1;
    private int modeTick[] = { 0, 0, 0, 0 };
    private int timeSinceEaten = 0;

    private boolean pauseModeTimer = false;

    private final Color[] ghostColor = {
            new Color(255, 7, 7), // Blinky
            new Color(7, 255, 255), // Inky
            new Color(255, 184, 222), // Pinky
            new Color(255, 159, 7), // Clyde
            new Color(255, 255, 255),
            new Color(7, 7, 255)
    };

    private final Color[] eyeColor = {
            new Color(33, 33, 222),
            new Color(245, 245, 255),
            new Color(255, 15, 15)
    };

    private final EdiblePoint[] ediblePoint = new EdiblePoint[] {
            // Dots
                // Row 1
            new EdiblePoint(0, 0),
            new EdiblePoint(1, 0),
            new EdiblePoint(2, 0),
            new EdiblePoint(3, 0),
            new EdiblePoint(4, 0),
            new EdiblePoint(5, 0),
            new EdiblePoint(6, 0),
            new EdiblePoint(7, 0),
            new EdiblePoint(8, 0),
            new EdiblePoint(9, 0),
            new EdiblePoint(10, 0),
            new EdiblePoint(11, 0),

            new EdiblePoint(14, 0),
            new EdiblePoint(15, 0),
            new EdiblePoint(16, 0),
            new EdiblePoint(17, 0),
            new EdiblePoint(18, 0),
            new EdiblePoint(19, 0),
            new EdiblePoint(20, 0),
            new EdiblePoint(21, 0),
            new EdiblePoint(22, 0),
            new EdiblePoint(23, 0),
            new EdiblePoint(24, 0),
            new EdiblePoint(25, 0),
                // Row 2
            new EdiblePoint(0, 1),
            new EdiblePoint(5, 1),
            new EdiblePoint(11, 1),
            new EdiblePoint(14, 1),
            new EdiblePoint(20, 1),
            new EdiblePoint(25, 1),
                // Row 3
            new EdiblePoint(5, 2),
            new EdiblePoint(11, 2),
            new EdiblePoint(14, 2),
            new EdiblePoint(20, 2),
                // Row 4
            new EdiblePoint(0, 3),
            new EdiblePoint(5, 3),
            new EdiblePoint(11, 3),
            new EdiblePoint(14, 3),
            new EdiblePoint(20, 3),
            new EdiblePoint(25, 3),
                // Row 5
            new EdiblePoint(0, 4),
            new EdiblePoint(1, 4),
            new EdiblePoint(2, 4),
            new EdiblePoint(3, 4),
            new EdiblePoint(4, 4),
            new EdiblePoint(5, 4),
            new EdiblePoint(6, 4),
            new EdiblePoint(7, 4),
            new EdiblePoint(8, 4),
            new EdiblePoint(9, 4),
            new EdiblePoint(10, 4),
            new EdiblePoint(11, 4),
            new EdiblePoint(12, 4),
            new EdiblePoint(13, 4),
            new EdiblePoint(14, 4),
            new EdiblePoint(15, 4),
            new EdiblePoint(16, 4),
            new EdiblePoint(17, 4),
            new EdiblePoint(18, 4),
            new EdiblePoint(19, 4),
            new EdiblePoint(20, 4),
            new EdiblePoint(21, 4),
            new EdiblePoint(22, 4),
            new EdiblePoint(23, 4),
            new EdiblePoint(24, 4),
            new EdiblePoint(25, 4),
                // Row 6
            new EdiblePoint(0, 5),
            new EdiblePoint(5, 5),

            new EdiblePoint(8, 5),
            new EdiblePoint(17, 5),

            new EdiblePoint(20, 5),
            new EdiblePoint(25, 5),
                // Row 7
            new EdiblePoint(0, 6),
            new EdiblePoint(5, 6),

            new EdiblePoint(8, 6),
            new EdiblePoint(17, 6),

            new EdiblePoint(20, 6),
            new EdiblePoint(25, 6),
                // Row 8
            new EdiblePoint(0, 7),
            new EdiblePoint(1, 7),
            new EdiblePoint(2, 7),
            new EdiblePoint(3, 7),
            new EdiblePoint(4, 7),
            new EdiblePoint(5, 7),

            new EdiblePoint(8, 7),
            new EdiblePoint(9, 7),
            new EdiblePoint(10, 7),
            new EdiblePoint(11, 7),

            new EdiblePoint(14, 7),
            new EdiblePoint(15, 7),
            new EdiblePoint(16, 7),
            new EdiblePoint(17, 7),

            new EdiblePoint(20, 7),
            new EdiblePoint(21, 7),
            new EdiblePoint(22, 7),
            new EdiblePoint(23, 7),
            new EdiblePoint(24, 7),
            new EdiblePoint(25, 7),
                // Row 9
            new EdiblePoint(5, 8),
            new EdiblePoint(20, 8),
                // Row 10
            new EdiblePoint(5, 9),
            new EdiblePoint(20, 9),
                // Row 11
            new EdiblePoint(5, 10),
            new EdiblePoint(20, 10),
                // Row 12
            new EdiblePoint(5, 11),
            new EdiblePoint(20, 11),
                // Row 13
            new EdiblePoint(5, 12),
            new EdiblePoint(20, 12),
                // Row 14
            new EdiblePoint(5, 13),
            new EdiblePoint(20, 13),
                // Row 15
            new EdiblePoint(5, 14),
            new EdiblePoint(20, 14),
                // Row 16
            new EdiblePoint(5, 15),
            new EdiblePoint(20, 15),
                // Row 17
            new EdiblePoint(5, 16),
            new EdiblePoint(20, 16),
                // Row 18
            new EdiblePoint(5, 17),
            new EdiblePoint(20, 17),
                // Row 19
            new EdiblePoint(5, 18),
            new EdiblePoint(20, 18),
                // Row 20
            new EdiblePoint(0, 19),
            new EdiblePoint(1, 19),
            new EdiblePoint(2, 19),
            new EdiblePoint(3, 19),
            new EdiblePoint(4, 19),
            new EdiblePoint(5, 19),
            new EdiblePoint(6, 19),
            new EdiblePoint(7, 19),
            new EdiblePoint(8, 19),
            new EdiblePoint(9, 19),
            new EdiblePoint(10, 19),
            new EdiblePoint(11, 19),

            new EdiblePoint(14, 19),
            new EdiblePoint(15, 19),
            new EdiblePoint(16, 19),
            new EdiblePoint(17, 19),
            new EdiblePoint(18, 19),
            new EdiblePoint(19, 19),
            new EdiblePoint(20, 19),
            new EdiblePoint(21, 19),
            new EdiblePoint(22, 19),
            new EdiblePoint(23, 19),
            new EdiblePoint(24, 19),
            new EdiblePoint(25, 19),
                // Row 21
            new EdiblePoint(0, 20),
            new EdiblePoint(5, 20),
            new EdiblePoint(11, 20),

            new EdiblePoint(14, 20),
            new EdiblePoint(20, 20),
            new EdiblePoint(25, 20),
                // Row 22
            new EdiblePoint(0, 21),
            new EdiblePoint(5, 21),
            new EdiblePoint(11, 21),

            new EdiblePoint(14, 21),
            new EdiblePoint(20, 21),
            new EdiblePoint(25, 21),
                // Row 23
            new EdiblePoint(1, 22),
            new EdiblePoint(2, 22),

            new EdiblePoint(5, 22),
            new EdiblePoint(6, 22),
            new EdiblePoint(7, 22),
            new EdiblePoint(8, 22),
            new EdiblePoint(9, 22),
            new EdiblePoint(10, 22),
            new EdiblePoint(11, 22),

            new EdiblePoint(14, 22),
            new EdiblePoint(15, 22),
            new EdiblePoint(16, 22),
            new EdiblePoint(17, 22),
            new EdiblePoint(18, 22),
            new EdiblePoint(19, 22),
            new EdiblePoint(20, 22),

            new EdiblePoint(23, 22),
            new EdiblePoint(24, 22),
                // Row 24
            new EdiblePoint(2, 23),
            new EdiblePoint(5, 23),
            new EdiblePoint(8, 23),

            new EdiblePoint(17, 23),
            new EdiblePoint(20, 23),
            new EdiblePoint(23, 23),
                // Row 25
            new EdiblePoint(2, 24),
            new EdiblePoint(5, 24),
            new EdiblePoint(8, 24),

            new EdiblePoint(17, 24),
            new EdiblePoint(20, 24),
            new EdiblePoint(23, 24),
                // Row 26
            new EdiblePoint(0, 25),
            new EdiblePoint(1, 25),
            new EdiblePoint(2, 25),
            new EdiblePoint(3, 25),
            new EdiblePoint(4, 25),
            new EdiblePoint(5, 25),

            new EdiblePoint(8, 25),
            new EdiblePoint(9, 25),
            new EdiblePoint(10, 25),
            new EdiblePoint(11, 25),

            new EdiblePoint(14, 25),
            new EdiblePoint(15, 25),
            new EdiblePoint(16, 25),
            new EdiblePoint(17, 25),

            new EdiblePoint(20, 25),
            new EdiblePoint(21, 25),
            new EdiblePoint(22, 25),
            new EdiblePoint(23, 25),
            new EdiblePoint(24, 25),
            new EdiblePoint(25, 25),
                // Row 27
            new EdiblePoint(0, 26),
            new EdiblePoint(11, 26),

            new EdiblePoint(14, 26),
            new EdiblePoint(25, 26),
                // Row 28
            new EdiblePoint(0, 27),
            new EdiblePoint(11, 27),

            new EdiblePoint(14, 27),
            new EdiblePoint(25, 27),
                // Row 29
            new EdiblePoint(0, 28),
            new EdiblePoint(1, 28),
            new EdiblePoint(2, 28),
            new EdiblePoint(3, 28),
            new EdiblePoint(4, 28),
            new EdiblePoint(5, 28),
            new EdiblePoint(6, 28),
            new EdiblePoint(7, 28),
            new EdiblePoint(8, 28),
            new EdiblePoint(9, 28),
            new EdiblePoint(10, 28),
            new EdiblePoint(11, 28),
            new EdiblePoint(12, 28),
            new EdiblePoint(13, 28),
            new EdiblePoint(14, 28),
            new EdiblePoint(15, 28),
            new EdiblePoint(16, 28),
            new EdiblePoint(17, 28),
            new EdiblePoint(18, 28),
            new EdiblePoint(19, 28),
            new EdiblePoint(20, 28),
            new EdiblePoint(21, 28),
            new EdiblePoint(22, 28),
            new EdiblePoint(23, 28),
            new EdiblePoint(24, 28),
            new EdiblePoint(25, 28),

            // Energizer
            new EdiblePoint(0, 2, true),
            new EdiblePoint(25, 2, true),
            new EdiblePoint(0, 22, true),
            new EdiblePoint(25, 22, true)
    };
    private EdiblePoint[] tempPoints;

    private final MazeCollision[] mazeCollision = new MazeCollision[] {
            new MazeCollision(0, 0, 223, 3),
            new MazeCollision(108, 3, 8, 32),
            new MazeCollision(220, 3, 3, 76),
            new MazeCollision(20, 19, 24, 16),
            new MazeCollision(60, 19, 32, 16),
            new MazeCollision(132, 19, 32, 16),
            new MazeCollision(180, 19, 24, 16),
            new MazeCollision(20, 51, 24, 8),
            new MazeCollision(60, 51, 8, 56),
            new MazeCollision(68, 75, 24, 8),
            new MazeCollision(84, 51, 56, 8),
            new MazeCollision(108, 59, 8, 24),
            new MazeCollision(156, 51, 8, 56),
            new MazeCollision(132, 75, 24, 8),
            new MazeCollision(180, 51, 24, 8),
            new MazeCollision(0, 3, 4, 76),
            new MazeCollision(4, 75, 40, 4),
            new MazeCollision(40, 79, 4, 28),
            new MazeCollision(0, 104, 40, 3),
            new MazeCollision(0, 123, 44, 4),
            new MazeCollision(40, 127, 4, 28),
            new MazeCollision(0, 152, 40, 3),
            new MazeCollision(180, 75, 40, 4),
            new MazeCollision(180, 79, 3, 28),
            new MazeCollision(183, 104, 40, 3),
            new MazeCollision(180, 123, 43, 4),
            new MazeCollision(180, 127, 3, 28),
            new MazeCollision(183, 152, 40, 3),
            new MazeCollision(220, 155, 3, 92),
            new MazeCollision(204, 195, 16, 8),
            new MazeCollision(0, 155, 4, 92),
            new MazeCollision(4, 195, 16, 8),
            new MazeCollision(60, 123, 8, 32),
            new MazeCollision(156, 123, 8, 32),
            new MazeCollision(84, 147, 56, 8),
            new MazeCollision(108, 155, 8, 24),
            new MazeCollision(20, 171, 24, 8),
            new MazeCollision(36, 179, 8, 24),
            new MazeCollision(60, 171, 32, 8),
            new MazeCollision(132, 171, 32, 8),
            new MazeCollision(180, 171, 24, 8),
            new MazeCollision(180, 179, 8, 24),
            new MazeCollision(60, 195, 8, 24),
            new MazeCollision(20, 219, 72, 8),
            new MazeCollision(84, 195, 56, 8),
            new MazeCollision(108, 203, 8, 24),
            new MazeCollision(156, 195, 8, 24),
            new MazeCollision(132, 219, 72, 8),
            new MazeCollision(4, 243, 216, 4),

            // Ghost Cage
            new MazeCollision(84, 99, 19, 4),
            new MazeCollision(84, 103, 3, 28),
            new MazeCollision(87, 128, 49, 3),
            new MazeCollision(136, 99, 4, 32),
            new MazeCollision(120, 99, 16, 4),
            new MazeCollision(104, 99, 15, 4, true)
    };

    // TODO: Add Fruit
    private final GameCollision[] gameCollision = new GameCollision[] {
            new GameCollision(14, 14), // Pac Man
            new GameCollision(GHOST, GHOST), // Ghost
            new GameCollision(2, 2), // Dot
            new GameCollision(8, 8) // Energizer
    };

    public GuiPacMan (World world, TileEntityArcade tileEntity, EntityPlayer player) {
        super (world, tileEntity, player);
        setGuiSize(GUI_X, GUI_Y, 0.8F);
        setTexture(texture, 512, 512);

        tempPoints = ediblePoint;

        // TODO: Remove
        inMenu = false;
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        playX = xScaled - (GUI_X / 2) + 5;
        playY = yScaled - (GUI_Y / 2) + 14;

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.mc.getTextureManager().bindTexture(texture);
        this.drawModalRectWithCustomSizedTexture(xScaled - (GUI_X / 2) + 5, yScaled - (GUI_Y / 2) + 14, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);

        // Maze
        if ((tickCounter - energizerTick) >= 6) {
            energizerTick = tickCounter;

            if (ENERGIZER_STATE == 0) ENERGIZER_STATE = 1;
            else if (ENERGIZER_STATE == 1) ENERGIZER_STATE = 0;
        }
        drawEdibles(false);

        /* Ghost Blink per level and fright time
                Level | Fright Time | # of Blinks (Flashes)
            -   1     | 6 sec       | 5
            -   2     | 5 sec       | 5
            -   3     | 4 sec       | 5
            -   4     | 3 sec       | 5
            -   5     | 2 sec       | 5
            -   6     | 5 sec       | 5
            -   7     | 2 sec       | 5
            -   8     | 2 sec       | 5
            -   9     | 1 sec       | 3
            -   10    | 5 sec       | 5
            -   11    | 2 sec       | 5
            -   12    | 1 sec       | 3
            -   13    | 1 sec       | 3
            -   14    | 3 sec       | 5
            -   15    | 1 sec       | 3
            -   16    | 1 sec       | 3
            -   17    | 0 sec       | 0
            -   18    | 1 sec       | 3
            -   19    | 0 sec       | 0
            -   20    | 0 sec       | 0
            -   21+   | 0 sec       | 0

         */
        // TODO: Scatter, Chase timing
        // Ghost
        if ((tickCounter - blinkTick) >= 5 && blink) {
            blinkTick = tickCounter;

            if (FRIGHT_STATE == 0) FRIGHT_STATE = 1;
            else if (FRIGHT_STATE == 1) FRIGHT_STATE = 0;
        }
        if ((tickCounter - ghostTick) >= 4) {
            ghostTick = tickCounter;

            if (GHOST_STATE == 0) GHOST_STATE = 1;
            else if (GHOST_STATE == 1) GHOST_STATE = 0;
        }
        if ((tickCounter - frightTick) >= 70) blink = true;
        if ((tickCounter - frightTick) >= 90) { // TODO: Change fright speed depending on level
            frightTick = tickCounter;
            for (int i = 0; i < 4; i++) frightened[i] = false;
            blink = false;
        }
        blinkyAi();

        // TODO: Implement AI
        pinkyAi();
        inkyAi();
        clydeAi();

        checkHouse();

        // Maze Debug
        //drawMazeCollision(true);

        // Pac-Man
        if ((tickCounter - movementTick) >= 2) {
            movementTick = tickCounter;

            if (canMoveUp(0) && direction[0] == 1) {
                if (PACMAN_STATE == 0) PACMAN_STATE = 1;
                else PACMAN_STATE = 0;
                playerY--;
            } else if (canMoveDown(0) && direction[0] == 2) {
                if (PACMAN_STATE == 0) PACMAN_STATE = 1;
                else PACMAN_STATE = 0;
                playerY++;
            } else if (canMoveLeft(0) && direction[0] == 3) {
                if (PACMAN_STATE == 0) PACMAN_STATE = 1;
                else PACMAN_STATE = 0;
                playerX--;
            } else if (canMoveRight(0) && direction[0] == 4) {
                if (PACMAN_STATE == 0) PACMAN_STATE = 1;
                else PACMAN_STATE = 0;
                playerX++;
            }
        }
        drawPacman(playerX, playerY, direction[0], false);

        // Pac-Man Collision Check with edibles and ghosts
        checkCenter();

        // Check time since last eaten. 4 second limit for level 1-4. 3 second limit for 5+

        // Controls
        if (canMoveLeft(0) && nextDirection == 3) {
            direction[0] = 3;
            nextDirection = 0;
        } else if (canMoveRight(0) && nextDirection == 4) {
            direction[0] = 4;
            nextDirection = 0;
        } else if (canMoveUp(0) && nextDirection == 1) {
            direction[0] = 1;
            nextDirection = 0;
        } else if (canMoveDown(0) && nextDirection == 2) {
            direction[0] = 2;
            nextDirection = 0;
        }

        // Sound
        if (playSiren) {
            playSiren = false;
            this.mc.getSoundHandler().playSound(new LoopingSound(ArcadeSoundRegistry.PACMAN_SIREN, SoundCategory.BLOCKS)); // Repeats a few times before stopping completely.
        }

        this.fontRendererObj.drawString("Score: " + score, xScaled - (GUI_X / 2) + 6, yScaled - (GUI_Y / 2) + 6, Color.WHITE.getRGB());
        this.fontRendererObj.drawString("Ready!", xScaled - (this.fontRendererObj.getStringWidth("Ready!") / 2), yScaled, Color.YELLOW.getRGB());
    }

    // TODO: Controls
    /* Direction ID
        0 = Still
        1 = Up
        2 = Down
        3 = Left
        4 = Right
     */
    @Override
    protected void keyTyped (char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == KeyHandler.left.getKeyCode()) {
            if (inMenu) {
            } else {
                nextDirection = 3;
            }
        }
        if (keyCode == KeyHandler.right.getKeyCode()) {
            if (inMenu) {
            } else {
                nextDirection = 4;
            }
        }
        if (keyCode == KeyHandler.up.getKeyCode()) {
            if (inMenu) {
            } else {
                nextDirection = 1;
            }
        }
        if (keyCode == KeyHandler.down.getKeyCode()) {
            if (inMenu) {
            } else {
                nextDirection = 2;
            }
        }
    }

    /*
    private int[][] ghostPos = {
            { 25, 20 }, // Blinky
            { 21, 26 }, // Inky
            { 25, 26 }, // Pinky
            { 29, 26 } // Clyde
    };
     */

    // Ghost's individual counter is deactivated (but not reset) when a life is lost
    // The global counter is activated and reset everytime a life is lost
    // Global dot counter release
    // Pinky = 7
    // Inky = 17
    // To deactivate the global counter, Clyde has to be in the house when the counter reaches 32
    // This will reset the global counter to zero and once again use the ghost's separate counter
    private int getDotLimit (int level, int ghost) {
        if (ghost == 2) return 0; // Pinky
        else if (ghost == 1) { // Inky
            if (level == 0) return 30;
            else return 0;
        } else if (ghost == 3) { // Clyde
            if (level == 0) return 60;
            else if (level == 1) return 50;
            else return 0;
        }
        return 0;
    }

    // TODO: Redo
    private void checkHouse () {
        // Some how the check not working is making everything mostly work...
        if (ghostPos[2][0] != 25 && ghostPos[2][1] != 26 && inHouse[2]) { // Pinky
            inHouse[2] = false;
            // Start Inky's dotCounter
            if (inHouse[1]) {
                curCounter = 1;
                ghostPos[1][0] = 25;
            }
        }
        if (ghostPos[1][0] != 25 && ghostPos[1][1] != 26 && inHouse[1]) { // Inky
            inHouse[1] = false;
            // Start Clyde's dotCounter
            if (inHouse[3]) {
                curCounter = 3;
                ghostPos[3][0] = 25;
            }
        }
        if (ghostPos[3][0] != 25 && ghostPos[3][1] != 26 && inHouse[3]) { // Clyde
            inHouse[3] = false;
        }
    }

    public void drawGhost (int x, int y, int direction, int ghost, boolean frightened, boolean eaten, boolean debug) {
        if (!eaten) {
            if (!frightened) {
                float[] body = colorToFloat(ghostColor[ghost]);
                float[] eye = colorToFloat(eyeColor[0]);

                GlStateManager.color(body[0], body[1], body[2]);
                // Body
                if (GHOST_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
                else this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);

                // Eye
                GlStateManager.color(1.0F, 1.0F, 1.0F);
                switch (direction) {
                    case 0: // Still
                        break;
                    case 1: // Up
                        // Eye
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        // Pupil
                        GlStateManager.color(eye[0], eye[1], eye[2]);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        break;
                    case 2: // Down
                        // Eye
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        // Pupil
                        GlStateManager.color(eye[0], eye[1], eye[2]);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        break;
                    case 3: // Left
                        // Eye
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        // Pupil
                        GlStateManager.color(eye[0], eye[1], eye[2]);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        break;
                    case 4: // Right
                        // Eye
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                        // Pupil
                        GlStateManager.color(eye[0], eye[1], eye[2]);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 5, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 7, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                        break;
                }
            } else {
                if (FRIGHT_STATE == 0) { // Blue
                    // Body
                    float[] body = colorToFloat(ghostColor[5]);
                    GlStateManager.color(body[0], body[1], body[2]);
                    if (GHOST_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
                    else this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);

                    // Pupil
                    float[] eye = colorToFloat(eyeColor[1]);
                    GlStateManager.color(eye[0], eye[1], eye[2]);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);

                    // Mouth
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 8, GUI_X, MAZE_Y + GHOST, MOUTH_X, MOUTH_Y, 512, 512);
                } else if (FRIGHT_STATE == 1) { // White
                    // Body
                    float[] body = colorToFloat(ghostColor[4]);
                    GlStateManager.color(body[0], body[1], body[2]);
                    if (GHOST_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
                    else this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);

                    // Pupil
                    float[] eye = colorToFloat(eyeColor[2]);
                    GlStateManager.color(eye[0], eye[1], eye[2]);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);

                    // Mouth
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 8, GUI_X, MAZE_Y + GHOST, MOUTH_X, MOUTH_Y, 512, 512);
                }
            }
        } else {
            float[] eye = colorToFloat(eyeColor[0]);

            // Eye
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            switch (direction) {
                case 0: // Still
                    break;
                case 1: // Up
                    // Eye
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    // Pupil
                    GlStateManager.color(eye[0], eye[1], eye[2]);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    break;
                case 2: // Down
                    // Eye
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    // Pupil
                    GlStateManager.color(eye[0], eye[1], eye[2]);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    break;
                case 3: // Left
                    // Eye
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    // Pupil
                    GlStateManager.color(eye[0], eye[1], eye[2]);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    break;
                case 4: // Right
                    // Eye
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                    // Pupil
                    GlStateManager.color(eye[0], eye[1], eye[2]);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 5, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 7, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                    break;
            }
        }

        if (debug) {
            float[] debugColor = colorToFloat(new Color(230, 30, 100));
            GlStateManager.color(debugColor[0], debugColor[1], debugColor[2]);

            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right

            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].center[0] + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].center[1] + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Center
        }
    }

    public void drawPacman (int x, int y, int direction, boolean debug) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        switch (direction) {
            case 0: // Still
                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), 0, GUI_Y, PACMAN, PACMAN, 512, 512);
                break;
            case 1: // Up
                if (PACMAN_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), 0, GUI_Y , PACMAN, PACMAN, 512, 512);
                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), PACMAN, GUI_Y + (PACMAN * 2), PACMAN, PACMAN, 512, 512);
                break;
            case 2: // Down
                if (PACMAN_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), 0, GUI_Y , PACMAN, PACMAN, 512, 512);
                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), PACMAN, GUI_Y + (PACMAN * 3), PACMAN, PACMAN, 512, 512);
                break;
            case 3: // Left
                if (PACMAN_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), 0, GUI_Y, PACMAN, PACMAN, 512, 512);
                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), PACMAN, GUI_Y + PACMAN, PACMAN, PACMAN, 512, 512);
                break;
            case 4: // Right
                if (PACMAN_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), 0, GUI_Y , PACMAN, PACMAN, 512, 512);
                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), PACMAN, GUI_Y , PACMAN, PACMAN, 512, 512);
                break;
        }

        if (debug) {
            GlStateManager.color(0.0F, 1.0F, 0.0F);
            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + gameCollision[0].height + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[0].width + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[0].width + 5 + (x * speedMultiplier), playY + gameCollision[0].height + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right

            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[0].center[0] + 5 + (x * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Center
        }
    }

    private void drawEnergizer (int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        if (ENERGIZER_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 8 + (x * 8), playY + 8 + (y * 8), GUI_X + DOT, MAZE_Y, ENERGIZER, ENERGIZER, 512, 512);
    }

    private void drawDot (int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        this.drawModalRectWithCustomSizedTexture(playX + 11 + (x * 8), playY + 11 + (y * 8), GUI_X, MAZE_Y, DOT, DOT, 512, 512);
    }

    private void drawEdibles (boolean debug) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);

        for (int i = 0; i < tempPoints.length; i++) {
            if (tempPoints[i] != null) {
                if (tempPoints[i].isEnergizer) drawEnergizer((int) tempPoints[i].pos.getX(), (int) tempPoints[i].pos.getY());
                else drawDot((int) tempPoints[i].pos.getX(), (int) tempPoints[i].pos.getY());

                if (debug) {
                    float[] debugColor = colorToFloat(new Color(0, 144, 200));
                    GlStateManager.color(debugColor[0], debugColor[1], debugColor[2]);
                    if (tempPoints[i].isEnergizer) this.drawModalRectWithCustomSizedTexture(playX + 8 + gameCollision[3].center[0] + ((int)tempPoints[i].pos.getX() * 8), playY + 8 + gameCollision[3].center[1] - 1 + ((int)tempPoints[i].pos.getY() * 8), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512);
                    else this.drawModalRectWithCustomSizedTexture(playX + 11 + gameCollision[2].center[0] + ((int)tempPoints[i].pos.getX() * 8), playY + 11 + gameCollision[2].center[1] - 1 + ((int)tempPoints[i].pos.getY() * 8), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512);
                }
            }
        }
    }

    @Deprecated
    public void drawMazeCollision (boolean debug) {
        float[] allow = colorToFloat(new Color(125, 0, 144));
        GlStateManager.color(1.0F, 0.0F, 0.0F);
        for (int i = 0; i < mazeCollision.length; i++) {
            if (mazeCollision[i].allowGhost) GlStateManager.color(allow[0], allow[1], allow[2]);
            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x, playY + mazeCollision[i].y, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x, playY + mazeCollision[i].y + mazeCollision[i].height, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x + mazeCollision[i].width, playY + mazeCollision[i].y, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x + mazeCollision[i].width, playY + mazeCollision[i].y + mazeCollision[i].height, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right
        }
    }

    private int getModeTime (byte level, int period) {
        if (level == 0) { // Level 1
            if (period == 0 || period == 2) return (7 * 20);
            else if (period == 1 || period == 3 || period == 5) return (20 * 20);
            else if (period == 4 || period == 6) return (5 * 20);
            else if (period == 7) return 0; // Indefinite
        } else if (level > 0 && level < 4) { // Level 2-4
            if (period == 0 || period == 2) return (7 * 20);
            else if (period == 1 || period == 3) return (20 * 20);
            else if (period == 5) return (1033 * 20);
            else if (period == 4) return (5 * 20);
            else if (period == 6) return 1;
            else if (period == 7) return 0; // Indefinite
        } else if (level > 3) { // Level 5+
            if (period == 0 || period == 2 || period == 4) return (5 * 20);
            else if (period == 1 || period == 3) return (20 * 20);
            else if (period == 5) return (1037 * 20);
            else if (period == 6) return 1;
            else if (period == 7) return 0; // Indefinite
        }
        return 0;
    }

    /*  Level | Normal | Dots | Fright | Energizer
        1     | 80%    | 71%  | 90%    | 79%
        2-4   | 90%    | 79%  | 95%    | 83%
        5-20  | 100%   | 87%  | 100%   | 87%
        21+   | 90%    | 79%  | -      | -
     */
    private int getPacManSpeed (int level) {
        if (level == 1) return 4;
        if (level > 1 && level < 5) return 5;
        else if (level >= 5 && level < 21) return 6;
        else if (level >= 21) return 5;
        return 4;
    }

    /*  Level | Normal | Fright | Tunnel
        1     | 75%    | 50%    | 40%
        2-4   | 85%    | 55%    | 45%
        5-20  | 95%    | 60%    | 50%
        21+   | 95%    | -      | 50%
     */
    private int getGhostSpeed (int level) {
        return 1;
    }

    /* Direction ID
        0 = Still
        1 = Up
        2 = Down
        3 = Left
        4 = Right
     */
    private boolean canMoveLeft (int character) {
        if (character == 0) {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int y = 0; y <= gameCollision[character].height; y++) {
                    if ((playX + 5 + (playerX * speedMultiplier)) == (playX + mazeCollision[c].x + mazeCollision[c].width + 1)) {
                        if ((playY + y + 4 + (playerY * speedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (playerY * speedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
                    }
                }
            }
        } else {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int y = 0; y <= gameCollision[1].height; y++) {
                    if ((playX + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) == (playX + mazeCollision[c].x + mazeCollision[c].width + 1)) {
                        if ((playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canMoveRight (int character) {
        if (character == 0) {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int y = 0; y <= gameCollision[character].height; y++) {
                    if ((playX + gameCollision[character].width + 5 + (playerX * speedMultiplier)) == (playX + mazeCollision[c].x - 1)) {
                        if ((playY + y + 4 + (playerY * speedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (playerY * speedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
                    }
                }
            }
        } else {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int y = 0; y <= gameCollision[1].height; y++) {
                    if ((playX + gameCollision[1].width + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) == (playX + mazeCollision[c].x - 1)) {
                        if ((playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canMoveUp (int character) {
        if (character == 0) {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int x = 0; x <= gameCollision[character].width; x++) {
                    if ((playY + 4 + (playerY * speedMultiplier)) == (playY + mazeCollision[c].y + mazeCollision[c].height + 1)) {
                        if ((playX + x + 5 + (playerX * speedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (playerX * speedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width)) return false;
                    }
                }
            }
        } else {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int x = 0; x <= gameCollision[1].width; x++) {
                    if (!mazeCollision[c].allowGhost) {
                        if ((playY + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) == (playY + mazeCollision[c].y + mazeCollision[c].height + 1)) {
                            if ((playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width))
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean canMoveDown (int character) {
        if (character == 0) {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int x = 0; x <= gameCollision[character].width; x++) {
                    if ((playY + gameCollision[character].height + 4 + (playerY * speedMultiplier)) == (playY + mazeCollision[c].y - 1)) {
                        if ((playX + x + 5 + (playerX * speedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (playerX * speedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width)) return false;
                    }
                }
            }
        } else {
            for (int c = 0; c < mazeCollision.length; c++) {
                for (int x = 0; x <= gameCollision[1].width; x++) {
                    if ((playY + gameCollision[1].height + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) == (playY + mazeCollision[c].y - 1)) {
                        if ((playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width)) return false;
                    }
                }
            }
        }
        return true;
    }

    /*
    Top Left
    playX + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier)

    Bottom Left
    playX + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier)

    Top Right
    playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier)

    Bottom Right
    playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier)
     */

    //this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].center[0] + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].center[1] + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Center
    /** Checks Center collider of Character Collider to see if it collided with PacMan or Ghost or dot/energizer */
    private boolean checkCenter () {
        // Edible Check
        for (int i = 0; i < tempPoints.length; i++) {
            if (tempPoints[i] != null) {
                if ((playX + gameCollision[0].center[1] + 5 + (playerX * speedMultiplier)) == (playX + 11 + gameCollision[2].center[0] + ((int) tempPoints[i].pos.getX() * 8)) && (playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier)) == (playY + 11 + gameCollision[2].center[1] - 1 + ((int) tempPoints[i].pos.getY() * 8))) {
                    if (WAKA == 0) {
                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_1, 1.0F));
                        WAKA = 1;
                    } else if (WAKA == 1) {
                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_2, 1.0F));
                        WAKA = 2;
                    } else if (WAKA == 2) {
                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_3, 1.0F));
                        WAKA = 3;
                    } else if (WAKA == 3) {
                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_4, 1.0F));
                        WAKA = 4;
                    } else if (WAKA == 4) {
                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_5, 1.0F));
                        WAKA = 5;
                    } else if (WAKA == 5) {
                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_6, 1.0F));
                        WAKA = 0;
                    }
                    if (tempPoints[i].isEnergizer) {
                        // Check for energizer sound
                        score += 50;
                        FRIGHT_STATE = 0;
                        frightTick = 0;

                        for (int g = 0; g < 4; g++) {
                            if (!inHouse[g]) frightened[g] = true;
                        }
                    } else score += 10;
                    dotCounter[4]++;
                    dotCounter[curCounter]++;
                    timeSinceEaten = tickCounter;
                    tempPoints[i] = null;
                }
            }
        }

        // TODO: Death Check
        // TODO: Death Animation

        return false;
    }

    /* Ghost AI Basics
        - Scatter, Chase, Repeat
        - Frightened Mode
                - Choose random direction with random number gen
                - If direction leads to wall, try going up, left, down, right
     */

    /* Direction ID
        0 = Still
        1 = Up
        2 = Down
        3 = Left
        4 = Right
     */

    // TODO: Eaten Movement
    private void blinkyAi () {
        if ((tickCounter - modeTick[0]) >= getModeTime(level, period[0])) {
            modeTick[0] = tickCounter;

            if (period[0] == 0 || period[0] == 2 || period[0] == 4 || period[0] == 6) mode[0] = 0; // Scatter
            else mode[0] = 1; // Chase

            if (period[0] != 7) period[0]++;
        }

        // Movement
        if ((tickCounter - ghostMovementTick[0]) >= 2) {
            ghostMovementTick[0] = tickCounter;

            if (!frightened[0]) {
                // Scatter
                if (mode[0] == 0) direction[1] = pathfinding(1, playX + 12 + (50 * 4), playY + 11);
                // Chase
                else if (mode[0] == 1) direction[1] = pathfinding(1, playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));

                //direction[1] = pathfinding(1, playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));

                // Move
                if (canMoveUp(1) && direction[1] == 1) ghostPos[0][1]--;
                if (canMoveDown(1) && direction[1] == 2) ghostPos[0][1]++;
                if (canMoveLeft(1) && direction[1] == 3) ghostPos[0][0]--;
                if (canMoveRight(1) && direction[1] == 4) ghostPos[0][0]++;
            } else { // Fright AI
                // TODO: Better AI
                // Set Position
                if (canMoveUp(1) && direction[1] == 1) ghostPos[0][1]--;
                if (canMoveDown(1) && direction[1] == 2) ghostPos[0][1]++;
                if (canMoveLeft(1) && direction[1] == 3) ghostPos[0][0]--;
                if (canMoveRight(1) && direction[1] == 4) ghostPos[0][0]++;

                // Add probability to movement
                if (canMoveUp(1) && direction[1] != 2) {
                    if (getWorld().rand.nextInt(2) == 1) direction[1] = 1;
                }
                if (canMoveDown(1) && direction[1] != 1) {
                    if (getWorld().rand.nextInt(2) == 1) direction[1] = 2;
                }
                if (canMoveLeft(1) && direction[1] != 4) {
                    if (getWorld().rand.nextInt(2) == 1) direction[1] = 3;
                }
                if (canMoveRight(1) && direction[1] != 3) {
                    if (getWorld().rand.nextInt(2) == 1) direction[1] = 4;
                }
            }
        }

        drawGhost(ghostPos[0][0], ghostPos[0][1], direction[1], 0, frightened[0], eaten[0], false);
    }

    private void inkyAi () {
        if ((tickCounter - ghostMovementTick[1]) >= 2) {
            ghostMovementTick[1] = tickCounter;

            if (dotCounter[1] >= getDotLimit(level, 1)) {
                if (!frightened[1]) {
                    if (mode[1] == 0) direction[2] = pathfinding(2, playX + gameCollision[0].center[0] + 5 + (30 * 4), playY + gameCollision[0].center[1] + 4 + (56 * 4)); // Scatter
                    else { // Chase
                        //direction[2] = pathfinding(2, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY - 4) * speedMultiplier));
                    }

                    // Move
                    if (canMoveUp(2) && direction[2] == 1) ghostPos[1][1]--;
                    if (canMoveDown(2) && direction[2] == 2) ghostPos[1][1]++;
                    if (canMoveLeft(2) && direction[2] == 3) ghostPos[1][0]--;
                    if (canMoveRight(2) && direction[2] == 4) ghostPos[1][0]++;
                }
            }
        }
        drawGhost(ghostPos[1][0], ghostPos[1][1], direction[2], 1, frightened[1], eaten[1], false);
    }

    private void pinkyAi () {
        if ((tickCounter - ghostMovementTick[2]) >= 2) {
            ghostMovementTick[2] = tickCounter;

            if (dotCounter[2] >= getDotLimit(level, 2)) {
                if (!frightened[2]) {
                    if (mode[2] == 0) direction[3] = pathfinding(3, playX + 12, playY + 11); // Scatter
                    else { // Chase
                        if (direction[0] == 1) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY - 4) * speedMultiplier));
                        else if (direction[0] == 2) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY + 4) * speedMultiplier));
                        else if (direction[0] == 3) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));
                        else if (direction[0] == 4) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + ((playerX + 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));
                    }

                    // Move
                    if (canMoveUp(3) && direction[3] == 1) ghostPos[2][1]--;
                    if (canMoveDown(3) && direction[3] == 2) ghostPos[2][1]++;
                    if (canMoveLeft(3) && direction[3] == 3) ghostPos[2][0]--;
                    if (canMoveRight(3) && direction[3] == 4) ghostPos[2][0]++;
                }
            }
        }
        drawGhost(ghostPos[2][0], ghostPos[2][1], direction[3], 2, frightened[2], eaten[2], false);
    }

    private void clydeAi () {
        if ((tickCounter - ghostMovementTick[3]) >= 2) {
            ghostMovementTick[3] = tickCounter;

            if (dotCounter[3] >= getDotLimit(level, 3)) {
                if (!frightened[3]) {
                    if (mode[3] == 0) direction[4] = pathfinding(4, playX + gameCollision[0].center[0] + 5 + (20 * 4), playY + gameCollision[0].center[1] + 4 + (56 * 4)); // Scatter
                    else { // Chase
                        //direction[2] = pathfinding(2, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY - 4) * speedMultiplier));
                    }

                    // Move
                    if (canMoveUp(4) && direction[4] == 1) ghostPos[3][1]--;
                    if (canMoveDown(4) && direction[4] == 2) ghostPos[3][1]++;
                    if (canMoveLeft(4) && direction[4] == 3) ghostPos[3][0]--;
                    if (canMoveRight(4) && direction[4] == 4) ghostPos[3][0]++;
                }
            }
        }
        drawGhost(ghostPos[3][0], ghostPos[3][1], direction[4], 3, frightened[3], eaten[3], false);
    }

    private void frightAI (int ghost) {

    }

    /* Direction ID
        0 = Still
        1 = Up
        2 = Down
        3 = Left
        4 = Right
     */

    // TODO: Gotta do this shit again... yay
    // When going up or down, sometimes makes a u-turn which isn't allowed
    // GL
    private int pathfinding (int ghost, int targetX, int targetY) {
        int x = playX + gameCollision[1].center[0] + 5 + (ghostPos[ghost - 1][0] * ghostSpeedMultiplier);
        int y = playY + gameCollision[1].center[1] + 4 + (ghostPos[ghost - 1][1] * ghostSpeedMultiplier);

        int shouldGo = 0;

        double distance = Math.sqrt(Math.pow((targetX - x), 2) + Math.pow((targetY - y), 2));

        double xDistance = Math.abs(targetX - x);
        double yDistance = Math.abs(targetY - y);

        if (xDistance == 0) {
            if (yDistance != 0) {
                if (canMoveUp(ghost) && direction[ghost] != 2) {
                    double yPredict = Math.abs(targetY - (y - 1));
                    if (yDistance > yPredict) return 1;
                }

                if (canMoveDown(ghost) && direction[ghost] != 1) {
                    double yPredict = Math.abs(targetY - (y + 1));
                    if (yDistance > yPredict) return 2;
                }

                if (!canMoveDown(ghost) || !canMoveUp(ghost)) {
                    if (canMoveRight(ghost) && direction[ghost] != 3) return 4;
                    if (canMoveLeft(ghost) && direction[ghost] != 4) return 3;
                }

                // Might have to remove
                if (canMoveRight(ghost) && direction[ghost] != 3) return 4;
                if (canMoveLeft(ghost) && direction[ghost] != 4) return 3;
            }
        } else { // x != 0
            if (yDistance != 0) {
                if (canMoveUp(ghost) && direction[ghost] != 2) {
                    /*
                    if (canMoveLeft(ghost) && direction[ghost] != 4) {
                        double xPredict = Math.abs(targetX - (x - 1));
                        if (xDistance > xPredict) return 3;
                    }
                    if (canMoveRight(ghost) && direction[ghost] != 3) {
                        double xPredict = Math.abs(targetX - (x + 1));
                        if (xDistance > xPredict) return 4;
                    }
                    */
                    if (!canMoveRight(ghost) || !canMoveLeft(ghost)) return 1;
                    double yPredict = Math.abs(targetY - (y - 1));
                    if (yDistance > yPredict) return 1;
                }

                if (canMoveDown(ghost) && direction[ghost] != 1) {
                    if (!canMoveRight(ghost) || !canMoveLeft(ghost)) return 2;
                    double yPredict = Math.abs(targetY - (y + 1));
                    if (yDistance > yPredict) return 2;
                }

                if (canMoveLeft(ghost) && direction[ghost] != 4) {
                    double xPredict = Math.abs(targetX - (x - 1));
                    if (xDistance > xPredict) return 3;
                }

                if (canMoveRight(ghost) && direction[ghost] != 3) {
                    double xPredict = Math.abs(targetX - (x + 1));
                    if (xDistance > xPredict) return 4;
                }

                if (!canMoveDown(ghost) || !canMoveUp(ghost)) {
                    if (canMoveLeft(ghost) && direction[ghost] != 4) return 3;
                    if (canMoveRight(ghost) && direction[ghost] != 3) return 4;
                }
            } else { // y == 0
                if (canMoveLeft(ghost) && direction[ghost] != 4) {
                    if (!canMoveRight(ghost)) {
                        double xPredict = Math.abs(targetX - (x + 1));
                        if (xDistance > xPredict) return 3;
                    }
                    double xPredict = Math.abs(targetX - (x - 1));
                    if (xDistance > xPredict) return 3;
                }

                if (canMoveRight(ghost) && direction[ghost] == 3) {
                    if (canMoveLeft(ghost)) return 3;
                    else {
                        if (canMoveUp(ghost)) return 1;
                        if (canMoveDown(ghost)) return 2;
                    }
                }

                if (canMoveRight(ghost) && direction[ghost] != 3) {
                    if (!canMoveLeft(ghost)) {
                        double xPredict = Math.abs(targetX - (x - 1));
                        if (xDistance > xPredict) return 3;
                    }
                    double xPredict = Math.abs(targetX - (x + 1));
                    if (xDistance > xPredict) return 4;
                }

                if (!canMoveLeft(ghost) || !canMoveRight(ghost)) {
                    if (canMoveUp(ghost) && direction[ghost] != 2) return 1;
                    if (canMoveDown(ghost) && direction[ghost] != 1) return 2;
                }
            }
        }

        if (canMoveUp(ghost) && direction[ghost] != 2) return 1;
        if (canMoveLeft(ghost) && direction[ghost] != 4) return 3;
        if (canMoveDown(ghost) && direction[ghost] != 1) return 2;
        if (canMoveRight(ghost) && direction[ghost] != 3) return 4;
        return shouldGo; // This should technically never be called
    }

    private float[] colorToFloat (Color color) {
        float red = Math.round((color.getRed() / 255.0F) * 100.0F) / 100.0F;
        float green = Math.round((color.getGreen() / 255.0F) * 100.0F) / 100.0F;
        float blue = Math.round((color.getBlue() / 255.0F) * 100.0F) / 100.0F;

        return new float[] { red, green, blue };
    }

    public class MazeCollision {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final boolean allowGhost;

        public MazeCollision (int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.allowGhost = false;
        }

        public MazeCollision (int x, int y, int width, int height, boolean allowGhost) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.allowGhost = allowGhost;
        }
    }

    public class GameCollision {
        public final int width;
        public final int height;
        public final int[] center;

        public GameCollision (int width, int height) {
            this.width = width;
            this.height = height;
            this.center = new int[] { width / 2, height / 2 };
        }
    }

    public class EdiblePoint {
        public final Point pos;
        public final boolean isEnergizer;

        public EdiblePoint (int x, int y, boolean isEnergizer) {
            this.pos = new Point(x, y);
            this.isEnergizer = isEnergizer;
        }

        public EdiblePoint (int x, int y) {
            this.pos = new Point(x, y);
            this.isEnergizer = false;
        }
    }
}
