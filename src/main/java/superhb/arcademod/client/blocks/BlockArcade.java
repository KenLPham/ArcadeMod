package superhb.arcademod.client.blocks;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import superhb.arcademod.Arcade;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import superhb.arcademod.client.ArcadeBlocks;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.EnumGame;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockArcade extends Block implements IBlockVariant {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum GAME = PropertyEnum.create("game", EnumGame.class);

    public BlockArcade(Material material) {
        super(material);
        setHardness(1.0F);
        setDefaultState(blockState.getBaseState().withProperty(GAME, EnumGame.SNAKE).withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public boolean hasTileEntity (IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity (World world, IBlockState state) {
        return new TileEntityArcade();
    }

    @Override
    public boolean isFullCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable (IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canSpawnInBlock () {
        return false;
    }

    @Override
    public Item getItemDropped (IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public AxisAlignedBB getBoundingBox (IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, (12.0D / 16.0D));
            case SOUTH:
                return new AxisAlignedBB(0.0D, 0.0D, (4.0D / 16.0D), 1.0D, 1.0D, 1.0D);
            case WEST:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, (12.0D / 16.0D), 1.0D, 1.0D);
            case EAST:
                return new AxisAlignedBB((4.0D / 16.0D), 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, (31.0D / 16.0D), (12.0D / 16.0D));
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox (IBlockState state, World world, BlockPos pos) {
        return state.getBoundingBox(world, pos).offset(pos);
    }

    @Override
    public void addCollisionBoxToList (IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean par6) {
        super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, (13.0D / 16.0D), 0.0D, 1.0D, (3.0D / 16.0D), 1.0D));
        switch (state.getValue(FACING)) {
            case NORTH:
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, (27.0D / 16.0D), 0.0D, 1.0D, (30.0D / 16.0D), (14.0D / 16.0D)));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 1.0D, 0.0D, 1.0D, (27.0D / 16.0D), (12.0D / 16.0D)));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, (13.0D / 16.0D), (13.0D / 16.0D)));
                break;
            case SOUTH:
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, (27.0D / 16.0D), (2.0D / 16.0D), 1.0D, (30.0D / 16.0D), 1.0D));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 1.0D, (4.0D / 16.0D), 1.0D, (27.0D / 16.0D), 1.0D));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 0.0D, (3.0D / 16.0D), 1.0D, (13.0D / 16.0D), 1.0D));
                break;
            case WEST:
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, (27.0D / 16.0D), 0.0D, (14.0D / 16.0D), (30.0D / 16.0D), 1.0D));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 1.0D, 0.0D, (12.0D / 16.0D), (27.0D / 16.0D), 1.0D));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 0.0D, 0.0D, (13.0D / 16.0D), (13.0D / 16.0D), 1.0D));
                break;
            case EAST:
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB((2.0D / 16.0D), (27.0D / 16.0D), 0.0D, 1.0D, (30.0D / 16.0D), 1.0D));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB((4.0D / 16.0D), 1.0D, 0.0D, 1.0D, (27.0D / 16.0D), 1.0D));
                super.addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB((3.0D / 16.0D), 0.0D, 0.0D, 1.0D, (13.0D / 16.0D), 1.0D));
                break;
        }
    }

    // TODO: wat
    @Override
    public ItemStack getPickBlock (IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound compound = new NBTTagCompound();
        ItemStack stack = new ItemStack(this);
        compound.setInteger("Game", 0);
        stack.setTagCompound(compound);

        if (tile instanceof TileEntityArcade) {
            TileEntityArcade arcade = (TileEntityArcade)tile;
            compound.setInteger("Game", arcade.getGameID());
            // TODO: setLeaderboard
            stack.setTagCompound(compound);
            return stack;
        }
        return stack;
    }

    @Override
    public IBlockState getActualState (IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityArcade) {
            TileEntityArcade arcade = (TileEntityArcade)tile;
            int id = arcade.getGameID();
            return state.withProperty(GAME, EnumGame.getValue(id));
        }
        return state;
    }

    @Override
    public IBlockState getStateFromMeta (int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState (IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState () {
        return new BlockStateContainer(this, new IProperty[] { FACING, GAME });
    }

    @Override
    public int damageDropped (IBlockState state) {
        return 0;
    }

    @Override
    public void breakBlock (World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy (World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()), ArcadeBlocks.invisible.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
    }

    @Override
    public void onBlockHarvested (World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        world.setBlockToAir(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
    }

    @Override
    public void onBlockDestroyedByExplosion (World world, BlockPos pos, Explosion explosion) {
        world.setBlockToAir(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
    }

    @Override
    public void onBlockDestroyedByPlayer (World world, BlockPos pos, IBlockState state) {
        world.setBlockToAir(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
    }

    @Override
    public IBlockState getStateForPlacement (World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing());
    }

    // TODO: if crouched don't open gui
    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityArcade) {
            TileEntityArcade arcade = (TileEntityArcade)tile;

            if (world.isRemote) player.openGui(Arcade.instance, arcade.getGameID(), world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public String getVariantName (ItemStack stack) {
        if (stack.hasTagCompound()) return EnumGame.getRegistryName(stack.getTagCompound().getInteger("Game"));
        return EnumGame.getRegistryName(0);
    }

    // TODO: Better way to add subtypes to creative tab
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks (Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
        NBTTagCompound snake = new NBTTagCompound();
        ItemStack snakeStack = new ItemStack(item);
        snake.setInteger("Game", 0);
        snakeStack.setTagCompound(snake);
        list.add(snakeStack);

        NBTTagCompound tetrominoes = new NBTTagCompound();
        ItemStack tetrominoesStack = new ItemStack(item);
        tetrominoes.setInteger("Game", 1);
        tetrominoesStack.setTagCompound(tetrominoes);
        list.add(tetrominoesStack);
    }

    @Override
    public boolean eventReceived (IBlockState state, World world, BlockPos pos, int eventId, int eventParam) {
        super.eventReceived(state, world, pos, eventId, eventParam);

        TileEntity tile = world.getTileEntity(pos);

        return tile == null ? false : tile.receiveClientEvent(eventId, eventParam);
    }

    @Override
    public EnumBlockRenderType getRenderType (IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
