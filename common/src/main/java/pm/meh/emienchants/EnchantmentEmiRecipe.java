package pm.meh.emienchants;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class EnchantmentEmiRecipe implements EmiRecipe {

    private final ResourceLocation enchantmentResourceLocation;
    private final Enchantment enchantment;
    private final List<EmiIngredient> inputs;

    public EnchantmentEmiRecipe(ResourceLocation location, Enchantment enchantment) {
        enchantmentResourceLocation = location;
        this.enchantment = enchantment;

        inputs = IntStream.range(1, enchantment.getMaxLevel() + 1).mapToObj(this::getBookForLevel).toList();
    }

    private EmiIngredient getBookForLevel(int level) {
        CompoundTag tagEnchantEntry = new CompoundTag();
        tagEnchantEntry.putString("id", enchantmentResourceLocation.toString());
        tagEnchantEntry.putShort("lvl", (short) level);

        ListTag tagStoredEnchantments = new ListTag();
        tagStoredEnchantments.add(tagEnchantEntry);

        CompoundTag tagItem = new CompoundTag();
        tagItem.put("StoredEnchantments", tagStoredEnchantments);

        ItemStack item = new ItemStack(Items.ENCHANTED_BOOK);
        item.setTag(tagItem);

        return EmiStack.of(item);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EnchantmentEmiPlugin.ENCHANTS_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return enchantmentResourceLocation;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 100;
    }

    @Override
    public int getDisplayHeight() {
        return 30;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.addText(enchantment.getFullname(1), 5, 5, 0, false);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
