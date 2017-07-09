package superhb.arcademod.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.network.ClientCoinMessage;
import superhb.arcademod.util.ArcadePacketHandler;
import superhb.arcademod.util.KeyHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import superhb.arcademod.util.RecipeUtil;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit (FMLPreInitializationEvent event) {
        super.preInit(event);
        KeyHandler.preInit();
        ArcadePacketHandler.INSTANCE.registerMessage(ClientCoinMessage.Handler.class, ClientCoinMessage.class, 1, Side.CLIENT);
    }

    @Override
    public void init (FMLInitializationEvent event) {
        super.init(event);
        RecipeUtil.registerRecipes();
    }
}
