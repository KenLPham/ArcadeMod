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
	
	@ObjectHolder("theme.pacman")
	public static final SoundEvent PACMAN_INTRO = null;
	
	@ObjectHolder("effect.pacman_ghost")
	public static final SoundEvent PACMAN_GHOST = null;
	
	@ObjectHolder("theme.pacman_siren")
	public static final SoundEvent PACMAN_SIREN = null;
	
	@ObjectHolder("theme.pacman_fright")
	public static final SoundEvent PACMAN_FRIGHT = null;
	
	@ObjectHolder("effect.pacman_life")
	public static final SoundEvent PACMAN_LIFE = null;

	@ObjectHolder("theme.pacman_eat")
	public static final SoundEvent PACMAN_EAT = null;

	@ObjectHolder("theme.spaceinvaders")
	public static final SoundEvent SPACEINVADERS = null;
	
	@ObjectHolder("effect.spaceinvaders_shoot")
	public static final SoundEvent SPACEINVADERS_SHOOT = null;
	
	@ObjectHolder("effect.spaceinvaders_explode")
	public static final SoundEvent SPACEINVADERS_EXPLODE = null;
	
	@ObjectHolder("effect.spaceinvaders_destroyed")
	public static final SoundEvent SPACEINVADERS_DESTROYED = null;

	@ObjectHolder("effect.pong_hit")
	public static final SoundEvent PONG_HIT = null;

	@ObjectHolder("effect.pong_miss")
	public static final SoundEvent PONG_MISS = null;

	@ObjectHolder("effect.pong_wall")
	public static final SoundEvent PONG_WALL = null;
	
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
					createSound("effect.pacman_life"),
					createSound("theme.pacman_eat"),
					createSound("theme.spaceinvaders"),
					createSound("effect.spaceinvaders_shoot"),
					createSound("effect.spaceinvaders_explode"),
					createSound("effect.spaceinvaders_destroyed"),
					createSound("effect.pong_hit"),
					createSound("effect.pong_miss"),
					createSound("effect.pong_wall")
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
