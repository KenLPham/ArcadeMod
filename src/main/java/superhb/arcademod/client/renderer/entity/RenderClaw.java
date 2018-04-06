package superhb.arcademod.client.renderer.entity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import superhb.arcademod.Reference;
import superhb.arcademod.client.entity.EntityClaw;

public class RenderClaw extends RenderOBJ<EntityClaw> {
	public RenderClaw (RenderManager manager) {
		super(manager);
	}
	
	@Override
	protected ResourceLocation[] getEntityModels () {
		return new ResourceLocation[] { new ResourceLocation(Reference.MODID, "entity/claw.obj") };
	}
	
	// https://github.com/2piradians/Minewatch/blob/1.12.1/src/main/java/twopiradians/minewatch/client/render/entity/RenderJunkratTrap.java
	@Override
	protected boolean preRender (EntityClaw entity, int model, BufferBuilder buffer, double x, double y, double z, float yaw, float partialTick) {
		return true;
	}
}
