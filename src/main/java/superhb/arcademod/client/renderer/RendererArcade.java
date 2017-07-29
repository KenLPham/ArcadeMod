package superhb.arcademod.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.client.models.ModelPacMan;
import superhb.arcademod.client.tileentity.TileEntityTestArcade;

// TODO: Change texture based on variant
public class RendererArcade extends TileEntitySpecialRenderer {
    private final ModelPacMan model;
    private static final ResourceLocation texture = new ResourceLocation(Reference.MODID + ":textures/blocks/arcade.png");

    public RendererArcade () {
        model = new ModelPacMan();
    }

    @Override
    public void renderTileEntityAt (TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tile instanceof TileEntityTestArcade) Arcade.logger.info("TileEntity is TileEntityTestArcade");

        GL11.glPushMatrix();
        GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
        bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glRotatef(180, 0, 0, 1);
        // TODO: Base rotation on facing
        int rot = 0;
        /* TODO: Store Facing into TileEntity
        switch (te.getBlockMetadata() % 4) {
                        case 0:
                            rotation = 270;
                            break;
                        case 1:
                            rotation = 0;
                            break;
                        case 2:
                            rotation = 90;
                            break;
                        case 3:
                            rotation = 180;
                            break;
                    }
         */
        GL11.glRotatef(rot, 0, 1, 0);
        model.render(null, 0, 0, -0.1F, 0, 0, 0.0625F);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
