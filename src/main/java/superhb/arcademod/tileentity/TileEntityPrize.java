package superhb.arcademod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class TileEntityPrize extends TileEntity {
    private String customName;

    public TileEntityPrize () {
    }

    public String getCustomName () {
        return customName;
    }

    public void setCustomName (String name) {
        customName = name;
    }

    public String getName () {
        return hasCustomName() ? customName : "tile.arcademod:prize_box.name";
    }

    public boolean hasCustomName () {
        return customName != null && !customName.equals("");
    }

    @Override
    public ITextComponent getDisplayName () {
        return hasCustomName() ? new TextComponentString(getName()) : new TextComponentTranslation(getName());
    }

    @Override
    public NBTTagCompound writeToNBT (NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (hasCustomName()) compound.setString("CustomName", getCustomName());
        return compound;
    }

    @Override
    public void readFromNBT (NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("CustomName", 8)) setCustomName(compound.getString("CustomName"));
    }
}
