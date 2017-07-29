package superhb.arcademod.client.models;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import superhb.arcademod.Reference;
import superhb.arcademod.client.items.override.ItemPlushieOverride;

import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class ModelPlushie implements IPerspectiveAwareModel {
    private IBakedModel model;
    private ItemPlushieOverride override;

    public static final ModelResourceLocation[] resources = {
            new ModelResourceLocation(Reference.MODID + ":snake_arcade", "inventory")
    };

    public ModelPlushie (IBakedModel model) {
        this.model = model;
        override = new ItemPlushieOverride(Collections.EMPTY_LIST);
    }

    @Override
    public TextureAtlasSprite getParticleTexture () {
        return model.getParticleTexture();
    }

    @Override
    public List<BakedQuad> getQuads (IBlockState state, EnumFacing facing, long rand) {
        return model.getQuads(state, facing, rand);
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
    public ItemCameraTransforms getItemCameraTransforms () {
        return model.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides () {
        return override;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective (ItemCameraTransforms.TransformType transformType) {
        if (model instanceof IPerspectiveAwareModel) {
            Matrix4f matrix = ((IPerspectiveAwareModel)model).handlePerspective(transformType).getRight();
        } else {
            ItemCameraTransforms transforms = model.getItemCameraTransforms();
            ItemTransformVec3f vec = transforms.getTransform(transformType);
            TRSRTransformation transformation = new TRSRTransformation(vec);
            Matrix4f matrix = null;
            if (transformation != null) matrix = transformation.getMatrix();
            return Pair.of(this, matrix);
        }
        return null;
    }
}
