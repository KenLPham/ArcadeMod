package superhb.arcademod.client.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.client.blocks.IBlockVariant;
import superhb.arcademod.client.tileentity.TileEntityArcade;
import superhb.arcademod.util.EnumGame;
import superhb.arcademod.util.EnumMob;

public class ItemBlockArcade extends ItemBlock implements IItemMeshDefinition {
    public ItemBlockArcade (Block block) {
        super(block);

        if (!(block instanceof IBlockVariant)) throw new IllegalArgumentException(String.format("The given Block, %s, is not an instance of IBlockVariant", block.getUnlocalizedName()));

        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public int getMetadata (int damage) {
        return damage;
    }

    @Override
    public boolean placeBlockAt (ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        BlockPos up = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        if (world.getBlockState(up).getBlock() != Blocks.AIR) return false;
        else {
            super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

            if (stack.getTagCompound() == null) {
                stack.setTagCompound(new NBTTagCompound());

                NBTTagCompound compound = stack.getTagCompound();
                compound.setInteger("Game", 0);
                // TODO: setLeaderboard
            }
            NBTTagCompound compound = stack.getTagCompound();
            if (world.getTileEntity(pos) instanceof TileEntityArcade) {
                TileEntityArcade tile = (TileEntityArcade) world.getTileEntity(pos);
                tile.setGameID(compound.getInteger("Game"));
                // TODO: setLeaderboard
            }
            return true;
        }
    }

    @Override
    public void onUpdate (ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
            NBTTagCompound compound = stack.getTagCompound();
            compound.setInteger("Game", 0);
            // TODO: setLeaderboard
        }
    }

    // Called when Crafted
    @Override
    public void onCreated (ItemStack stack, World world, EntityPlayer player) {

    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + ((IBlockVariant)block).getVariantName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemMeshDefinition getMeshDefinition () {
        return new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                if (stack.hasTagCompound()) {
                    return new ModelResourceLocation(Reference.MODID + ":arcade_machine", "facing=south,game=" + EnumGame.values()[stack.getTagCompound().getInteger("Game")].getName());
                }
                else return new ModelResourceLocation(Reference.MODID + ":arcade_machine", "facing=south,game=snake");
            }
        };
    }
}
