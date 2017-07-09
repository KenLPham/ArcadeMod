package superhb.arcademod.proxy;

import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.Arcade;
import superhb.arcademod.gui.GuiHandler;
import superhb.arcademod.network.*;
import superhb.arcademod.util.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {
    public void preInit (FMLPreInitializationEvent event) {
        RegisterUtil.registerAll(event);
        ArcadePacketHandler.INSTANCE.registerMessage(ServerCoinMessage.Handler.class, ServerCoinMessage.class, 0, Side.SERVER);
        ArcadePacketHandler.INSTANCE.registerMessage(RewardMessage.Handler.class, RewardMessage.class, 2, Side.SERVER);
    }

    public void init (FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(Arcade.instance, new GuiHandler());
    }

    public void postInit (FMLPostInitializationEvent event) {}
}
