package superhb.arcademod.client.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import superhb.arcademod.client.blocks.IBlockVariant;
import superhb.arcademod.tileentity.TileEntityPlushie;

// TODO: Variant model
public class ItemBlockPlushie extends ItemBlock {
    public ItemBlockPlushie (Block block) {
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
        super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());

            NBTTagCompound compound = stack.getTagCompound();
            compound.setInteger("Mob", 0);
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (world.getTileEntity(pos) instanceof TileEntityPlushie) {
            TileEntityPlushie tile = (TileEntityPlushie)world.getTileEntity(pos);
            tile.setMobID(compound.getInteger("Mob"));
        }
        return true;
    }

    @Override
    public void onUpdate (ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
            NBTTagCompound compound = stack.getTagCompound();
            compound.setInteger("Mob", 0);
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
}
