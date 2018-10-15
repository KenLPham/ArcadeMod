package superhb.arcademod.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.UpdateAnnouncer;
import superhb.arcademod.client.gui.GuiHandler;
import superhb.arcademod.network.*;
import superhb.arcademod.network.pong.GetPlayerMessage;
import superhb.arcademod.util.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {
	public void preInit (FMLPreInitializationEvent event) {
		// Register Network Packet
		ArcadePacketHandler.INSTANCE.registerMessage(ServerCoinMessage.Handler.class, ServerCoinMessage.class, 3, Side.SERVER);
		ArcadePacketHandler.INSTANCE.registerMessage(RewardMessage.Handler.class, RewardMessage.class, 4, Side.SERVER);
		ArcadePacketHandler.INSTANCE.registerMessage(ServerBuyMessage.Handler.class, ServerBuyMessage.class, 5, Side.SERVER);

		ArcadePacketHandler.INSTANCE.registerMessage(ClientCoinMessage.Handler.class, ClientCoinMessage.class, 1, Side.CLIENT);
		ArcadePacketHandler.INSTANCE.registerMessage(ClientBuyMessage.Handler.class, ClientBuyMessage.class, 2, Side.CLIENT);
		ArcadePacketHandler.INSTANCE.registerMessage(GetPlayerMessage.Handler.class, GetPlayerMessage.class, 6, Side.CLIENT);

		// Register Event
		if (!Arcade.disableUpdateNotification) MinecraftForge.EVENT_BUS.register(new UpdateAnnouncer());
	}
	
	public void init (FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Arcade.instance, new GuiHandler());
	}
	
	public void postInit (FMLPostInitializationEvent event) {}
}
