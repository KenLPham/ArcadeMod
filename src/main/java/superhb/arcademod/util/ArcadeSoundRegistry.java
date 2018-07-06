package superhb.arcademod.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import superhb.arcademod.Reference;
import superhb.arcademod.client.audio.LoopingSound;

public class ArcadeSoundRegistry {
    public static SoundEvent INSERT_COIN;
    public static SoundEvent TETROMINOES;
    public static SoundEvent PACMAN_WAKA_1, PACMAN_WAKA_2, PACMAN_WAKA_3, PACMAN_WAKA_4, PACMAN_WAKA_5, PACMAN_WAKA_6,
            PACMAN_DEATH, PACMAN_FRUIT,
            PACMAN_INTRO, PACMAN_GHOST,
            PACMAN_SIREN, PACMAN_FRIGHT, PACMAN_LIFE,
            PACMAN_ENERGIZER_1, PACMAN_ENERGIZER_2, PACMAN_ENERGIZER_3, PACMAN_ENERGIZER_4, PACMAN_HOME; // TODO: Add Audio
    
    public static SoundEvent SPACEINVADERS;	
    public static SoundEvent SPACEINVADERS_SHOOT;
    public static SoundEvent SPACEINVADERS_EXPLODE;
    public static SoundEvent SPACEINVADERS_DESTROYED;

    public static void registerSounds () {
        // General
        INSERT_COIN = registerSound("effect.insert");

        // Tetrominoes
        TETROMINOES = registerSound("theme.tetris");

        // Pac-Man
        PACMAN_WAKA_1 = registerSound("effect.pacman_waka_1");
        PACMAN_WAKA_2 = registerSound("effect.pacman_waka_2");
        PACMAN_WAKA_3 = registerSound("effect.pacman_waka_3");
        PACMAN_WAKA_4 = registerSound("effect.pacman_waka_4");
        PACMAN_WAKA_5 = registerSound("effect.pacman_waka_5");
        PACMAN_WAKA_6 = registerSound("effect.pacman_waka_6");
        PACMAN_DEATH = registerSound("effect.pacman_death");
        PACMAN_FRUIT = registerSound("effect.pacman_fruit");
        PACMAN_INTRO = registerSound("theme.pacman");
        PACMAN_GHOST = registerSound("effect.pacman_ghost");
        PACMAN_SIREN = registerSound("theme.pacman_siren");
        PACMAN_FRIGHT = registerSound("theme.pacman_fright");
        PACMAN_LIFE = registerSound("effect.pacman_life");
        SPACEINVADERS = registerSound("theme.spaceinvaders");
        SPACEINVADERS_SHOOT = registerSound("effect.spaceinvaders_shoot");
        SPACEINVADERS_EXPLODE = registerSound("effect.spaceinvaders_explode");
        SPACEINVADERS_DESTROYED = registerSound("effect.spaceinvaders_destroyed");
    }

    private static SoundEvent registerSound (String name) {
        final ResourceLocation sound = new ResourceLocation(Reference.MODID, name);
        return GameRegistry.register(new SoundEvent(sound).setRegistryName(sound));
    }
}
