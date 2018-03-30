package superhb.arcademod.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import superhb.arcademod.Reference;

@ObjectHolder(Reference.MODID)
public class ArcadeSounds {
	@ObjectHolder("effect.insert")
	public static final SoundEvent INSERT_COIN = null;
	
	@ObjectHolder("theme.tetris")
	public static final SoundEvent TETROMINOES = null;
	
	@ObjectHolder("effect.pacman_waka_1")
	public static final SoundEvent PACMAN_WAKA_1 = null;
	
	@ObjectHolder("effect.pacman_waka_2")
	public static final SoundEvent PACMAN_WAKA_2 = null;
	
	@ObjectHolder("effect.pacman_waka_3")
	public static final SoundEvent PACMAN_WAKA_3 = null;
	
	@ObjectHolder("effect.pacman_waka_4")
	public static final SoundEvent PACMAN_WAKA_4 = null;
	
	@ObjectHolder("effect.pacman_waka_5")
	public static final SoundEvent PACMAN_WAKA_5 = null;
	
	@ObjectHolder("effect.pacman_waka_6")
	public static final SoundEvent PACMAN_WAKA_6 = null;
	
	@ObjectHolder("effect.pacman_death")
	public static final SoundEvent PACMAN_DEATH = null;
	
	@ObjectHolder("effect.pacman_fruit")
	public static final SoundEvent PACMAN_FRUIT = null;
	
	@ObjectHolder("effect.pacman")
	public static final SoundEvent PACMAN_INTRO = null;
	
	@ObjectHolder("effect.pacman_ghost")
	public static final SoundEvent PACMAN_GHOST = null;
	
	@ObjectHolder("effect.pacman_siren")
	public static final SoundEvent PACMAN_SIREN = null;
	
	@ObjectHolder("effect.pacman_fright")
	public static final SoundEvent PACMAN_FRIGHT = null;
	
	@ObjectHolder("effect.pacman_life")
	public static final SoundEvent PACMAN_LIFE = null;
	
	@EventBusSubscriber
	public static class RegistrationHandler {
		@SubscribeEvent
		public static void registerSound (RegistryEvent.Register<SoundEvent> event) {
			final SoundEvent[] sounds = {
					createSound("effect.insert"),
					createSound("theme.tetris"),
					createSound("effect.pacman_waka_1"),
					createSound("effect.pacman_waka_2"),
					createSound("effect.pacman_waka_3"),
					createSound("effect.pacman_waka_4"),
					createSound("effect.pacman_waka_5"),
					createSound("effect.pacman_waka_6"),
					createSound("effect.pacman_death"),
					createSound("effect.pacman_fruit"),
					createSound("theme.pacman"),
					createSound("effect.pacman_ghost"),
					createSound("theme.pacman_siren"),
					createSound("theme.pacman_fright"),
					createSound("effect.pacman_life")
			};
			
			event.getRegistry().registerAll(sounds);
		}
		
		private static SoundEvent createSound (String name) {
			final ResourceLocation sound = new ResourceLocation(Reference.MODID, name);
			SoundEvent soundEvent = new SoundEvent(sound).setRegistryName(sound);
			return soundEvent;
		}
	}
}
