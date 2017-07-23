package superhb.arcademod.proxy;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.client.renderer.RendererArcade;
import superhb.arcademod.network.ClientBuyMessage;
import superhb.arcademod.network.ClientCoinMessage;
import superhb.arcademod.client.tileentity.TileEntityTestArcade;
import superhb.arcademod.util.ArcadePacketHandler;
import superhb.arcademod.util.KeyHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import superhb.arcademod.util.RecipeUtil;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit (FMLPreInitializationEvent event) {
        super.preInit(event);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTestArcade.class, new RendererArcade());

        KeyHandler.preInit();
        ArcadePacketHandler.INSTANCE.registerMessage(ClientCoinMessage.Handler.class, ClientCoinMessage.class, 1, Side.CLIENT);
        ArcadePacketHandler.INSTANCE.registerMessage(ClientBuyMessage.Handler.class, ClientBuyMessage.class, 4, Side.CLIENT);
    }

    @Override
    public void init (FMLInitializationEvent event) {
        super.init(event);
        RecipeUtil.registerRecipes();
    }
}
