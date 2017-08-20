package superhb.arcademod.client.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityCamera extends Entity {
    public EntityCamera (World world, double x, double y, double z) {
        super(world);
        this.width = 0;
        this.height = 0;
        setRotation(0, 45);
        setPositionAndUpdate(x + 0.5D, y + 2.0D, z - 0.5D);
    }

    @Override
    protected void entityInit () {
        //setPositionAndUpdate(x, y + 1.0D, z);
    }

    @Override
    protected void writeEntityToNBT (NBTTagCompound compound) {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }
}
