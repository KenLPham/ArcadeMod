package superhb.arcademod.content;

import superhb.arcademod.Arcade;
import superhb.arcademod.content.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class ArcadeBlocks {
    public static final Block coinPusher = new BlockPusher(Material.ROCK).setRegistryName("coin_pusher");//.setCreativeTab(Arcade.tab);
    public static final Block arcadeMachine = new BlockArcade(Material.ROCK).setRegistryName("arcade_machine").setCreativeTab(Arcade.tab);
    public static final Block plushie = new BlockPlushie(Material.CLOTH).setRegistryName("plushie");//.setCreativeTab(Arcade.tab);
    public static final Block invisible = new BlockInvisible(Material.ROCK).setRegistryName("invisible");
}
