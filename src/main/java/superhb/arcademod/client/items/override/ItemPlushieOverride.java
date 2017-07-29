package superhb.arcademod.client.items.override;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import superhb.arcademod.client.items.ItemPlushie;

import java.util.List;

public class ItemPlushieOverride  extends ItemOverrideList {
    public ItemPlushieOverride (List<ItemOverride> list) {
        super(list);
    }

    @Override
    public IBakedModel handleItemState (IBakedModel model, ItemStack stack, World world, EntityLivingBase entity) {
        int mob = 0;
        if (stack != null) mob = stack.getTagCompound().getInteger("Mob");
        return new ItemPlushie(model, mob);
    }
}
