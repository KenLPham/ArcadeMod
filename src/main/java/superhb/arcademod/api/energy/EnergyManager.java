package superhb.arcademod.api.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyManager extends EnergyStorage implements INBTSerializable<NBTTagCompound> {
    public EnergyManager(int capacity) {
        super(capacity);
    }

    public EnergyManager(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public EnergyManager(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public EnergyManager(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    @Override
    public NBTTagCompound serializeNBT () {
        return null;
    }

    @Override
    public void deserializeNBT (NBTTagCompound compound) {

    }
}
