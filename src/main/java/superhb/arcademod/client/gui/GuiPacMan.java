package superhb.arcademod.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.audio.LoopingSound;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.ArcadeSoundRegistry;
import superhb.arcademod.util.KeyHandler;

import java.awt.*;
import java.io.IOException;

// This might not be so fucked anymore
// http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php?print=1
public class GuiPacMan extends GuiArcade {
    // 1 dot = 10 pt (240 total)
    // 1 energizer = 50 pt (4 total)
    // Captured Ghost: 1 = 200; 2 = 400; 3 = 800; 4 = 1200;
    // All ghosts are captured with one energizer, additional 12000 pts
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
    private static final int FRUIT = 14;
    private static final int PACMAN = 15;
    private static final int DOT = 2;
    private static final int ENERGIZER = 8;

    private byte level;

    // Board Variables
    private int boardX, boardY;
    private int score;
    private Tile[][] tiles = new Tile[31][28]; // 28x31
    private int ENERGIZER_STATE = 0;

    // Character Variables
    private Player pacman;
    private Ghost[] ghosts = new Ghost[4];
    //private int moveTick = 0,
    private int energizerTick = 0, scatterTick = 0, scaredTick = 0;
    private int scaredTime = 0, scaredFlash = 0;

    public GuiPacMan (World world, TileEntityArcade tile, EntityPlayer player) {
        super(world, tile, player);
        setGuiSize(GUI_X, GUI_Y, 0.8F);
        setTexture(texture, 512, 512);

        // Setup Tiles
        setupTiles();

        // TODO: Call when press start
        getLevelData();
        inMenu = false;
        pacman = new Player();

        ghosts[0] = new Ghost(EnumGhost.BLINKY); // TODO: Elroy
        ghosts[1] = new Ghost(EnumGhost.INKY);
        ghosts[2] = new Ghost(EnumGhost.PINKY);
        ghosts[3] = new Ghost(EnumGhost.CLYDE);
    }

    @Override
    public void updateScreen () {
		super.updateScreen();
    	//tickCounter += 3;
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTick) {
        boardX = xScaled - (GUI_X / 2) + 5;
        boardY = yScaled - (GUI_Y / 2) + 14;
        super.drawScreen(mouseX, mouseY, partialTick);

        // Maze
        drawModalRectWithCustomSizedTexture(boardX, boardY, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);

        // Edibles
        if ((tickCounter - energizerTick) >= 10) {
            energizerTick = tickCounter;

            if (ENERGIZER_STATE == 0) ENERGIZER_STATE = 1;
            else ENERGIZER_STATE = 0;
        }

        for (int y = 0; y < 31; y++) {
            for (int x = 0; x < 28; x++) {
                if (tiles[y][x] != null) tiles[y][x].updatePosition(boardX, boardY).drawTile();
            }
        }

        // Ghosts
        ghosts[0].drawGhost().ai().move(partialTick).updatePosition(boardX, boardY);
        ghosts[1].drawGhost().ai().move(partialTick).updatePosition(boardX, boardY);
        ghosts[2].drawGhost().ai().move(partialTick).updatePosition(boardX, boardY);
        ghosts[3].drawGhost().ai().move(partialTick).updatePosition(boardX, boardY);

        // Pac-Man
        pacman.drawPlayer().move(partialTick).updatePosition(boardX, boardY);

        // Text
        this.fontRendererObj.drawString(String.format("%d", score), boardX + 30, boardY - 8, Color.white.getRGB());
    }

    @Override
    protected void keyTyped (char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == KeyHandler.left.getKeyCode()) pacman.desired = Direction.LEFT;
        else if (keyCode == KeyHandler.right.getKeyCode()) pacman.desired = Direction.RIGHT;
        else if (keyCode == KeyHandler.down.getKeyCode()) pacman.desired = Direction.DOWN;
        else if (keyCode == KeyHandler.up.getKeyCode()) pacman.desired = Direction.UP;
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
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[2][i] = new Tile(i, 2, EnumTile.WALL);
            else tiles[2][i] = new Tile(i, 2);
        }
        // Row 3
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[3][i] = new Tile(i, 3, EnumTile.WALL);
            else if (i == 1 || i == 26) tiles[3][i] = new Tile(i, 3, 2);
            else tiles[3][i] = new Tile(i, 3);
        }
        // Row 4
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[4][i] = new Tile(i, 4, EnumTile.WALL);
            else tiles[4][i] = new Tile(i, 4);
        }
        // Row 5
        for (int i = 0; i < 28; i++) {
            if (i == 0 || i == 27) tiles[5][i] = new Tile(i, 5, EnumTile.WALL);
            else tiles[5][i] = new Tile(i, 5);
        }
        // Row 6
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[6][i] = new Tile(i, 6, EnumTile.WALL);
            else tiles[6][i] = new Tile(i, 6);
        }
        // Row 7
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[7][i] = new Tile(i, 7, EnumTile.WALL);
            else tiles[7][i] = new Tile(i, 7);
        }
        // Row 8
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 6 && i < 9) || (i > 12 && i < 15) || (i > 18 && i < 21) || i == 27) tiles[8][i] = new Tile(i, 8, EnumTile.WALL);
            else tiles[8][i] = new Tile(i, 8);
        }
        // Row 9
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 28)) tiles[9][i] = new Tile(i, 9, EnumTile.WALL);
            else if (i == 12 || i == 15) tiles[9][i] = new Tile(i, 9, 0);
            else tiles[9][i] = new Tile(i, 9);
        }
        // Row 10
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 28)) tiles[10][i] = new Tile(i, 10, EnumTile.WALL);
            else if (i == 12 || i == 15) tiles[10][i] = new Tile(i, 10, 0);
            else tiles[10][i] = new Tile(i, 10);
        }
        // Row 11
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[11][i] = new Tile(i, 11, EnumTile.WALL);
            else if (i == 9 || i == 18) tiles[11][i] = new Tile(i, 11, 0);
            else if (i > 9 && i < 18) tiles[11][i] = new Tile(i, 11, EnumTile.GHOST_LIMIT);
            else tiles[11][i] = new Tile(i, 11);
        }
        // Row 12
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 13) || (i > 14 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[12][i] = new Tile(i, 12, EnumTile.WALL);
            else if (i == 9 || i == 18) tiles[12][i] = new Tile(i, 12, 0);
            else if (i == 13 || i == 14) tiles[12][i] = new Tile(i, 12, EnumTile.GHOST_ONLY);
            else tiles[12][i] = new Tile(i, 12);
        }
        // Row 13
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || i == 10 || i == 17 || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[13][i] = new Tile(i, 13, EnumTile.WALL);
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
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || i == 10 || i == 17 || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[15][i] = new Tile(i, 15, EnumTile.WALL);
            else if (i == 9 || i == 18) tiles[15][i] = new Tile(i, 15, 0);
            else if (i > 10 && i < 17) tiles[15][i] = new Tile(i, 15, EnumTile.GHOST_ONLY);
            else tiles[15][i] = new Tile(i, 15);
        }
        // Row 16
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[16][i] = new Tile(i, 16, EnumTile.WALL);
            else if (i == 9 || i == 18) tiles[16][i] = new Tile(i, 16, 0);
            else tiles[16][i] = new Tile(i, 16);
        }
        // Row 17
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[17][i] = new Tile(i, 17, EnumTile.WALL);
            else if (i > 8 && i < 19) tiles[17][i] = new Tile(i, 17, 0);
            else tiles[17][i] = new Tile(i, 17);
        }
        // Row 18
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[18][i] = new Tile(i, 18, EnumTile.WALL);
            else if (i == 9 || i == 18) tiles[18][i] = new Tile(i, 18, 0);
            else tiles[18][i] = new Tile(i, 18);
        }
        // Row 19
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[19][i] = new Tile(i, 19, EnumTile.WALL);
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
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[21][i] = new Tile(i, 21, EnumTile.WALL);
            else tiles[21][i] = new Tile(i, 21);
        }
        // Row 22
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[22][i] = new Tile(i, 22, EnumTile.WALL);
            else tiles[22][i] = new Tile(i, 22);
        }
        // Row 23
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 3 && i < 6) || (i > 21 && i < 24) || i == 27) tiles[23][i] = new Tile(i, 23, EnumTile.WALL);
            else if (i == 1 || i == 26) tiles[23][i] = new Tile(i, 23, 2);
            else if ((i > 9 && i < 13) || (i > 14 & i < 18)) tiles[23][i] = new Tile(i, 23, EnumTile.GHOST_LIMIT, 1);
            else if (i > 12 && i < 15) tiles[23][i] = new Tile(i, 23, EnumTile.GHOST_LIMIT);
            else if (i == 13 || i == 14) tiles[23][i] = new Tile(i, 23, 0);
            else tiles[23][i] = new Tile(i, 23);
        }
        // Row 24
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 3) || (i > 3 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 24) || i > 24) tiles[24][i] = new Tile(i, 24, EnumTile.WALL);
            else tiles[24][i] = new Tile(i, 24);
        }
        // Row 25
        for (int i = 0; i < 28; i++) {
            if ((i >= 0 && i < 3) || (i > 3 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 24) || i > 24) tiles[25][i] = new Tile(i, 25, EnumTile.WALL);
            else tiles[25][i] = new Tile(i, 25);
        }
        // Row 26
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 6 && i < 9) || (i > 12 && i < 15) || (i > 18 && i < 21) || i == 27) tiles[26][i] = new Tile(i, 26, EnumTile.WALL);
            else  tiles[26][i] = new Tile(i, 26);
        }
        // Row 27
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 26) || i == 27) tiles[27][i] = new Tile(i, 27, EnumTile.WALL);
            else  tiles[27][i] = new Tile(i, 27);
        }
        // Row 28
        for (int i = 0; i < 28; i++) {
            if (i == 0 || (i > 1 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 26) || i == 27) tiles[28][i] = new Tile(i, 28, EnumTile.WALL);
            else  tiles[28][i] = new Tile(i, 28);
        }
        // Row 29
        for (int i = 0; i < 28; i++) {
            if (i == 0 || i == 27) tiles[29][i] = new Tile(i, 29, EnumTile.WALL);
            else tiles[29][i] = new Tile(i, 29);
        }
        // Row 30
        for (int i = 0; i < 28; i++) tiles[30][i] = new Tile(i, 30, EnumTile.WALL);
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
        BLINKY(0, "Blinky", 14, 11, new Color(255, 7, 7)),
        INKY(1, "Inky", 12, 14, new Color(7, 255, 255)),
        PINKY(2, "Pinky", 14, 14, new Color(255, 184, 222)),
        CLYDE(3, "Clyde", 16, 14, new Color(255, 159, 7));

        private int id;
        private String name;
        private int x, y;
        private Color color;

        EnumGhost (int id, String name, int x, int y, Color color) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
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
//            if (type == EnumTile.WALL) {
//                glColor(type.getColor());
//                drawModalRectWithCustomSizedTexture(extendedX, extendedY, 234, 264, 8, 8, 512, 512);
//            }

            // TODO: Remove
            if (type == EnumTile.GHOST_LIMIT || type == EnumTile.TELE_ZONE) {
                glColor(Color.orange);
                drawModalRectWithCustomSizedTexture(extendedX, extendedY, 234, 264, 8, 8, 512, 512);
            }

            // Draw Food
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            if (edible == 1) { // Dot
                drawModalRectWithCustomSizedTexture(extendedX + 3, extendedY + 3, GUI_X, MAZE_Y, DOT, DOT, 512, 512);
            } else if (edible == 2) { // Energizer
                if (ENERGIZER_STATE == 0) drawModalRectWithCustomSizedTexture(extendedX, extendedY, GUI_X + DOT, MAZE_Y, ENERGIZER, ENERGIZER, 512, 512);
            }
            return this;
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
        int lives = 3, foodEaten, ghostsEaten; // When scareTime is reached, ghostsEaten must be reset.
        boolean energizerMode, canEatGhost;

        int STATE = 0;

        boolean teleport;
        boolean stopped = false;

        public Player () {
            super(14, 23);

            teleport = false;
            current = desired = Direction.LEFT;
        }

        // TODO: cornering
        private Mover move (float partialTick) {
            if (desired != current) changeDirection(desired);

            // Teleport (Might have to redo)
            if (checkTile() == EnumTile.TELE) {
                if (extendedX <= (offsetX + 2) && y == 14) moveX = 103;
                if (extendedX >= (offsetX + (27 * 8)) && y == 14) moveX = -108;
            }

            // TODO: Stop for 1/60th a second when dot is eaten
            // Eat Dots
            if (tiles[y][x].edible == 1) {
                tiles[y][x].edible = 0;
                foodEaten++;
                score += 10;
            }

            // Eat Energizer
            if (tiles[y][x].edible == 2) {
                tiles[y][x].edible = 0;
                foodEaten++;
                score += 50;
                energizerMode = true;
				canEatGhost = true; // set to false when ScareTime is done
            }

			//System.out.println(" " + (tickCounter + partialTick));

            //if ((tickCounter - moveTick) / (float)(1/3) <= 0.666f) {
            //    moveTick = tickCounter;

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
            //}
            return this;
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

			// TODO: Jittery with new movement system
            // Animation
            if (onTile()) {
                if (STATE == 0) STATE = 1;
                else STATE = 0;
            }

            return this;
        }

        // TODO: Draw Lives
        private Player drawPlayer () {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            switch (current) {
                case STAND:
                    drawModalRectWithCustomSizedTexture(extendedX - 4, extendedY - 4, 0, GUI_Y  + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
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
            return this;
        }
    }

    // TODO: Ghost Teleport logic
    private class Ghost extends Mover {
        EnumGhost info;
        boolean scared = false, eaten = false, scatter = false, inHouse;
        int SCARED_STATE = 0, BODY_STATE = 0;
        int dotCounter = 0; // See Home Sweet Home Section

        // TODO: Have to set start pos with extended coords
        public Ghost (EnumGhost ghost) {
            super(ghost.getX(), ghost.getY());
            info = ghost;

            if (ghost != EnumGhost.BLINKY) {
                inHouse = true;
                current = desired = Direction.UP;
            } else current = desired = Direction.LEFT;
        }

        private Mover move (float partialTick) {
            if (desired != current) changeDirection(desired);

            if ((extendedX - offsetX) % 4 == 0 && (extendedY - offsetY) % 4 == 0) {
                if (BODY_STATE == 0) BODY_STATE = 1;
                else BODY_STATE = 0;
            }

            // Collision Detection
            if ((x == pacman.x) && (y == pacman.y)) {
                if (scared) {
                    eaten = true;
                    scared = false;
                    // add to score
                }
                // else kill pacman
            }

            // TODO: Delay before leaving house
            if (eaten) {
                if ((x >= 11 && x <= 16) && (y >= 13 && y <= 15)) {
                    eaten = false;
                    inHouse = true;
                }
            }
            if (inHouse) { // TODO: Let Ghosts go from up -> down and down -> up.
                if ((x == 13 || x == 14) && y == 11) inHouse = false;
            }

            // Scared direction
            if (pacman.energizerMode) {
                if (!eaten) {
                    scared = true;
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

                if (info == EnumGhost.CLYDE) pacman.energizerMode = false;
            }

            //System.out.println("Tick:" + (moveTick + (tickCounter - moveTick) * partialTick) + "Move:" + moveTick);
            //System.out.println((moveTick + (tickCounter - moveTick) * partialTick) / (5 * moveTick));

            // TODO: Variable Speed
            if ((moveTick + (tickCounter - moveTick) * partialTick) >= 1) {
                moveTick = tickCounter;

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
            return this;
        }

        private float getSpeed () {
        	if (level == 0) { // Level 0
				if (!eaten) {
					if (pacman.canEatGhost) {
						if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.4f;
						else return 0.5f;
					}
					if (tiles[y][x].type == EnumTile.TELE_ZONE) return 0.4f;
					return 0.75f;
				} else return 2;
			}
			return 0.75f;
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
        private Ghost ai () {
            if (current == Direction.LEFT) { // Can't Move right
                if (isBlockedLeft(x - 1, y)) { // is blocked left
                    if (!isBlockedDown(x - 1, y) && !isBlocked(x - 1, y)) {
                        if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY + 8)) desired = Direction.UP;
                        else desired = Direction.DOWN;
                    }
                    if (!isBlocked(x - 1, y) && isBlockedDown(x - 1, y)) desired = Direction.UP;
                    if (!isBlockedDown(x - 1, y) && isBlocked(x - 1, y)) desired = Direction.DOWN;
                } else { // is not blocked left
                    if (!isBlockedDown(x - 1, y) && !isBlocked(x - 1, y)) {
                        if (calculateDistance(extendedX - 16, extendedY) < calculateDistance(extendedX - 8, extendedY - 8) && calculateDistance(extendedX - 16, extendedY) < calculateDistance(extendedX - 8, extendedY + 8)) desired = Direction.LEFT;
                        else if ((calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 16, extendedY)) && tiles[y][x].type != EnumTile.GHOST_LIMIT) desired = Direction.UP;
                        else if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 16, extendedY) && calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 8, extendedY - 8)) desired = Direction.DOWN;
                        else if (calculateDistance(extendedX - 8, extendedY + 8) == calculateDistance(extendedX - 16, extendedY) && calculateDistance(extendedX - 16, extendedY) == calculateDistance(extendedX - 8, extendedY - 8)) desired = Direction.DOWN;
                    }
                    if (!isBlocked(x - 1, y)) {
                        if ((calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 16, extendedY)) && tiles[y][x].type != EnumTile.GHOST_LIMIT) desired = Direction.UP;
                    }
                    if (!isBlockedDown(x - 1, y)) {
                        if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 16, extendedY)) desired = Direction.DOWN;
                    }
                    //if (isBlocked(x - 1, y) && isBlockedDown(x - 1, y)) desired = Direction.LEFT;
                }
            }
            if (current == Direction.RIGHT) { // Can't Move left
                if (isBlockedRight(x + 1, y)) {
                    if (!isBlockedDown(x + 1, y) && !isBlocked(x + 1, y)) {
                        if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY + 8)) desired = Direction.UP;
                        else desired = Direction.DOWN;
                    }
                    if (!isBlocked(x + 1, y) && isBlockedDown(x + 1, y)) desired = Direction.UP;
                    if (!isBlockedDown(x + 1, y) && isBlocked(x + 1, y)) desired = Direction.DOWN;
                } else {
                    if (!isBlockedDown(x + 1, y) && !isBlocked(x + 1, y)) {
                        if ((calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 16, extendedY)) && tiles[y][x].type != EnumTile.GHOST_LIMIT) desired = Direction.UP;
                        else if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY - 8) && calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 16, extendedY)) desired = Direction.DOWN;
                        else if (calculateDistance(extendedX + 16, extendedY) < calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX + 16, extendedY) < calculateDistance(extendedX + 8, extendedY - 8)) desired = Direction.RIGHT;
                        else if ((calculateDistance(extendedX + 16, extendedY) == calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX + 8, extendedY + 8) == calculateDistance(extendedX + 8, extendedY - 8)) && tiles[y][x].type != EnumTile.GHOST_LIMIT) desired = Direction.UP;
                    }
                    if (!isBlocked(x + 1, y)) {
                        if ((calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 16, extendedY)) && tiles[y][x].type != EnumTile.GHOST_LIMIT) desired = Direction.UP;
                    }
                    if (!isBlockedDown(x + 1, y)) {
                        if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 16, extendedY)) desired = Direction.DOWN;
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
                            if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY - 8)) desired = Direction.LEFT;
                            else desired = Direction.RIGHT;
                        }
                    }
                    if (!isBlockedLeft(x, y - 1) && isBlockedRight(x, y - 1)) desired = Direction.LEFT;
                    if (!isBlockedRight(x, y - 1) && isBlockedLeft(x, y - 1)) desired = Direction.RIGHT;
                } else {
                    if (!isBlockedRight(x, y - 1) && !isBlockedLeft(x, y - 1)) {
                        if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY - 8) && calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16)) desired = Direction.LEFT;
                        else if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY - 8) && calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16)) desired = Direction.RIGHT;
                        else if (calculateDistance(extendedX, extendedY - 16) < calculateDistance(extendedX - 8, extendedY - 8) && calculateDistance(extendedX, extendedY - 16) < calculateDistance(extendedX + 8, extendedY - 8)) desired = Direction.UP;
                        else if (calculateDistance(extendedX - 8, extendedY - 8) == calculateDistance(extendedX + 8, extendedY - 8) && calculateDistance(extendedX + 8, extendedY - 8) == calculateDistance(extendedX, extendedY - 16)) desired = Direction.LEFT;
                    }
                    if (!isBlockedLeft(x, y - 1)) {
                        if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16)) desired = Direction.LEFT;
                    }
                    if (!isBlockedRight(x, y - 1)) {
                        if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16)) desired = Direction.RIGHT;
                    }
                    //if (isBlockedLeft(x, y - 1) && isBlockedRight(x, y - 1)) desired = Direction.UP;
                }
            }
            if (current == Direction.DOWN) { // Can't move up
                if (isBlockedDown(x, y + 1)) { // is blocked down
                    if (!isBlockedLeft(x, y + 1) && !isBlockedRight(x, y + 1)) {
                        if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY + 8)) desired = Direction.LEFT;
                        else desired = Direction.RIGHT;
                    }
                    if (!isBlockedRight(x, y + 1) && isBlockedLeft(x, y + 1)) desired = Direction.RIGHT;
                    if (!isBlockedLeft(x, y + 1) && isBlockedRight(x, y + 1)) desired = Direction.LEFT;
                } else {
                    if (!isBlockedLeft(x, y + 1) && !isBlockedRight(x, y + 1)) {
                        if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY + 8) && calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16)) desired = Direction.LEFT;
                        else if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16)) desired = Direction.RIGHT;
                        else if (calculateDistance(extendedX, extendedY + 16) < calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX, extendedY + 16) < calculateDistance(extendedX + 8, extendedY + 8)) desired = Direction.DOWN;
                        else if (calculateDistance(extendedX, extendedY + 16) == calculateDistance(extendedX - 8, extendedY + 8) && calculateDistance(extendedX - 8, extendedY + 8) == calculateDistance(extendedX + 8, extendedY + 8)) desired = Direction.RIGHT;
                    }
                    if (!isBlockedLeft(x, y + 1)) {
                        if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16)) desired = Direction.LEFT;
                    }
                    if (!isBlockedRight(x, y + 1)) {
                        if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16)) desired = Direction.RIGHT;
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
            return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL || tiles[y][Math.max(0, x - 1)].type == EnumTile.TELE_ZONE;
        }

        private boolean isBlockedRight (int x, int y) {
            return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL || tiles[y][Math.min(27, x + 1)].type == EnumTile.TELE_ZONE;
        }

        @Override
        public boolean isBlockedDown () {
            return isBlockedDown(x, y);
        }

        private Point getTarget () {
            if (scared) {
                return new Point((int)(Math.random() * 28) * 8 + offsetX, (int)(Math.random() * 30) * 8 + offsetY); // Not the best option
            } else if (eaten) {
                return new Point((13 * 8) + offsetX, (15 * 8) + offsetY);
            } else if (inHouse) {
                return new Point((13 * 8) + offsetX, (11 * 8) + offsetY);
            } else {
                switch (info) {
                    case BLINKY:
                        return scatter ? new Point((27 * 8), 0) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
                    case INKY:
                        switch (pacman.current) { // Scatter Bottom Right
                            case STAND:
                                return scatter ? new Point((27 * 8), (30 * 8)) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
                            case LEFT:
                                return scatter ? new Point((27 * 8), (30 * 8)) : new Point(((pacman.getPosition().x - 3) * 8) + offsetX, ((pacman.getPosition().y + 1) * 8) + offsetY);
                            case RIGHT:
                                return scatter ? new Point((27 * 8), (30 * 8)) : new Point(((pacman.getPosition().x + 3) * 8) + offsetX, ((pacman.getPosition().y - 1) * 8) + offsetY);
                            case UP:
                                return scatter ? new Point((27 * 8), (30 * 8)) : new Point(((pacman.getPosition().x - 1) * 8) + offsetX, ((pacman.getPosition().y - 7) * 8) + offsetY);
                            case DOWN:
                                return scatter ? new Point((27 * 8), (30 * 8)) : new Point(((pacman.getPosition().x - 4) * 8) + offsetX, ((pacman.getPosition().y + 2) * 8) + offsetY);
                        }
                    case PINKY:
                        switch (pacman.current) { // Scatter Top Left
                            case STAND:
                                return scatter ? new Point(0, 0) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
                            case LEFT:
                                return scatter ? new Point(0, 0) : new Point((pacman.getPosition().x * 8) + offsetX - (4 * 8), (pacman.getPosition().y * 8) + offsetY);
                            case RIGHT:
                                return scatter ? new Point(0, 0) : new Point((pacman.getPosition().x * 8) + offsetX + (4 * 8), (pacman.getPosition().y * 8) + offsetY);
                            case UP:
                                return scatter ? new Point(0, 0) : new Point((pacman.getPosition().x * 8) + offsetX - (4 * 8), (pacman.getPosition().y * 8) + offsetY - (4 * 8));
                            case DOWN:
                                return scatter ? new Point(0, 0) : new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY + (4 * 8));
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
                                if ((extendedX <= (pacman.extendedX + (8 * 8)) && extendedX >= (pacman.extendedX - (8 * 8))) && (extendedY <= (pacman.extendedY - (8 * i)))) return new Point(offsetX, (30 * 8) + offsetY);
                            }
                            for (int i = 1; i < 4; i++) {
                                if ((extendedX <= (pacman.extendedX + (8 * 8)) && extendedX >= (pacman.extendedX - (8 * 8))) && (extendedY <= (pacman.extendedY + (8 * i)))) return new Point(offsetX, (30 * 8) + offsetY);
                            }
                            for (int i = 4; i < 6; i++) {
                                if ((extendedX <= (pacman.extendedX + (8 * 7)) && extendedX >= (pacman.extendedX - (8 * 7))) && (extendedY <= (pacman.extendedY - (8 * i)))) return new Point(offsetX, (30 * 8) + offsetY);
                            }
                            for (int i = 4; i < 6; i++) {
                                if ((extendedX <= (pacman.extendedX + (8 * 7)) && extendedX >= (pacman.extendedX - (8 * 7))) && (extendedY <= (pacman.extendedY + (8 * i)))) return new Point(offsetX, (30 * 8) + offsetY);
                            }
                            if ((extendedX <= (pacman.extendedX + (8 * 6)) && extendedX >= (pacman.extendedX - (8 * 6))) && (extendedY <= (pacman.extendedY - (8 * 6)))) return new Point(offsetX, (30 * 8) + offsetY);
                            if ((extendedX <= (pacman.extendedX + (8 * 6)) && extendedX >= (pacman.extendedX - (8 * 6))) && (extendedY <= (pacman.extendedY + (8 * 6)))) return new Point(offsetX, (30 * 8) + offsetY);
                            if ((extendedX <= (pacman.extendedX + (8 * 5)) && extendedX >= (pacman.extendedX - (8 * 5))) && (extendedY <= (pacman.extendedY - (8 * 7)))) return new Point(offsetX, (30 * 8) + offsetY);
                            if ((extendedX <= (pacman.extendedX + (8 * 5)) && extendedX >= (pacman.extendedX - (8 * 5))) && (extendedY <= (pacman.extendedY + (8 * 7)))) return new Point(offsetX, (30 * 8) + offsetY);
                            if ((extendedX <= (pacman.extendedX + (8 * 3)) && extendedX >= (pacman.extendedX - (8 * 3))) && (extendedY <= (pacman.extendedY - (8 * 8)))) return new Point(offsetX, (30 * 8) + offsetY);
                            if ((extendedX <= (pacman.extendedX + (8 * 3)) && extendedX >= (pacman.extendedX - (8 * 3))) && (extendedY <= (pacman.extendedY + (8 * 8)))) return new Point(offsetX, (30 * 8) + offsetY);

                            return new Point((pacman.getPosition().x * 8) + offsetX, (pacman.getPosition().y * 8) + offsetY);
                        }
                }
            }
            return null;
        }

        private Ghost drawGhost () {
            // Target TODO: Remove
            glColor(Color.green);
            drawModalRectWithCustomSizedTexture(getTarget().x, getTarget().y, 234, 264, 8, 8, 512, 512);

//                    new Color(33, 33, 222), // Blue
//                    new Color(245, 245, 255), // White
//                    new Color(255, 15, 15) // Red

            // TODO: Blink when timer starts to run out
            // TODO: Fix it so that when eaten and another energizer is eaten, face changes to scared with no body (ghost stays eaten and isn't affected by scare)
            if (scared) {
                if ((tickCounter - scaredTick) == (scaredTime - 1) * 20) {
                    // 1 second to blink
                }
                glColor(new Color(33, 33, 222));
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
                // TODO: Blink when timer starts to run out
                glColor(new Color(245, 245, 255));
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

        int moveTick = 0;

        // Position
        int x, y;
        int x1, y1;
        float moveX, moveY;
        int extendedX, extendedY;
        int offsetX, offsetY;

        private Mover (int x, int y) {
            this.x1 = (x * 8) - 4;
            this.y1 = y * 8;
            this.x = x;
            this.y = y;
        }

//        private Mover (int x, int y, int ugh) {
//            this.x1 = x;
//            this.y1 = y;
//        }

        public Mover updatePosition (int x, int y) {
            extendedX = (int)(x1 + x + moveX);
            extendedY = (int)(y1 + y + moveY);

            offsetX = x;
            offsetY = y;

            if ((extendedX - x) % 8 == 0) this.x = (extendedX - x) / 8;
            if ((extendedY - y) % 8 == 0) this.y = (extendedY - y) / 8;

            return this;
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
    }

    private void getLevelData () {
        // Set bonus fruit
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

//    // Animation Variables
//    private int PACMAN_STATE = 0;
//    private int FRIGHT_STATE = 0;
//    private int BLINK_SPEED = 2;
//    private int GHOST_STATE = 0;
//    private int ENERGIZER_STATE = 0;
//    private boolean resetState = true;
//
//    // Audio Variables
//    private int WAKA = 0;
//    private int ENERGIZER_SOUND = 0;
//    private float volume = 1.0F;
//    private boolean playSiren = true;
//    private boolean playDeath = true;
//
//    // Game Variables
//    private boolean died = false;
//    private int life = 2;
//    private int level;
//    private int score = 0;
//    private int fruit = 0;
//    private final int speedMultiplier = 4;
//    private final int ghostSpeedMultiplier = 4;
//    private boolean blink = false;
//    private boolean start = true;
//    private boolean levelComplete = false;
//    private boolean spawnFruit = false;
//
//    /*
//        0 = Pac Man
//        1 = Blinky
//        2 = Inky
//        3 = Pinky
//        4 = Clyde
//     */
//    private Direction[] direction = { Direction.LEFT, Direction.STAND, Direction.UP, Direction.UP, Direction.UP };
//    private Direction nextDirection = Direction.STAND;
//
//    // Ghost Variables
//    private int[] mode = { 0, 0, 0, 0 };
//    private int[] period = { 0, 0, 0, 0 };
//    private boolean[] frightened = { false, false, false, false };
//    private boolean[] eaten = { false, false, false, false };
//    private boolean[] inHouse = { false, true, true, true };
//    private int[] dotCounter = { 0, 0, 0, 0, 0, 0 }; // dotCounter[4] = Global Dot Counter; dotCounter[5] = Level Dot Counter
//    private int curCounter = 0;
//
//    private int playX, playY;
//    private int playerX = 25, playerY = 44;
//    private int[][] ghostPos = {
//            { 25, 20 }, // Blinky
//            { 21, 26 }, // Inky
//            { 25, 26 }, // Pinky
//            { 29, 26 } // Clyde
//    };
//
//    private int blinkTick = 0;
//    private int frightTick = 0;
//    private int animationTick = 0, animationSpeed = 2;
//    private int energizerTick = 0;
//    private int ghostTick = 0, ghostSpeed = 1;
//    private int ghostMovementTick[] = { 0, 0, 0, 0 };
//    private int movementTick = 0, movementSpeed = 1;
//    private int modeTick[] = { 0, 0, 0, 0 };
//    private int timeSinceEaten = 0;
//    private int startTick = 0;
//    private int fruitTick = 0, fruitTime = 0;
//
//    private boolean pauseModeTimer = false; // I forgot what this was for
//
//    private final Color[] ghostColor = {
//            new Color(255, 7, 7), // Blinky
//            new Color(7, 255, 255), // Inky
//            new Color(255, 184, 222), // Pinky
//            new Color(255, 159, 7), // Clyde
//            new Color(255, 255, 255),
//            new Color(7, 7, 255)
//    };
//
//    private final Color[] eyeColor = {
//            new Color(33, 33, 222),
//            new Color(245, 245, 255),
//            new Color(255, 15, 15)
//    };
//
//    private final EdiblePoint[] ediblePoint = new EdiblePoint[] {
//            // Dots
//                // Row 1
//            new EdiblePoint(0, 0),
//            new EdiblePoint(1, 0),
//            new EdiblePoint(2, 0),
//            new EdiblePoint(3, 0),
//            new EdiblePoint(4, 0),
//            new EdiblePoint(5, 0),
//            new EdiblePoint(6, 0),
//            new EdiblePoint(7, 0),
//            new EdiblePoint(8, 0),
//            new EdiblePoint(9, 0),
//            new EdiblePoint(10, 0),
//            new EdiblePoint(11, 0),
//
//            new EdiblePoint(14, 0),
//            new EdiblePoint(15, 0),
//            new EdiblePoint(16, 0),
//            new EdiblePoint(17, 0),
//            new EdiblePoint(18, 0),
//            new EdiblePoint(19, 0),
//            new EdiblePoint(20, 0),
//            new EdiblePoint(21, 0),
//            new EdiblePoint(22, 0),
//            new EdiblePoint(23, 0),
//            new EdiblePoint(24, 0),
//            new EdiblePoint(25, 0),
//                // Row 2
//            new EdiblePoint(0, 1),
//            new EdiblePoint(5, 1),
//            new EdiblePoint(11, 1),
//            new EdiblePoint(14, 1),
//            new EdiblePoint(20, 1),
//            new EdiblePoint(25, 1),
//                // Row 3
//            new EdiblePoint(5, 2),
//            new EdiblePoint(11, 2),
//            new EdiblePoint(14, 2),
//            new EdiblePoint(20, 2),
//                // Row 4
//            new EdiblePoint(0, 3),
//            new EdiblePoint(5, 3),
//            new EdiblePoint(11, 3),
//            new EdiblePoint(14, 3),
//            new EdiblePoint(20, 3),
//            new EdiblePoint(25, 3),
//                // Row 5
//            new EdiblePoint(0, 4),
//            new EdiblePoint(1, 4),
//            new EdiblePoint(2, 4),
//            new EdiblePoint(3, 4),
//            new EdiblePoint(4, 4),
//            new EdiblePoint(5, 4),
//            new EdiblePoint(6, 4),
//            new EdiblePoint(7, 4),
//            new EdiblePoint(8, 4),
//            new EdiblePoint(9, 4),
//            new EdiblePoint(10, 4),
//            new EdiblePoint(11, 4),
//            new EdiblePoint(12, 4),
//            new EdiblePoint(13, 4),
//            new EdiblePoint(14, 4),
//            new EdiblePoint(15, 4),
//            new EdiblePoint(16, 4),
//            new EdiblePoint(17, 4),
//            new EdiblePoint(18, 4),
//            new EdiblePoint(19, 4),
//            new EdiblePoint(20, 4),
//            new EdiblePoint(21, 4),
//            new EdiblePoint(22, 4),
//            new EdiblePoint(23, 4),
//            new EdiblePoint(24, 4),
//            new EdiblePoint(25, 4),
//                // Row 6
//            new EdiblePoint(0, 5),
//            new EdiblePoint(5, 5),
//
//            new EdiblePoint(8, 5),
//            new EdiblePoint(17, 5),
//
//            new EdiblePoint(20, 5),
//            new EdiblePoint(25, 5),
//                // Row 7
//            new EdiblePoint(0, 6),
//            new EdiblePoint(5, 6),
//
//            new EdiblePoint(8, 6),
//            new EdiblePoint(17, 6),
//
//            new EdiblePoint(20, 6),
//            new EdiblePoint(25, 6),
//                // Row 8
//            new EdiblePoint(0, 7),
//            new EdiblePoint(1, 7),
//            new EdiblePoint(2, 7),
//            new EdiblePoint(3, 7),
//            new EdiblePoint(4, 7),
//            new EdiblePoint(5, 7),
//
//            new EdiblePoint(8, 7),
//            new EdiblePoint(9, 7),
//            new EdiblePoint(10, 7),
//            new EdiblePoint(11, 7),
//
//            new EdiblePoint(14, 7),
//            new EdiblePoint(15, 7),
//            new EdiblePoint(16, 7),
//            new EdiblePoint(17, 7),
//
//            new EdiblePoint(20, 7),
//            new EdiblePoint(21, 7),
//            new EdiblePoint(22, 7),
//            new EdiblePoint(23, 7),
//            new EdiblePoint(24, 7),
//            new EdiblePoint(25, 7),
//                // Row 9
//            new EdiblePoint(5, 8),
//            new EdiblePoint(20, 8),
//                // Row 10
//            new EdiblePoint(5, 9),
//            new EdiblePoint(20, 9),
//                // Row 11
//            new EdiblePoint(5, 10),
//            new EdiblePoint(20, 10),
//                // Row 12
//            new EdiblePoint(5, 11),
//            new EdiblePoint(20, 11),
//                // Row 13
//            new EdiblePoint(5, 12),
//            new EdiblePoint(20, 12),
//                // Row 14
//            new EdiblePoint(5, 13),
//            new EdiblePoint(20, 13),
//                // Row 15
//            new EdiblePoint(5, 14),
//            new EdiblePoint(20, 14),
//                // Row 16
//            new EdiblePoint(5, 15),
//            new EdiblePoint(20, 15),
//                // Row 17
//            new EdiblePoint(5, 16),
//            new EdiblePoint(20, 16),
//                // Row 18
//            new EdiblePoint(5, 17),
//            new EdiblePoint(20, 17),
//                // Row 19
//            new EdiblePoint(5, 18),
//            new EdiblePoint(20, 18),
//                // Row 20
//            new EdiblePoint(0, 19),
//            new EdiblePoint(1, 19),
//            new EdiblePoint(2, 19),
//            new EdiblePoint(3, 19),
//            new EdiblePoint(4, 19),
//            new EdiblePoint(5, 19),
//            new EdiblePoint(6, 19),
//            new EdiblePoint(7, 19),
//            new EdiblePoint(8, 19),
//            new EdiblePoint(9, 19),
//            new EdiblePoint(10, 19),
//            new EdiblePoint(11, 19),
//
//            new EdiblePoint(14, 19),
//            new EdiblePoint(15, 19),
//            new EdiblePoint(16, 19),
//            new EdiblePoint(17, 19),
//            new EdiblePoint(18, 19),
//            new EdiblePoint(19, 19),
//            new EdiblePoint(20, 19),
//            new EdiblePoint(21, 19),
//            new EdiblePoint(22, 19),
//            new EdiblePoint(23, 19),
//            new EdiblePoint(24, 19),
//            new EdiblePoint(25, 19),
//                // Row 21
//            new EdiblePoint(0, 20),
//            new EdiblePoint(5, 20),
//            new EdiblePoint(11, 20),
//
//            new EdiblePoint(14, 20),
//            new EdiblePoint(20, 20),
//            new EdiblePoint(25, 20),
//                // Row 22
//            new EdiblePoint(0, 21),
//            new EdiblePoint(5, 21),
//            new EdiblePoint(11, 21),
//
//            new EdiblePoint(14, 21),
//            new EdiblePoint(20, 21),
//            new EdiblePoint(25, 21),
//                // Row 23
//            new EdiblePoint(1, 22),
//            new EdiblePoint(2, 22),
//
//            new EdiblePoint(5, 22),
//            new EdiblePoint(6, 22),
//            new EdiblePoint(7, 22),
//            new EdiblePoint(8, 22),
//            new EdiblePoint(9, 22),
//            new EdiblePoint(10, 22),
//            new EdiblePoint(11, 22),
//
//            new EdiblePoint(14, 22),
//            new EdiblePoint(15, 22),
//            new EdiblePoint(16, 22),
//            new EdiblePoint(17, 22),
//            new EdiblePoint(18, 22),
//            new EdiblePoint(19, 22),
//            new EdiblePoint(20, 22),
//
//            new EdiblePoint(23, 22),
//            new EdiblePoint(24, 22),
//                // Row 24
//            new EdiblePoint(2, 23),
//            new EdiblePoint(5, 23),
//            new EdiblePoint(8, 23),
//
//            new EdiblePoint(17, 23),
//            new EdiblePoint(20, 23),
//            new EdiblePoint(23, 23),
//                // Row 25
//            new EdiblePoint(2, 24),
//            new EdiblePoint(5, 24),
//            new EdiblePoint(8, 24),
//
//            new EdiblePoint(17, 24),
//            new EdiblePoint(20, 24),
//            new EdiblePoint(23, 24),
//                // Row 26
//            new EdiblePoint(0, 25),
//            new EdiblePoint(1, 25),
//            new EdiblePoint(2, 25),
//            new EdiblePoint(3, 25),
//            new EdiblePoint(4, 25),
//            new EdiblePoint(5, 25),
//
//            new EdiblePoint(8, 25),
//            new EdiblePoint(9, 25),
//            new EdiblePoint(10, 25),
//            new EdiblePoint(11, 25),
//
//            new EdiblePoint(14, 25),
//            new EdiblePoint(15, 25),
//            new EdiblePoint(16, 25),
//            new EdiblePoint(17, 25),
//
//            new EdiblePoint(20, 25),
//            new EdiblePoint(21, 25),
//            new EdiblePoint(22, 25),
//            new EdiblePoint(23, 25),
//            new EdiblePoint(24, 25),
//            new EdiblePoint(25, 25),
//                // Row 27
//            new EdiblePoint(0, 26),
//            new EdiblePoint(11, 26),
//
//            new EdiblePoint(14, 26),
//            new EdiblePoint(25, 26),
//                // Row 28
//            new EdiblePoint(0, 27),
//            new EdiblePoint(11, 27),
//
//            new EdiblePoint(14, 27),
//            new EdiblePoint(25, 27),
//                // Row 29
//            new EdiblePoint(0, 28),
//            new EdiblePoint(1, 28),
//            new EdiblePoint(2, 28),
//            new EdiblePoint(3, 28),
//            new EdiblePoint(4, 28),
//            new EdiblePoint(5, 28),
//            new EdiblePoint(6, 28),
//            new EdiblePoint(7, 28),
//            new EdiblePoint(8, 28),
//            new EdiblePoint(9, 28),
//            new EdiblePoint(10, 28),
//            new EdiblePoint(11, 28),
//            new EdiblePoint(12, 28),
//            new EdiblePoint(13, 28),
//            new EdiblePoint(14, 28),
//            new EdiblePoint(15, 28),
//            new EdiblePoint(16, 28),
//            new EdiblePoint(17, 28),
//            new EdiblePoint(18, 28),
//            new EdiblePoint(19, 28),
//            new EdiblePoint(20, 28),
//            new EdiblePoint(21, 28),
//            new EdiblePoint(22, 28),
//            new EdiblePoint(23, 28),
//            new EdiblePoint(24, 28),
//            new EdiblePoint(25, 28),
//
//            // Energizer
//            new EdiblePoint(0, 2, true),
//            new EdiblePoint(25, 2, true),
//            new EdiblePoint(0, 22, true),
//            new EdiblePoint(25, 22, true)
//    };
//    private EdiblePoint[] tempPoints;
//
//    private final MazeCollision[] mazeCollision = new MazeCollision[] {
//            new MazeCollision(0, 0, 223, 3),
//            new MazeCollision(108, 3, 8, 32),
//            new MazeCollision(220, 3, 3, 76),
//            new MazeCollision(20, 19, 24, 16),
//            new MazeCollision(60, 19, 32, 16),
//            new MazeCollision(132, 19, 32, 16),
//            new MazeCollision(180, 19, 24, 16),
//            new MazeCollision(20, 51, 24, 8),
//            new MazeCollision(60, 51, 8, 56),
//            new MazeCollision(68, 75, 24, 8),
//            new MazeCollision(84, 51, 56, 8),
//            new MazeCollision(108, 59, 8, 24),
//            new MazeCollision(156, 51, 8, 56),
//            new MazeCollision(132, 75, 24, 8),
//            new MazeCollision(180, 51, 24, 8),
//            new MazeCollision(0, 3, 4, 76),
//            new MazeCollision(4, 75, 40, 4),
//            new MazeCollision(40, 79, 4, 28),
//            new MazeCollision(0, 104, 40, 3),
//            new MazeCollision(0, 123, 44, 4),
//            new MazeCollision(40, 127, 4, 28),
//            new MazeCollision(0, 152, 40, 3),
//            new MazeCollision(180, 75, 40, 4),
//            new MazeCollision(180, 79, 3, 28),
//            new MazeCollision(183, 104, 40, 3),
//            new MazeCollision(180, 123, 43, 4),
//            new MazeCollision(180, 127, 3, 28),
//            new MazeCollision(183, 152, 40, 3),
//            new MazeCollision(220, 155, 3, 92),
//            new MazeCollision(204, 195, 16, 8),
//            new MazeCollision(0, 155, 4, 92),
//            new MazeCollision(4, 195, 16, 8),
//            new MazeCollision(60, 123, 8, 32),
//            new MazeCollision(156, 123, 8, 32),
//            new MazeCollision(84, 147, 56, 8),
//            new MazeCollision(108, 155, 8, 24),
//            new MazeCollision(20, 171, 24, 8),
//            new MazeCollision(36, 179, 8, 24),
//            new MazeCollision(60, 171, 32, 8),
//            new MazeCollision(132, 171, 32, 8),
//            new MazeCollision(180, 171, 24, 8),
//            new MazeCollision(180, 179, 8, 24),
//            new MazeCollision(60, 195, 8, 24),
//            new MazeCollision(20, 219, 72, 8),
//            new MazeCollision(84, 195, 56, 8),
//            new MazeCollision(108, 203, 8, 24),
//            new MazeCollision(156, 195, 8, 24),
//            new MazeCollision(132, 219, 72, 8),
//            new MazeCollision(4, 243, 216, 4),
//
//            // Ghost Cage
//            new MazeCollision(84, 99, 19, 4),
//            new MazeCollision(84, 103, 3, 28),
//            new MazeCollision(87, 128, 49, 3),
//            new MazeCollision(136, 99, 4, 32),
//            new MazeCollision(120, 99, 16, 4),
//            new MazeCollision(104, 99, 15, 4, true)
//    };
//
//    private final GameCollision[] gameCollision = new GameCollision[] {
//            new GameCollision(14, 14), // Pac Man 0
//            new GameCollision(GHOST, GHOST), // Ghost 1
//            new GameCollision(2, 2), // Dot 2
//            new GameCollision(8, 8), // Energizer 3
//            new GameCollision(14, 14) // Fruit 4
//    };
//
//    public GuiPacMan (World world, TileEntityArcade tileEntity, EntityPlayer player) {
//        super (world, tileEntity, player);
//        setGuiSize(GUI_X, GUI_Y, 0.8F);
//        setTexture(texture, 512, 512);
//
//        tempPoints = ediblePoint.clone();
//        inMenu = false;
//    }
//
//    @Override
//    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
//        playX = xScaled - (GUI_X / 2) + 5;
//        playY = yScaled - (GUI_Y / 2) + 14;
//
//        super.drawScreen(mouseX, mouseY, partialTicks);
//
//        this.mc.getTextureManager().bindTexture(texture);
//        this.drawModalRectWithCustomSizedTexture(xScaled - (GUI_X / 2) + 5, yScaled - (GUI_Y / 2) + 14, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);
//
//        if (levelComplete) nextLevel();
//
//        // Maze
//        if ((tickCounter - energizerTick) >= 6) {
//            energizerTick = tickCounter;
//            if (ENERGIZER_STATE == 0) ENERGIZER_STATE = 1;
//            else if (ENERGIZER_STATE == 1) ENERGIZER_STATE = 0;
//        }
//        drawEdibles(false);
//
//        /* Ghost Blink per level and fright time
//                Level | Fright Time | # of Blinks (Flashes)
//            -   1     | 6 sec       | 5
//            -   2     | 5 sec       | 5
//            -   3     | 4 sec       | 5
//            -   4     | 3 sec       | 5
//            -   5     | 2 sec       | 5
//            -   6     | 5 sec       | 5
//            -   7     | 2 sec       | 5
//            -   8     | 2 sec       | 5
//            -   9     | 1 sec       | 3
//            -   10    | 5 sec       | 5
//            -   11    | 2 sec       | 5
//            -   12    | 1 sec       | 3
//            -   13    | 1 sec       | 3
//            -   14    | 3 sec       | 5
//            -   15    | 1 sec       | 3
//            -   16    | 1 sec       | 3
//            -   17    | 0 sec       | 0
//            -   18    | 1 sec       | 3
//            -   19    | 0 sec       | 0
//            -   20    | 0 sec       | 0
//            -   21+   | 0 sec       | 0
//         */
//        // Ghost
//        if ((tickCounter - blinkTick) >= 5 && blink) {
//            blinkTick = tickCounter;
//            if (FRIGHT_STATE == 0) FRIGHT_STATE = 1;
//            else if (FRIGHT_STATE == 1) FRIGHT_STATE = 0;
//        }
//        if ((tickCounter - ghostTick) >= 4) {
//            ghostTick = tickCounter;
//            if (GHOST_STATE == 0) GHOST_STATE = 1;
//            else if (GHOST_STATE == 1) GHOST_STATE = 0;
//        }
//        if ((tickCounter - frightTick) == 70) blink = true;
//        if ((tickCounter - frightTick) == 90) {
//            frightTick = tickCounter;
//            for (int i = 0; i < 4; i++) frightened[i] = false;
//            blink = false;
//        }
//        blinkyAi();
//
//        pinkyAi();
//        inkyAi();
//        clydeAi();
//
//        if (!died) {
//            checkHouse();
//
//            if (spawnFruit) {
//                if ((tickCounter - fruitTick) == fruitTime) {
//                    //Arcade.logger.info("Timer done");
//                    fruitTick = tickCounter;
//                    spawnFruit = false;
//                    //if (fruit != 4) fruit += 2;
//                    if (fruit != 2) fruit++;
//                } else {
//                    //Arcade.logger.info(String.format("Tick: [%d]; FruitTick: [%d]; Diff: [%d]; Time: [%d]", tickCounter, fruitTick, (tickCounter - fruitTick), fruitTime));
//                    drawFruit(25, 32, false);
//                }
//            }
//
//            // Maze Debug
//            //drawMazeCollision(true);
//
//            // Pac-Man
//            if (!start) {
//                // Fruit Spawning
//                //if ((fruit != 4 && !spawnFruit) || fruit < 4) {
//                if ((fruit != 2 && !spawnFruit) || fruit < 2) {
//                    if (dotCounter[5] == 70 || dotCounter[5] == 170) {
//                        fruitTick = tickCounter;
//                        fruitTime = getFruitTick();
//                        spawnFruit = true;
//                    }
//                }
//
//                if ((tickCounter - movementTick) >= 2) {
//                    movementTick = tickCounter;
//                    if (canMoveUp(0) && direction[0] == Direction.UP) {
//                        if (PACMAN_STATE == 0) PACMAN_STATE = 1;
//                        else PACMAN_STATE = 0;
//                        playerY--;
//                    } else if (canMoveDown(0) && direction[0] == Direction.DOWN) {
//                        if (PACMAN_STATE == 0) PACMAN_STATE = 1;
//                        else PACMAN_STATE = 0;
//                        playerY++;
//                    } else if (canMoveLeft(0) && direction[0] == Direction.LEFT) {
//                        if (PACMAN_STATE == 0) PACMAN_STATE = 1;
//                        else PACMAN_STATE = 0;
//                        playerX--;
//                    } else if (canMoveRight(0) && direction[0] == Direction.RIGHT) {
//                        if (PACMAN_STATE == 0) PACMAN_STATE = 1;
//                        else PACMAN_STATE = 0;
//                        playerX++;
//                    }
//                }
//            }
//            drawPacman(playerX, playerY, direction[0], false);
//            if (level >= 0 && level <= 3) {
//                // 4 second limit
//                if ((tickCounter - timeSinceEaten) == (4 * 20)) {
//                    timeSinceEaten = tickCounter;
//                    // Kick ghost out
//                }
//            } else {
//                // 3 second limit
//                if ((tickCounter - timeSinceEaten) == (3 * 20)) {
//                    timeSinceEaten = tickCounter;
//                    // Kick ghost out
//                }
//            }
//
//            checkLevel();
//
//            // Controls
//            if (canMoveLeft(0) && nextDirection == Direction.LEFT) {
//                direction[0] = Direction.LEFT;
//                nextDirection = Direction.STAND;
//            } else if (canMoveRight(0) && nextDirection == Direction.RIGHT) {
//                direction[0] = Direction.RIGHT;
//                nextDirection = Direction.STAND;
//            } else if (canMoveUp(0) && nextDirection == Direction.UP) {
//                direction[0] = Direction.UP;
//                nextDirection = Direction.STAND;
//            } else if (canMoveDown(0) && nextDirection == Direction.DOWN) {
//                direction[0] = Direction.DOWN;
//                nextDirection = Direction.STAND;
//            }
//
//            // Sound
//            if (playSiren) {
//                playSiren = false;
//                this.mc.getSoundHandler().playSound(new LoopingSound(ArcadeSoundRegistry.PACMAN_SIREN, SoundCategory.BLOCKS)); // Repeats a few times before stopping completely.
//            }
//        } else { // ded
//            if (resetState) {
//                resetState = false;
//                PACMAN_STATE = 0;
//            }
//
//            if (playDeath) {
//                playDeath = false;
//                this.mc.getSoundHandler().playSound(new PositionedSoundRecord(ArcadeSoundRegistry.PACMAN_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F, getTileEntity().getPos()));
//            }
//
//            if ((tickCounter - animationTick) >= animationSpeed) {
//                animationTick = tickCounter;
//                if (PACMAN_STATE != 13) PACMAN_STATE++;
//            }
//            deathAnimation();
//
//            if (PACMAN_STATE == 13) {
//                // Check lives. if any left. reset. if not. game over
//                if (life == 0) {
//                    // Game Over
//                } else {
//                    life--;
//                    resetLevel();
//                }
//            }
//        }
//        drawLives();
//        drawFruit(50, 61, false);
//
//        // if gameOver = true this.fontRendererObj.drawString("Game Over", xScaled - (this.fontRendererObj.getStringWidth("Game Over") / 2), yScaled + 8, Color.RED.getRGB());
//        if ((tickCounter - startTick) >= (20 * 3) && start) start = false;
//        if (start) this.fontRendererObj.drawString("Ready!", xScaled - (this.fontRendererObj.getStringWidth("Ready!") / 2), yScaled + 8, Color.YELLOW.getRGB());
//
//        // Score Text
//        this.fontRendererObj.drawString("Score: " + score, xScaled - (GUI_X / 2) + 6, yScaled - (GUI_Y / 2) + 6, Color.WHITE.getRGB());
//    }
//
//    @Override
//    public void updateScreen () {
//        super.updateScreen();
//        // Pac-Man Collision Check
//        checkCenter();
//    }
//
//    @Override
//    protected void keyTyped (char typedChar, int keyCode) throws IOException {
//        super.keyTyped(typedChar, keyCode);
//
//        if (keyCode == KeyHandler.left.getKeyCode()) {
//            if (inMenu) {
//            } else {
//                nextDirection = Direction.LEFT;
//            }
//        }
//        if (keyCode == KeyHandler.right.getKeyCode()) {
//            if (inMenu) {
//            } else {
//                nextDirection = Direction.RIGHT;
//            }
//        }
//        if (keyCode == KeyHandler.up.getKeyCode()) {
//            if (inMenu) {
//            } else {
//                nextDirection = Direction.UP;
//            }
//        }
//        if (keyCode == KeyHandler.down.getKeyCode()) {
//            if (inMenu) {
//            } else {
//                nextDirection = Direction.DOWN;
//            }
//        }
//    }
//
//    /*
//    private int[][] ghostPos = {
//            { 25, 20 }, // Blinky
//            { 21, 26 }, // Inky
//            { 25, 26 }, // Pinky
//            { 29, 26 } // Clyde
//    };
//     */
//
//    // life is lost = ghost is eaten
//    // Ghost's individual counter is deactivated (but not reset) when a life is lost
//    // The global counter is activated and reset everytime a life is lost
//    // Global dot counter release
//    // Pinky = 7
//    // Inky = 17
//    // To deactivate the global counter, Clyde has to be in the house when the counter reaches 32
//    // This will reset the global counter to zero and once again use the ghost's separate counter
//    private int getDotLimit (int level, int ghost) {
//        if (ghost == 2) return 0; // Pinky
//        else if (ghost == 1) { // Inky
//            if (level == 0) return 30;
//            else return 0;
//        } else if (ghost == 3) { // Clyde
//            if (level == 0) return 60;
//            else if (level == 1) return 50;
//            else return 0;
//        }
//        return 0;
//    }
//
//    private void deathAnimation () {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//
//        int x = playX + 5 + (playerX * speedMultiplier);
//        int y = playY + 4 + (playerY * speedMultiplier);
//
//        switch (direction[0].getDirection()) {
//            case 0: // Still
//                this.drawModalRectWithCustomSizedTexture(x, y, 0, GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 1: // Up
//                this.drawModalRectWithCustomSizedTexture(x, y, (PACMAN * PACMAN_STATE), GUI_Y + (PACMAN * 2) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 2: // Down
//                this.drawModalRectWithCustomSizedTexture(x, y, (PACMAN * PACMAN_STATE), GUI_Y + (PACMAN * 3) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 3: // Left
//                this.drawModalRectWithCustomSizedTexture(x, y, (PACMAN * PACMAN_STATE), GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 4: // Right
//                this.drawModalRectWithCustomSizedTexture(x, y, (PACMAN * PACMAN_STATE), GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//        }
//    }
//
//    private void checkHouse () {
//        // Some how the check not working is making everything mostly work...
//        if (ghostPos[2][0] != 25 && ghostPos[2][1] != 26 && inHouse[2]) { // Pinky
//            inHouse[2] = false;
//            // Start Inky's dotCounter
//            if (inHouse[1]) {
//                curCounter = 1;
//                ghostPos[1][0] = 25;
//            }
//        }
//        if (ghostPos[1][0] != 25 && ghostPos[1][1] != 26 && inHouse[1]) { // Inky
//            inHouse[1] = false;
//            // Start Clyde's dotCounter
//            if (inHouse[3]) {
//                curCounter = 3;
//                ghostPos[3][0] = 25;
//            }
//        }
//        if (ghostPos[3][0] != 25 && ghostPos[3][1] != 26 && inHouse[3]) { // Clyde
//            inHouse[3] = false;
//        }
//    }
//
//    private void checkLevel () {
//        int c = 0;
//
//        for (int i = 0; i < tempPoints.length; i++) {
//            if (tempPoints[i] == null) c++;
//        }
//
//        if (c == tempPoints.length) levelComplete = true;
//    }
//
//    private void nextLevel () {
//        levelComplete = false;
//        tempPoints = ediblePoint.clone();
//
//        if (level < 256) level++;
//        // if level = 256, end game.
//
//        dotCounter[5] = 0;
//        fruit = 0;
//
//        resetLevel();
//    }
//
//    // Set Ghost back into pen and Pac-Man back into start position.
//    // Blinky Scatter is messed up when 1 life is lost
//    // Pinky leaves pen from the side
//    // Pinky hit box doesnt work well with detecting pacman
//    private void resetLevel () {
//        playerX = 25;
//        playerY = 44;
//        ghostPos = new int[][] {
//                { 25, 20 }, // Blinky
//                { 21, 26 }, // Inky
//                { 25, 26 }, // Pinky
//                { 29, 26 } // Clyde
//        };
//        died = false;
//        playSiren = true;
//        playDeath = true;
//        resetState = true;
//        inHouse = new boolean[] { false, true, true, true };
//        curCounter = 0;
//        for (int i = 0; i < 5; i++) dotCounter[i] = 0;
//        direction[0] = Direction.STAND;
//        PACMAN_STATE = 0;
//        mode = new int[] { 0, 0, 0, 0 };
//        period = new int[] { 0, 0, 0, 0 };
//        frightened = new boolean[] { false, false, false, false };
//        eaten = new boolean[] { false, false, false, false };
//        startTick = 0;
//        start = true;
//    }
//
//    private void drawGhost (int x, int y, Direction direction, int ghost, boolean frightened, boolean eaten, boolean debug) {
//        if (!eaten) {
//            if (!frightened) {
//                float[] body = colorToFloat(ghostColor[ghost]);
//                float[] eye = colorToFloat(eyeColor[0]);
//
//                GlStateManager.color(body[0], body[1], body[2]);
//                // Body
//                if (GHOST_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
//                else this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);
//
//                // Eye
//                GlStateManager.color(1.0F, 1.0F, 1.0F);
//                switch (direction.getDirection()) {
//                    case 0: // Still
//                        break;
//                    case 1: // Up
//                        // Eye
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        // Pupil
//                        GlStateManager.color(eye[0], eye[1], eye[2]);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        break;
//                    case 2: // Down
//                        // Eye
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        // Pupil
//                        GlStateManager.color(eye[0], eye[1], eye[2]);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        break;
//                    case 3: // Left
//                        // Eye
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        // Pupil
//                        GlStateManager.color(eye[0], eye[1], eye[2]);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        break;
//                    case 4: // Right
//                        // Eye
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                        // Pupil
//                        GlStateManager.color(eye[0], eye[1], eye[2]);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 5, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 7, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                        break;
//                }
//            } else {
//                if (FRIGHT_STATE == 0) { // Blue
//                    // Body
//                    float[] body = colorToFloat(ghostColor[5]);
//                    GlStateManager.color(body[0], body[1], body[2]);
//                    if (GHOST_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
//                    else this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);
//
//                    // Pupil
//                    float[] eye = colorToFloat(eyeColor[1]);
//                    GlStateManager.color(eye[0], eye[1], eye[2]);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//
//                    // Mouth
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 8, GUI_X, MAZE_Y + GHOST, MOUTH_X, MOUTH_Y, 512, 512);
//                } else if (FRIGHT_STATE == 1) { // White
//                    // Body
//                    float[] body = colorToFloat(ghostColor[4]);
//                    GlStateManager.color(body[0], body[1], body[2]);
//                    if (GHOST_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512);
//                    else this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512);
//
//                    // Pupil
//                    float[] eye = colorToFloat(eyeColor[2]);
//                    GlStateManager.color(eye[0], eye[1], eye[2]);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//
//                    // Mouth
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 8, GUI_X, MAZE_Y + GHOST, MOUTH_X, MOUTH_Y, 512, 512);
//                }
//            }
//        } else {
//            float[] eye = colorToFloat(eyeColor[0]);
//
//            // Eye
//            GlStateManager.color(1.0F, 1.0F, 1.0F);
//            switch (direction.getDirection()) {
//                case 0: // Still
//                    break;
//                case 1: // Up
//                    // Eye
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier), GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    // Pupil
//                    GlStateManager.color(eye[0], eye[1], eye[2]);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    break;
//                case 2: // Down
//                    // Eye
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 2, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 4, playY + 5 + (y * ghostSpeedMultiplier) + 3, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    // Pupil
//                    GlStateManager.color(eye[0], eye[1], eye[2]);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 6, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    break;
//                case 3: // Left
//                    // Eye
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    // Pupil
//                    GlStateManager.color(eye[0], eye[1], eye[2]);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 1, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 3, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    break;
//                case 4: // Right
//                    // Eye
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 3, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 5, playY + 5 + (y * ghostSpeedMultiplier) + 2, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
//                    // Pupil
//                    GlStateManager.color(eye[0], eye[1], eye[2]);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + 5, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier) + EYE_X + 7, playY + 5 + (y * ghostSpeedMultiplier) + 4, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
//                    break;
//            }
//        }
//
//        if (debug) {
//            float[] debugColor = colorToFloat(new Color(230, 30, 100));
//            GlStateManager.color(debugColor[0], debugColor[1], debugColor[2]);
//
//            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
//            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right
//
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[1].center[0] + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].center[1] + 4 + (y * ghostSpeedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Center
//        }
//    }
//
//    private void drawLives () {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//
//        if (life == 2 || life == 1) this.drawModalRectWithCustomSizedTexture(playX + PACMAN - 1, playY + 4 + (61 * speedMultiplier), PACMAN, GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//        if (life == 2) this.drawModalRectWithCustomSizedTexture(playX + (PACMAN * 2) - 1, playY + 4 + (61 * speedMultiplier), PACMAN, GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//    }
//
//    private void drawFruit (int x, int y, boolean debug) {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//        // Cherry
//        this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * 4), playY + 4 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2), MAZE_Y, FRUIT, FRUIT, 512, 512);
//        // Strawberry
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 6 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY, MAZE_Y, BERRY_X, BERRY_Y, 512, 512);
//        // Peach
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 6 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY + BERRY_X, MAZE_Y, PEACH_X, PEACH_Y, 512, 512);
//        // Apple
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 6 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY + BERRY_X + PEACH_X, MAZE_Y, APPLE, APPLE, 512, 512);
//        // Grapes
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 5 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY + BERRY_X + PEACH_X + APPLE, MAZE_Y, GRAPE_X, GRAPE_Y, 512, 512);
//        // Galaxian
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 6 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY + BERRY_X + PEACH_X + APPLE + GRAPE_X, MAZE_Y, GALAXIAN, GALAXIAN, 512, 512);
//        // Bell
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 6 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY + BERRY_X + PEACH_X + APPLE + GRAPE_X + GALAXIAN, MAZE_Y, BELL_X, BELL_Y, 512, 512);
//        // Key
//        //this.drawModalRectWithCustomSizedTexture(playX + 6 + (x * 4), playY + 6 + (y * 4), GUI_X + DOT + ENERGIZER + (GHOST * 2) + CHERRY + BERRY_X + PEACH_X + APPLE + GRAPE_X + GALAXIAN + BELL_X, MAZE_Y, KEY_X, KEY_Y, 512, 512);
//
//        if (debug) {
//            GlStateManager.color(0.0F, 1.0F, 1.0F);
//            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
//            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + gameCollision[4].height + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[4].width + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[4].width + 5 + (x * speedMultiplier), playY + gameCollision[4].height + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right
//
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[4].center[0] + 5 + (x * speedMultiplier), playY + gameCollision[4].center[1] + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Center
//        }
//    }
//
//    /* Fruits, Level, Worth
//        - Level 1: Cherry 100pt
//        - Level 2: Strawberry 300pt
//        - Level 3 & 4: Peach 500pt
//        - Level 5 & 6: Apple 700pt
//        - Level 7 & 8: Grapes 1000pt
//        - Level 9 & 10: Galaxian (tulip or thunderbird) 2000pt
//        - Level 11 & 12: Bell 3000pt
//        - Level 13+: Key 5000pt
//     */
//
//    private int getFruitScore () {
//        if (level == 0) return 100; // Cherry
//        if (level == 1) return 300; // Strawberry
//        if (level == 2 || level == 3) return 500; // Peach
//        if (level == 4 || level == 5) return 700; // Apple
//        if (level == 6 || level == 7) return 1000; // Grape
//        if (level == 8 || level == 9) return 2000; // Galaxian
//        if (level == 10 || level == 11) return 3000; // Bell
//        if (level >= 12) return 5000; // Key
//        return 100;
//    }
//
//    private int getFruitTick () {
//        return rand((9 * 20), (10 * 20));
//    }
//
//    private void drawPacman (int x, int y, Direction direction, boolean debug) {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//        switch (direction.getDirection()) {
//            case 0: // Still
//                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), 0, GUI_Y  + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 1: // Up
//                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), (PACMAN * PACMAN_STATE), GUI_Y + (PACMAN * 2) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 2: // Down
//                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), (PACMAN * PACMAN_STATE), GUI_Y + (PACMAN * 3) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 3: // Left
//                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), (PACMAN * PACMAN_STATE), GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//            case 4: // Right
//                this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), (PACMAN * PACMAN_STATE), GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
//                break;
//        }
//
//        if (debug) {
//            GlStateManager.color(0.0F, 1.0F, 0.0F);
//            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
//            this.drawModalRectWithCustomSizedTexture(playX + 5 + (x * speedMultiplier), playY + gameCollision[0].height + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[0].width + 5 + (x * speedMultiplier), playY + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[0].width + 5 + (x * speedMultiplier), playY + gameCollision[0].height + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right
//
//            this.drawModalRectWithCustomSizedTexture(playX + gameCollision[0].center[0] + 5 + (x * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (y * speedMultiplier), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Center
//        }
//    }
//
//    private void drawEnergizer (int x, int y) {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//        if (ENERGIZER_STATE == 0) this.drawModalRectWithCustomSizedTexture(playX + 8 + (x * 8), playY + 8 + (y * 8), GUI_X + DOT, MAZE_Y, ENERGIZER, ENERGIZER, 512, 512);
//    }
//
//    private void drawDot (int x, int y) {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//        this.drawModalRectWithCustomSizedTexture(playX + 11 + (x * 8), playY + 11 + (y * 8), GUI_X, MAZE_Y, DOT, DOT, 512, 512);
//    }
//
//    private void drawEdibles (boolean debug) {
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//
//        for (int i = 0; i < tempPoints.length; i++) {
//            if (tempPoints[i] != null) {
//                if (tempPoints[i].isEnergizer) drawEnergizer((int) tempPoints[i].pos.getX(), (int) tempPoints[i].pos.getY());
//                else drawDot((int) tempPoints[i].pos.getX(), (int) tempPoints[i].pos.getY());
//
//                if (debug) {
//                    float[] debugColor = colorToFloat(new Color(0, 144, 200));
//                    GlStateManager.color(debugColor[0], debugColor[1], debugColor[2]);
//                    if (tempPoints[i].isEnergizer) this.drawModalRectWithCustomSizedTexture(playX + 8 + gameCollision[3].center[0] + ((int)tempPoints[i].pos.getX() * 8), playY + 8 + gameCollision[3].center[1] - 1 + ((int)tempPoints[i].pos.getY() * 8), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512);
//                    else this.drawModalRectWithCustomSizedTexture(playX + 11 + gameCollision[2].center[0] + ((int)tempPoints[i].pos.getX() * 8), playY + 11 + gameCollision[2].center[1] - 1 + ((int)tempPoints[i].pos.getY() * 8), GUI_X, MAZE_Y + DOT, 1, 1, 512, 512);
//                }
//            }
//        }
//    }
//
//    @Deprecated
//    public void drawMazeCollision (boolean debug) {
//        float[] allow = colorToFloat(new Color(125, 0, 144));
//        GlStateManager.color(1.0F, 0.0F, 0.0F);
//        for (int i = 0; i < mazeCollision.length; i++) {
//            if (mazeCollision[i].allowGhost) GlStateManager.color(allow[0], allow[1], allow[2]);
//            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x, playY + mazeCollision[i].y, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Left
//            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x, playY + mazeCollision[i].y + mazeCollision[i].height, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Left
//            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x + mazeCollision[i].width, playY + mazeCollision[i].y, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Top Right
//            this.drawModalRectWithCustomSizedTexture(playX + mazeCollision[i].x + mazeCollision[i].width, playY + mazeCollision[i].y + mazeCollision[i].height, GUI_X, MAZE_Y + DOT, 1, 1, 512, 512); // Bottom Right
//        }
//    }
//
//    private int getModeTime (int level, int period) {
//        if (level == 0) { // Level 1
//            if (period == 0 || period == 2) return (7 * 20);
//            else if (period == 1 || period == 3 || period == 5) return (20 * 20);
//            else if (period == 4 || period == 6) return (5 * 20);
//            else if (period == 7) return 0; // Indefinite
//        } else if (level > 0 && level < 4) { // Level 2-4
//            if (period == 0 || period == 2) return (7 * 20);
//            else if (period == 1 || period == 3) return (20 * 20);
//            else if (period == 5) return (1033 * 20);
//            else if (period == 4) return (5 * 20);
//            else if (period == 6) return 1;
//            else if (period == 7) return 0; // Indefinite
//        } else if (level > 3) { // Level 5+
//            if (period == 0 || period == 2 || period == 4) return (5 * 20);
//            else if (period == 1 || period == 3) return (20 * 20);
//            else if (period == 5) return (1037 * 20);
//            else if (period == 6) return 1;
//            else if (period == 7) return 0; // Indefinite
//        }
//        return 0;
//    }
//
//    /*  Level | Normal | Dots | Fright | Energizer
//        1     | 80%    | 71%  | 90%    | 79%
//        2-4   | 90%    | 79%  | 95%    | 83%
//        5-20  | 100%   | 87%  | 100%   | 87%
//        21+   | 90%    | 79%  | -      | -
//     */
//    private int getPacManSpeed (int level) {
//        if (level == 1) return 4;
//        if (level > 1 && level < 5) return 5;
//        else if (level >= 5 && level < 21) return 6;
//        else if (level >= 21) return 5;
//        return 4;
//    }
//
//    /*  Level | Normal | Fright | Tunnel
//        1     | 75%    | 50%    | 40%
//        2-4   | 85%    | 55%    | 45%
//        5-20  | 95%    | 60%    | 50%
//        21+   | 95%    | -      | 50%
//     */
//    private int getGhostSpeed (int level) {
//        return 1;
//    }
//
//    private boolean canMoveLeft (int character) {
//        if (character == 0) {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int y = 0; y <= gameCollision[character].height; y++) {
//                    if ((playX + 5 + (playerX * speedMultiplier)) == (playX + mazeCollision[c].x + mazeCollision[c].width + 1)) {
//                        if ((playY + y + 4 + (playerY * speedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (playerY * speedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
//                    }
//                }
//            }
//        } else {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int y = 0; y <= gameCollision[1].height; y++) {
//                    if ((playX + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) == (playX + mazeCollision[c].x + mazeCollision[c].width + 1)) {
//                        if ((playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean canMoveRight (int character) {
//        if (character == 0) {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int y = 0; y <= gameCollision[character].height; y++) {
//                    if ((playX + gameCollision[character].width + 5 + (playerX * speedMultiplier)) == (playX + mazeCollision[c].x - 1)) {
//                        if ((playY + y + 4 + (playerY * speedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (playerY * speedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
//                    }
//                }
//            }
//        } else {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int y = 0; y <= gameCollision[1].height; y++) {
//                    if ((playX + gameCollision[1].width + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) == (playX + mazeCollision[c].x - 1)) {
//                        if ((playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) >= (playY + mazeCollision[c].y) && (playY + y + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) <= (playY + mazeCollision[c].y + mazeCollision[c].height)) return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean canMoveUp (int character) {
//        if (character == 0) {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int x = 0; x <= gameCollision[character].width; x++) {
//                    if ((playY + 4 + (playerY * speedMultiplier)) == (playY + mazeCollision[c].y + mazeCollision[c].height + 1)) {
//                        if ((playX + x + 5 + (playerX * speedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (playerX * speedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width)) return false;
//                    }
//                }
//            }
//        } else {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int x = 0; x <= gameCollision[1].width; x++) {
//                    if (!mazeCollision[c].allowGhost) {
//                        if ((playY + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) == (playY + mazeCollision[c].y + mazeCollision[c].height + 1)) {
//                            if ((playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width))
//                                return false;
//                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean canMoveDown (int character) {
//        if (character == 0) {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int x = 0; x <= gameCollision[character].width; x++) {
//                    if ((playY + gameCollision[character].height + 4 + (playerY * speedMultiplier)) == (playY + mazeCollision[c].y - 1)) {
//                        if ((playX + x + 5 + (playerX * speedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (playerX * speedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width)) return false;
//                    }
//                }
//            }
//        } else {
//            for (int c = 0; c < mazeCollision.length; c++) {
//                for (int x = 0; x <= gameCollision[1].width; x++) {
//                    if ((playY + gameCollision[1].height + 4 + (ghostPos[character - 1][1] * ghostSpeedMultiplier)) == (playY + mazeCollision[c].y - 1)) {
//                        if ((playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) >= (playX + mazeCollision[c].x) && (playX + x + 5 + (ghostPos[character - 1][0] * ghostSpeedMultiplier)) <= (playX + mazeCollision[c].x + mazeCollision[c].width)) return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    /*
//    Top Left
//    playX + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier)
//
//    Bottom Left
//    playX + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier)
//
//    Top Right
//    playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + 4 + (y * ghostSpeedMultiplier)
//
//    Bottom Right
//    playX + gameCollision[1].width + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].height + 4 + (y * ghostSpeedMultiplier)
//
//    Center
//    playX + gameCollision[1].center[0] + 5 + (x * ghostSpeedMultiplier), playY + gameCollision[1].center[1] + 4 + (y * ghostSpeedMultiplier)
//     */
//
//    /** Checks Center collider of Character Collider to see if it collided with PacMan or Ghost or dot/energizer */
//    private boolean checkCenter () {
//        int xPlayer = playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier);
//        int yPlayer = playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier);
//        // Edible Check
//        for (int i = 0; i < tempPoints.length; i++) {
//            if (tempPoints[i] != null) {
//                if (xPlayer == (playX + 11 + gameCollision[2].center[0] + ((int)tempPoints[i].pos.getX() * 8)) && yPlayer == (playY + 11 + gameCollision[2].center[1] - 1 + ((int)tempPoints[i].pos.getY() * 8))) {
//                    if (WAKA == 0) {
//                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_1, 1.0F));
//                        WAKA = 1;
//                    } else if (WAKA == 1) {
//                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_2, 1.0F));
//                        WAKA = 2;
//                    } else if (WAKA == 2) {
//                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_3, 1.0F));
//                        WAKA = 3;
//                    } else if (WAKA == 3) {
//                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_4, 1.0F));
//                        WAKA = 4;
//                    } else if (WAKA == 4) {
//                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_5, 1.0F));
//                        WAKA = 5;
//                    } else if (WAKA == 5) {
//                        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ArcadeSoundRegistry.PACMAN_WAKA_6, 1.0F));
//                        WAKA = 0;
//                    }
//                    if (tempPoints[i].isEnergizer) {
//                        // Check for energizer sound
//                        score += 50;
//                        FRIGHT_STATE = 0;
//                        frightTick = tickCounter;
//
//                        for (int g = 0; g < 4; g++) {
//                            //if (!inHouse[g]) frightened[g] = true;
//                            frightened[g] = true;
//                        }
//                    } else score += 10;
//                    dotCounter[5]++; // Level Counter
//                    dotCounter[4]++; // Global Counter
//                    dotCounter[curCounter]++;
//                    timeSinceEaten = tickCounter;
//                    tempPoints[i] = null;
//                }
//            }
//        }
//
//        // Check Pac-Man Ghost Collision
//        for (int i = 0; i < 4; i++) {
//            int xGhost = playX + gameCollision[1].center[0] + 5 + (ghostPos[i][0] * ghostSpeedMultiplier);
//            int yGhost = playY + gameCollision[1].center[1] + 4 + (ghostPos[i][1] * ghostSpeedMultiplier);
//
//            if (((xPlayer - 4) == xGhost && yPlayer == yGhost) || (xPlayer == xGhost && (yPlayer - 4) == yGhost)) {
//                if (frightened[i]) eaten[i] = true;
//                else died = true;
//            }
//            if (xPlayer == xGhost && yPlayer == yGhost) {
//                if (frightened[i]) eaten[i] = true;
//                else died = true;
//            }
//            if (((xPlayer + 4) == xGhost && yPlayer == yGhost) || (xPlayer == xGhost && (yPlayer + 4) == yGhost)) {
//                if (frightened[i]) eaten[i] = true;
//                else died = true;
//            }
//        }
//
//        // Fruit Collision Detection
//        if ((xPlayer == (playX + gameCollision[4].center[0] + 5 + (25 * speedMultiplier))) && (yPlayer == (playY + gameCollision[4].center[1] + 4 + (32 * speedMultiplier))) && spawnFruit) {
//            //Arcade.logger.info("Collided with Fruit");
//
//            // Add score for fruit. Increment fruit. Fruit can only spawn max twice a level
//            //if (fruit != 4) fruit++;
//            //score += (getFruitScore() / 2);
//            if (fruit != 2) fruit++;
//            score += getFruitScore();
//            spawnFruit = false;
//        }
//        return false;
//    }
//
//    /* Ghost AI Basics
//        - Scatter, Chase, Repeat
//        - Frightened Mode
//                - Choose random direction with random number gen
//                - If direction leads to wall, try going up, left, down, right
//     */
//
//    // When eaten if ghost is at (25, 20), set to true. When true, move ghost to (25, 26) then set eaten to false and inPlace to false
//    private boolean inPlace[] = { false, false, false, false };
//
//    private void blinkyAi () {
//        if (!start) {
//            if (!died) {
//                if ((tickCounter - modeTick[0]) >= getModeTime(level, period[0])) {
//                    modeTick[0] = tickCounter;
//
//                    if (period[0] == 0 || period[0] == 2 || period[0] == 4 || period[0] == 6) mode[0] = 0; // Scatter
//                    else mode[0] = 1; // Chase
//
//                    if (period[0] != 7) period[0]++;
//                }
//
//                // Movement
//                if ((tickCounter - ghostMovementTick[0]) >= 2) {
//                    ghostMovementTick[0] = tickCounter;
//
//                    if (!frightened[0]) {
//                        // Scatter
//                        if (mode[0] == 0) direction[1] = pathfinding(1, playX + 12 + (50 * 4), playY + 11);
//                        else if (mode[0] == 1) direction[1] = pathfinding(1, playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier)); // Chase
//
//                        //direction[1] = pathfinding(1, playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));
//
//                        // Move
//                        if (canMoveUp(1) && direction[1] == Direction.UP) ghostPos[0][1]--;
//                        if (canMoveDown(1) && direction[1] == Direction.DOWN) ghostPos[0][1]++;
//                        if (canMoveLeft(1) && direction[1] == Direction.LEFT) ghostPos[0][0]--;
//                        if (canMoveRight(1) && direction[1] == Direction.RIGHT) ghostPos[0][0]++;
//                    } else { // Fright AI
//                        // ghostPos[0][0] == (playX + gameCollision[0].center[0] + 5 + (25 * speedMultiplier)) && ghostPos[0][1] == (playY + gameCollision[0].center[1] + 4 + (20 * speedMultiplier))
//                        if (eaten[0]) {
//                            if (ghostPos[0][0] == (playX + gameCollision[0].center[0] + 5 + (25 * speedMultiplier)) && ghostPos[0][1] == (playY + gameCollision[0].center[1] + 4 + (20 * speedMultiplier))) {
//                                // Move down until equal (25, 26) then set eaten to false and leave ghost in house
//                                if (!inPlace[0]) inPlace[0] = true;
//                            } else if (ghostPos[0][0] == (playX + gameCollision[0].center[0] + 5 + (25 * speedMultiplier)) && ghostPos[0][1] == (playY + gameCollision[0].center[1] + 4 + (20 * speedMultiplier))) {
//                                if (inPlace[0]) {
//                                    inPlace[0] = false;
//                                    eaten[0] = false;
//                                }
//                            } else direction[1] = pathfinding(1, playX + gameCollision[0].center[0] + 5 + (25 * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (20 * speedMultiplier));
//
//                            if (inPlace[0]) direction[1] = pathfinding(1, playX + gameCollision[0].center[0] + 5 + (25 * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (26 * speedMultiplier));
//
//                            // Change eaten speed
//                            if (canMoveUp(1) && direction[1] == Direction.UP) ghostPos[0][1] -= 2;
//                            if (canMoveDown(1) && direction[1] == Direction.DOWN) ghostPos[0][1] += 2;
//                            if (canMoveLeft(1) && direction[1] == Direction.LEFT) ghostPos[0][0] -= 2;
//                            if (canMoveRight(1) && direction[1] == Direction.RIGHT) ghostPos[0][0] += 2;
//                        } else {
//                            // Set Position
//                            if (canMoveUp(1) && direction[1] == Direction.UP) ghostPos[0][1]--;
//                            if (canMoveDown(1) && direction[1] == Direction.DOWN) ghostPos[0][1]++;
//                            if (canMoveLeft(1) && direction[1] == Direction.LEFT) ghostPos[0][0]--;
//                            if (canMoveRight(1) && direction[1] == Direction.RIGHT) ghostPos[0][0]++;
//
//                            // Add probability to movement
//                            if (canMoveUp(1) && direction[1].getDirection() != Direction.UP.getOpposite()) {
//                                if (getWorld().rand.nextInt(2) == 1) direction[1] = Direction.UP;
//                            }
//                            if (canMoveDown(1) && direction[1].getDirection() != Direction.DOWN.getOpposite()) {
//                                if (getWorld().rand.nextInt(2) == 1) direction[1] = Direction.DOWN;
//                            }
//                            if (canMoveLeft(1) && direction[1].getDirection() != Direction.LEFT.getOpposite()) {
//                                if (getWorld().rand.nextInt(2) == 1) direction[1] = Direction.LEFT;
//                            }
//                            if (canMoveRight(1) && direction[1].getDirection() != Direction.RIGHT.getOpposite()) {
//                                if (getWorld().rand.nextInt(2) == 1) direction[1] = Direction.RIGHT;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        drawGhost(ghostPos[0][0], ghostPos[0][1], direction[1], 0, frightened[0], eaten[0], false);
//    }
//
//    private void inkyAi () {
//        if (!start) {
//            if (!died) {
//                if ((tickCounter - ghostMovementTick[1]) >= 2) {
//                    ghostMovementTick[1] = tickCounter;
//
//                    if (dotCounter[1] >= getDotLimit(level, 1)) {
//                        if (!frightened[1]) {
//                            if (mode[1] == 0) direction[2] = pathfinding(2, playX + gameCollision[0].center[0] + 5 + (30 * 4), playY + gameCollision[0].center[1] + 4 + (56 * 4)); // Scatter
//                            else { // Chase
//                                //direction[2] = pathfinding(2, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY - 4) * speedMultiplier));
//                            }
//
//                            // Move
//                            if (canMoveUp(2) && direction[2] == Direction.UP) ghostPos[1][1]--;
//                            if (canMoveDown(2) && direction[2] == Direction.DOWN) ghostPos[1][1]++;
//                            if (canMoveLeft(2) && direction[2] == Direction.LEFT) ghostPos[1][0]--;
//                            if (canMoveRight(2) && direction[2] == Direction.RIGHT) ghostPos[1][0]++;
//                        }
//                    }
//                }
//            }
//        }
//        drawGhost(ghostPos[1][0], ghostPos[1][1], direction[2], 1, frightened[1], eaten[1], false);
//    }
//
//    private void pinkyAi () {
//        if (!start) {
//            if (!died) {
//                if ((tickCounter - ghostMovementTick[2]) >= 2) {
//                    ghostMovementTick[2] = tickCounter;
//
//                    if (dotCounter[2] >= getDotLimit(level, 2)) {
//                        if (!frightened[2]) {
//                            if (mode[2] == 0) direction[3] = pathfinding(3, playX + 12, playY + 11); // Scatter
//                            else { // Chase
//                                if (direction[0] == Direction.UP) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY - 4) * speedMultiplier));
//                                else if (direction[0] == Direction.DOWN) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + (playerX * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY + 4) * speedMultiplier));
//                                else if (direction[0] == Direction.LEFT) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));
//                                else if (direction[0] == Direction.RIGHT) direction[3] = pathfinding(3, playX + gameCollision[0].center[0] + 5 + ((playerX + 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + (playerY * speedMultiplier));
//                            }
//
//                            // Move
//                            if (canMoveUp(3) && direction[3] == Direction.UP) ghostPos[2][1]--;
//                            if (canMoveDown(3) && direction[3] == Direction.DOWN) ghostPos[2][1]++;
//                            if (canMoveLeft(3) && direction[3] == Direction.LEFT) ghostPos[2][0]--;
//                            if (canMoveRight(3) && direction[3] == Direction.RIGHT) ghostPos[2][0]++;
//                        }
//                    }
//                }
//            }
//        }
//        drawGhost(ghostPos[2][0], ghostPos[2][1], direction[3], 2, frightened[2], eaten[2], false);
//    }
//
//    private void clydeAi () {
//        if (!start) {
//            if (!died) {
//                if ((tickCounter - ghostMovementTick[3]) >= 2) {
//                    ghostMovementTick[3] = tickCounter;
//
//                    if (dotCounter[3] >= getDotLimit(level, 3)) {
//                        if (!frightened[3]) {
//                            if (mode[3] == 0) direction[4] = pathfinding(4, playX + gameCollision[0].center[0] + 5 + (20 * 4), playY + gameCollision[0].center[1] + 4 + (56 * 4)); // Scatter
//                            else { // Chase
//                                //direction[2] = pathfinding(2, playX + gameCollision[0].center[0] + 5 + ((playerX - 4) * speedMultiplier), playY + gameCollision[0].center[1] + 4 + ((playerY - 4) * speedMultiplier));
//                            }
//
//                            // Move
//                            if (canMoveUp(4) && direction[4] == Direction.UP) ghostPos[3][1]--;
//                            if (canMoveDown(4) && direction[4] == Direction.DOWN) ghostPos[3][1]++;
//                            if (canMoveLeft(4) && direction[4] == Direction.LEFT) ghostPos[3][0]--;
//                            if (canMoveRight(4) && direction[4] == Direction.RIGHT) ghostPos[3][0]++;
//                        }
//                    }
//                }
//            }
//        }
//        drawGhost(ghostPos[3][0], ghostPos[3][1], direction[4], 3, frightened[3], eaten[3], false);
//    }
//
//    private void frightAI (int ghost) {}
//
//    /* Direction ID
//        0 = Still
//        1 = Up
//        2 = Down
//        3 = Left
//        4 = Right
//     */
//
//    // When going up or down, sometimes makes a u-turn which isn't allowed
//    // GL
//    private Direction pathfinding (int ghost, int targetX, int targetY) {
//        int x = playX + gameCollision[1].center[0] + 5 + (ghostPos[ghost - 1][0] * ghostSpeedMultiplier);
//        int y = playY + gameCollision[1].center[1] + 4 + (ghostPos[ghost - 1][1] * ghostSpeedMultiplier);
//
//        int shouldGo = 0;
//
//        double distance = Math.sqrt(Math.pow((targetX - x), 2) + Math.pow((targetY - y), 2));
//
//        double xDistance = Math.abs(targetX - x);
//        double yDistance = Math.abs(targetY - y);
//
//        if (xDistance == 0) {
//            if (yDistance != 0) {
//                if (canMoveUp(ghost) && direction[ghost].getDirection() != Direction.UP.getOpposite()) {
//                    double yPredict = Math.abs(targetY - (y - 1));
//                    if (yDistance > yPredict) return Direction.UP;
//                }
//
//                if (canMoveDown(ghost) && direction[ghost].getDirection() != Direction.DOWN.getOpposite()) {
//                    double yPredict = Math.abs(targetY - (y + 1));
//                    if (yDistance > yPredict) return Direction.DOWN;
//                }
//
//                if (!canMoveDown(ghost) || !canMoveUp(ghost)) {
//                    if (canMoveRight(ghost) && direction[ghost].getDirection() != Direction.RIGHT.getOpposite()) return Direction.RIGHT;
//                    if (canMoveLeft(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) return Direction.LEFT;
//                }
//
//                // Might have to remove
//                if (canMoveRight(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) return Direction.RIGHT;
//                if (canMoveLeft(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) return Direction.LEFT;
//            }
//        } else { // x != 0
//            if (yDistance != 0) {
//                if (canMoveUp(ghost) && direction[ghost].getDirection() != Direction.UP.getOpposite()) {
//                    /*
//                    if (canMoveLeft(ghost) && direction[ghost] != 4) {
//                        double xPredict = Math.abs(targetX - (x - 1));
//                        if (xDistance > xPredict) return 3;
//                    }
//                    if (canMoveRight(ghost) && direction[ghost] != 3) {
//                        double xPredict = Math.abs(targetX - (x + 1));
//                        if (xDistance > xPredict) return 4;
//                    }
//                    */
//                    if (!canMoveRight(ghost) || !canMoveLeft(ghost)) return Direction.UP;
//                    double yPredict = Math.abs(targetY - (y - 1));
//                    if (yDistance > yPredict) return Direction.UP;
//                }
//
//                if (canMoveDown(ghost) && direction[ghost].getDirection() != Direction.DOWN.getOpposite()) {
//                    if (!canMoveRight(ghost) || !canMoveLeft(ghost)) return Direction.DOWN;
//                    double yPredict = Math.abs(targetY - (y + 1));
//                    if (yDistance > yPredict) return Direction.DOWN;
//                }
//
//                if (canMoveLeft(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) {
//                    double xPredict = Math.abs(targetX - (x - 1));
//                    if (xDistance > xPredict) return Direction.LEFT;
//                }
//
//                if (canMoveRight(ghost) && direction[ghost].getDirection() != Direction.RIGHT.getOpposite()) {
//                    double xPredict = Math.abs(targetX - (x + 1));
//                    if (xDistance > xPredict) return Direction.RIGHT;
//                }
//
//                if (!canMoveDown(ghost) || !canMoveUp(ghost)) {
//                    if (canMoveLeft(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) return Direction.LEFT;
//                    if (canMoveRight(ghost) && direction[ghost].getDirection() != Direction.RIGHT.getOpposite()) return Direction.RIGHT;
//                }
//            } else { // y == 0
//                if (canMoveLeft(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) {
//                    if (!canMoveRight(ghost)) {
//                        double xPredict = Math.abs(targetX - (x + 1));
//                        if (xDistance > xPredict) return Direction.LEFT;
//                    }
//                    double xPredict = Math.abs(targetX - (x - 1));
//                    if (xDistance > xPredict) return Direction.LEFT;
//                }
//
//                if (canMoveRight(ghost) && direction[ghost] == Direction.LEFT) {
//                    if (canMoveLeft(ghost)) return Direction.LEFT;
//                    else {
//                        if (canMoveUp(ghost)) return Direction.UP;
//                        if (canMoveDown(ghost)) return Direction.DOWN;
//                    }
//                }
//
//                if (canMoveRight(ghost) && direction[ghost].getDirection() != Direction.RIGHT.getDirection()) {
//                    if (!canMoveLeft(ghost)) { // can't move left so lets make it move left... wat
//                        double xPredict = Math.abs(targetX - (x - 1));
//                        if (xDistance > xPredict) return Direction.LEFT;
//                    }
//                    double xPredict = Math.abs(targetX - (x + 1));
//                    if (xDistance > xPredict) return Direction.RIGHT;
//                }
//
//                if (!canMoveLeft(ghost) || !canMoveRight(ghost)) {
//                    if (canMoveUp(ghost) && direction[ghost].getDirection() != Direction.UP.getOpposite()) return Direction.UP;
//                    if (canMoveDown(ghost) && direction[ghost].getDirection() != Direction.DOWN.getOpposite()) return Direction.DOWN;
//                }
//            }
//        }
//
//        if (canMoveUp(ghost) && direction[ghost].getDirection() != Direction.UP.getOpposite()) return Direction.UP;
//        if (canMoveLeft(ghost) && direction[ghost].getDirection() != Direction.LEFT.getOpposite()) return Direction.LEFT;
//        if (canMoveDown(ghost) && direction[ghost].getDirection() != Direction.DOWN.getOpposite()) return Direction.DOWN;
//        if (canMoveRight(ghost) && direction[ghost].getDirection() != Direction.RIGHT.getOpposite()) return Direction.RIGHT;
//        return Direction.STAND; // This should technically never be called
//    }
//
//    private float[] colorToFloat (Color color) {
//        float red = Math.round((color.getRed() / 255.0F) * 100.0F) / 100.0F;
//        float green = Math.round((color.getGreen() / 255.0F) * 100.0F) / 100.0F;
//        float blue = Math.round((color.getBlue() / 255.0F) * 100.0F) / 100.0F;
//
//        return new float[] { red, green, blue };
//    }
//
//    private int rand (int min, int max) {
//        return getWorld().rand.nextInt(max + 1 - min) + min;
//    }
//
//    public enum Direction {
//        STAND(0, 0),
//        UP(1, 2),
//        DOWN(2, 1),
//        LEFT(3, 4),
//        RIGHT(4, 3);
//
//        private int direction;
//        private int opposite;
//
//        Direction (int direction, int opposite) {
//            this.direction = direction;
//            this.opposite = opposite;
//        }
//
//        public int getDirection () {
//            return direction;
//        }
//
//        public int getOpposite() {
//            return opposite;
//        }
//    }
//
//    public class MazeCollision {
//        public final int x;
//        public final int y;
//        public final int width;
//        public final int height;
//        public final boolean allowGhost;
//
//        public MazeCollision (int x, int y, int width, int height) {
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.allowGhost = false;
//        }
//
//        public MazeCollision (int x, int y, int width, int height, boolean allowGhost) {
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//            this.allowGhost = allowGhost;
//        }
//    }
//
//    public class GameCollision {
//        public final int width;
//        public final int height;
//        public final int[] center;
//
//        public GameCollision (int width, int height) {
//            this.width = width;
//            this.height = height;
//            this.center = new int[] { width / 2, height / 2 };
//        }
//    }
//
//    public class EdiblePoint {
//        public final Point pos;
//        public final boolean isEnergizer;
//
//        public EdiblePoint (int x, int y, boolean isEnergizer) {
//            this.pos = new Point(x, y);
//            this.isEnergizer = isEnergizer;
//        }
//
//        public EdiblePoint (int x, int y) {
//            this.pos = new Point(x, y);
//            this.isEnergizer = false;
//        }
//    }
}
