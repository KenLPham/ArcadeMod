package superhb.arcademod.client.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemMeshDefinition {
    @SideOnly(Side.CLIENT)
    ItemMeshDefinition getMeshDefinition ();
}
