package superhb.arcademod.client.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ItemPlushie implements IPerspectiveAwareModel {
    private IBakedModel model;
    private int mob;

    public ItemPlushie (IBakedModel model, int mob) {
        this.model = model;
        this.mob = mob;
    }

    @Override
    public List<BakedQuad> getQuads (@Nullable IBlockState state, @Nullable EnumFacing facing, long rand) {
        if (facing != null) return model.getQuads(state, facing, rand);

        //List<BakedQuad> combined = new ArrayList(model.getQuads(state, facing, rand));
        //combined.addAll(getPlushies)
        return null;
    }

    @Override
    public boolean isAmbientOcclusion () {
        return model.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d () {
        return model.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer () {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture () {
        return model.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms () {
        return model.getItemCameraTransforms();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective (ItemCameraTransforms.TransformType transformType) {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        throw new UnsupportedOperationException("ItemPlushie does not have an override list");
    }
}
