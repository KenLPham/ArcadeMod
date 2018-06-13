package superhb.arcademod.util.prizebox;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

public class PrizeHelper {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static ItemStack getItemStack (JsonObject object) {
		String resourceName = JsonUtils.getString(object, "item");
		Item item = Item.getByNameOrId(resourceName);
		
		if (item == null) throw new JsonSyntaxException(String.format("Unknown item '%s'", resourceName));
		if (item.getHasSubtypes() && !object.has("data")) throw new JsonParseException(String.format("Missing metadata for item '%s'", resourceName));
		
		if (object.has("nbt")) {
			try {
				JsonElement element = object.get("nbt");
				NBTTagCompound nbt;
				
				if (element.isJsonObject()) nbt = JsonToNBT.getTagFromJson(GSON.toJson(element));
				else nbt = JsonToNBT.getTagFromJson(element.getAsString());
				
				NBTTagCompound tmp = new NBTTagCompound();
				tmp.setTag("tag", nbt);
				tmp.setString("id", resourceName);
				tmp.setInteger("Count", 1);
				tmp.setInteger("Damage", JsonUtils.getInt(object, "data", 0));
				
				return new ItemStack(tmp);
			} catch (NBTException e) {
				throw new JsonSyntaxException("Invalid NBT Entry: " + e.toString());
			}
		}
		return new ItemStack(item, 1, JsonUtils.getInt(object, "data", 0));
	}
}
