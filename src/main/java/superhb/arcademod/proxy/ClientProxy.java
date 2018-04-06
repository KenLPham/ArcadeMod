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
		
		// Register Client Network Message
		ArcadePacketHandler.INSTANCE.registerMessage(ClientCoinMessage.Handler.class, ClientCoinMessage.class, 0, Side.CLIENT);
		ArcadePacketHandler.INSTANCE.registerMessage(ClientBuyMessage.Handler.class, ClientBuyMessage.class, 1, Side.CLIENT);
		ArcadePacketHandler.INSTANCE.registerMessage(GetPlayerMessage.Handler.class, GetPlayerMessage.class, 5, Side.CLIENT);
	}
	
	@Override
	public void init (FMLInitializationEvent event) {
		super.init(event);
	}
}
