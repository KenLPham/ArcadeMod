package superhb.arcademod.client.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityClaw extends Entity {
	public EntityClaw (World world, double x, double y, double z) {
		super(world);
	}
	
	@Override
	protected void entityInit () {
	}
	
	@Override
	protected void writeEntityToNBT (NBTTagCompound compound) {
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
	}
}
