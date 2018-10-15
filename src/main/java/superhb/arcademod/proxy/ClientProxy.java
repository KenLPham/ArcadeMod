package superhb.arcademod.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.network.*;
import superhb.arcademod.network.pong.GetPlayerMessage;
import superhb.arcademod.util.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit (FMLPreInitializationEvent event) {
		super.preInit(event);
		
		KeyHandler.registerKeyBinding();
	}
	
	@Override
	public void init (FMLInitializationEvent event) {
		super.init(event);
	}
}
