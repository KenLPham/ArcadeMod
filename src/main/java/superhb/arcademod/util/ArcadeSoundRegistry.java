package superhb.arcademod.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import superhb.arcademod.Reference;

@EventBusSubscriber
public class ArcadeSoundRegistry {
	public static SoundEvent INSERT_COIN;
	public static SoundEvent TETROMINOES;
	public static SoundEvent PACMAN_WAKA_1, PACMAN_WAKA_2, PACMAN_WAKA_3, PACMAN_WAKA_4, PACMAN_WAKA_5, PACMAN_WAKA_6, PACMAN_DEATH, PACMAN_FRUIT, PACMAN_INTRO, PACMAN_GHOST, PACMAN_SIREN, PACMAN_FRIGHT, PACMAN_LIFE, PACMAN_ENERGIZER_1, PACMAN_ENERGIZER_2, PACMAN_ENERGIZER_3, PACMAN_ENERGIZER_4, PACMAN_HOME; // TODO: Add Audio
	
	@SubscribeEvent
	public static void registerSounds (RegistryEvent.Register<SoundEvent> event) {
		// General
		INSERT_COIN = registerSound(event.getRegistry(), "effect.insert");
		
		// Tetrominoes
		TETROMINOES = registerSound(event.getRegistry(), "theme.tetris");
		
		// Pac-Man
		PACMAN_WAKA_1 = registerSound(event.getRegistry(), "effect.pacman_waka_1");
		PACMAN_WAKA_2 = registerSound(event.getRegistry(), "effect.pacman_waka_2");
		PACMAN_WAKA_3 = registerSound(event.getRegistry(), "effect.pacman_waka_3");
		PACMAN_WAKA_4 = registerSound(event.getRegistry(), "effect.pacman_waka_4");
		PACMAN_WAKA_5 = registerSound(event.getRegistry(), "effect.pacman_waka_5");
		PACMAN_WAKA_6 = registerSound(event.getRegistry(), "effect.pacman_waka_6");
		PACMAN_DEATH = registerSound(event.getRegistry(), "effect.pacman_death");
		PACMAN_FRUIT = registerSound(event.getRegistry(), "effect.pacman_fruit");
		PACMAN_INTRO = registerSound(event.getRegistry(), "theme.pacman");
		PACMAN_GHOST = registerSound(event.getRegistry(), "effect.pacman_ghost");
		PACMAN_SIREN = registerSound(event.getRegistry(), "theme.pacman_siren");
		PACMAN_FRIGHT = registerSound(event.getRegistry(), "theme.pacman_fright");
		PACMAN_LIFE = registerSound(event.getRegistry(), "effect.pacman_life");
	}
	
	
	private static SoundEvent registerSound (IForgeRegistry<SoundEvent> registry, String name) {
		final ResourceLocation sound = new ResourceLocation(Reference.MODID, name);
		SoundEvent soundEvent = new SoundEvent(sound).setRegistryName(sound);
		registry.register(new SoundEvent(sound).setRegistryName(sound));
		return soundEvent;
	}
}
