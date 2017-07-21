package superhb.arcademod.util;

public enum EnumType {
    MACHINE("machine"),
    PLUSHIE("plushie");

    public String folderName;

    private EnumType (String folderName) {
        this.folderName = folderName;
    }

    public static EnumType get (String folderName) {
        for (EnumType type : values()) {
            if (type.folderName.equals(folderName)) return type;
        }
        return null;
    }
}
