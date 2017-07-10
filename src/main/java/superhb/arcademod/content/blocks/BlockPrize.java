package superhb.arcademod.content.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import superhb.arcademod.Arcade;

// TODO: Property facing
public class BlockPrize extends Block {
    public BlockPrize(Material material) {
        super(material);
        setHardness(1.0F);
    }

    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            player.openGui(Arcade.instance, 2, world, (int)player.posX, (int)player.posY, (int)player.posZ);
            return true;
        }
        return false;
    }
}
