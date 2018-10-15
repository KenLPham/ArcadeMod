package superhb.arcademod;

import net.minecraft.util.ResourceLocation;

public class Reference {
    public static final String MODID = "arcademod";
    public static final String NAME = "Arcade Mod";
    public static final String VERSION = "2.1.5";
    public static final String CLIENT_PROXY = "superhb.arcademod.proxy.ClientProxy";
    public static final String SERVER_PROXY = "superhb.arcademod.proxy.CommonProxy";
    public static final String DESCRIPTION = "Adds various arcade games to Minecraft";
    public static final String AUTHOR = "SuperHB";
    public static final String CREDIT = "";
    public static final String URL = "https://minecraft.curseforge.com/projects/arcade-mod/files";
    public static final String LOGO = "logo.png";
    public static final String UPDATE_URL = "https://raw.githubusercontent.com/KenLPham/ArcadeMod/update/update.json";

    public static ResourceLocation createResource (String key) {
        return new ResourceLocation(MODID, key);
    }
}
