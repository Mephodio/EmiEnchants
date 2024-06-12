package pm.meh.emienchants;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

public class Util {
    public static EmiStack getBookStackForLevel(ResourceLocation location, int level) {
        CompoundTag tagEnchantEntry = new CompoundTag();
        tagEnchantEntry.putString("id", location.toString());
        tagEnchantEntry.putShort("lvl", (short) level);

        ListTag tagStoredEnchantments = new ListTag();
        tagStoredEnchantments.add(tagEnchantEntry);

        CompoundTag tagItem = new CompoundTag();
        tagItem.put("StoredEnchantments", tagStoredEnchantments);

        ItemStack item = new ItemStack(Items.ENCHANTED_BOOK);
        item.setTag(tagItem);

        return EmiStack.of(item);
    }

    public static MutableComponent getLocalizedTextByCode(String code, String localizationIdTemplate) {
        String localizationId = String.format(localizationIdTemplate, code.toLowerCase(Locale.ROOT));
        MutableComponent translatable = Component.translatable(localizationId);
        if (translatable.getString().equals(localizationId)) {
            return Component.literal(code);
        } else {
            return translatable;
        }
    }
}
