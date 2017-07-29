package superhb.arcademod.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import superhb.arcademod.Reference;

public class ArcadeSoundRegistry {
    public static SoundEvent TETROMINOES, INSERT_COIN;

    public static void registerSounds () {
        TETROMINOES = registerSound("theme.tetris");
        INSERT_COIN = registerSound("effect.insert");
    }

    private static SoundEvent registerSound(String name) {
        final ResourceLocation sound = new ResourceLocation(Reference.MODID, name);
        return GameRegistry.register(new SoundEvent(sound).setRegistryName(sound));
    }
}
