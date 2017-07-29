package superhb.arcademod.util;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import superhb.arcademod.client.models.ModelPlushie;

public class ArcadeEventHandler {
    @SubscribeEvent
    public void onModelBake (ModelBakeEvent event) {
        Object obj = event.getModelRegistry().getObject(ModelPlushie.resources[0]);
        if (obj instanceof IBakedModel) {
            IBakedModel existing = (IBakedModel)obj;
            ModelPlushie model = new ModelPlushie(existing);
            event.getModelRegistry().putObject(ModelPlushie.resources[0], model);
        }
    }
}
