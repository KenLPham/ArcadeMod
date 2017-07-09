package superhb.arcademod.content.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

public class BlockPlushie extends Block implements IBlockVariant {
    public BlockPlushie(Material material) {
        super(material);
        setSoundType(SoundType.CLOTH);
    }

    @Override
    public String getVariantName (ItemStack stack) {
        return "";
    }
}
