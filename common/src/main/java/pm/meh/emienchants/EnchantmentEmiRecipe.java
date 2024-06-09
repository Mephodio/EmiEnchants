package pm.meh.emienchants;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
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
    private final EmiIngredient canApplyTo;
    private final EmiIngredient incompatibleSlot;

    public EnchantmentEmiRecipe(ResourceLocation location, Enchantment enchantment) {
        enchantmentResourceLocation = location;
        this.enchantment = enchantment;

        inputs = IntStream.range(1, enchantment.getMaxLevel() + 1).mapToObj(this::getBookForLevel).toList();
        canApplyTo = EmiIngredient.of(BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new).filter(enchantment::canEnchant).map(EmiStack::of).toList());
        incompatibleSlot = EmiIngredient.of(BuiltInRegistries.ENCHANTMENT.entrySet().stream()
                .filter(e -> !e.getValue().equals(enchantment) && !enchantment.isCompatibleWith(e.getValue()))
                .map(e -> getBookForLevel(e.getKey().location(), e.getValue().getMaxLevel())).toList());
    }

    private EmiIngredient getBookForLevel(int level) {
        return getBookForLevel(enchantmentResourceLocation, level);
    }

    private static EmiIngredient getBookForLevel(ResourceLocation location, int level) {
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
        return 160;
    }

    @Override
    public int getDisplayHeight() {
        return 140;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        final int xOffset = 22;
        final int xOffsetSmall = 2;
        final int yOffset = 2;
        final int rowHeight = 10;
        final int textColor = 0x333333;
        final boolean shadow = false;
        int row = 0;

        widgetHolder.addSlot(getBookForLevel(enchantment.getMaxLevel()), xOffsetSmall, yOffset);
        widgetHolder.addSlot(canApplyTo, xOffsetSmall, yOffset + 20);

        if (!incompatibleSlot.isEmpty()) {
            widgetHolder.addSlot(incompatibleSlot, xOffsetSmall, yOffset + 40);
        }

        widgetHolder.addText(Component.translatable(enchantment.getDescriptionId()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);
        widgetHolder.addText(Component.translatable("emienchants.property.max_level", enchantment.getMaxLevel()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);

        TextWidget rarityWidget = widgetHolder.addText(Component.translatable("emienchants.property.rarity",
                enchantment.getRarity().name(), enchantment.getRarity().getWeight()),
                xOffset, yOffset + rowHeight * row, textColor, shadow);
        widgetHolder.addTooltipText(IntStream.range(1, enchantment.getMaxLevel() + 1).mapToObj(lvl ->
                        (Component) Component.translatable("emienchants.property.cost", lvl, enchantment.getMinCost(lvl), enchantment.getMaxCost(lvl))).toList(),
                xOffset, yOffset + rowHeight * row++, rarityWidget.getBounds().width(), 8);

        widgetHolder.addText(Component.translatable("emienchants.property.category", enchantment.category.name()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);
        widgetHolder.addText(Component.translatable("emienchants.property.curse", enchantment.isCurse()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);
        widgetHolder.addText(Component.translatable("emienchants.property.discoverable", enchantment.isDiscoverable()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);
        widgetHolder.addText(Component.translatable("emienchants.property.tradeable", enchantment.isTradeable()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);
        widgetHolder.addText(Component.translatable("emienchants.property.treasure", enchantment.isTreasureOnly()),
                xOffset, yOffset + rowHeight * row++, textColor, shadow);

        String descriptionId = enchantment.getDescriptionId() + ".desc";
        Component descriptionTranslatable = Component.translatable(descriptionId).withStyle(ChatFormatting.ITALIC);

        if (!descriptionTranslatable.getString().equals(descriptionId)) {
            List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(descriptionTranslatable, getDisplayWidth() - xOffsetSmall * 2);
            for (FormattedCharSequence line : lines) {
                widgetHolder.addText(line, xOffsetSmall, yOffset + rowHeight * row++ + 4, textColor, shadow);
            }
        }
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
