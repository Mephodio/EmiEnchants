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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class EnchantmentEmiRecipe implements EmiRecipe {

    private static final ResourceLocation ICON_INFO = new ResourceLocation(Common.MOD_ID, "textures/gui/icon_info.png");
    private static final ResourceLocation ICON_ENCH_TABLE = new ResourceLocation(Common.MOD_ID, "textures/gui/icon_ench_table.png");
    private static final ResourceLocation ICON_VILLAGER = new ResourceLocation(Common.MOD_ID, "textures/gui/icon_villager.png");
    private static final ResourceLocation ICON_TREASURE = new ResourceLocation(Common.MOD_ID, "textures/gui/icon_treasure.png");
    private static final ResourceLocation ICON_CURSE = new ResourceLocation(Common.MOD_ID, "textures/gui/icon_curse.png");

    private static final int LAYOUT_X_OFFSET = 22;
    private static final int LAYOUT_X_OFFSET_SMALL = 2;
    private static final int LAYOUT_Y_OFFSET = 2;
    private static final int LAYOUT_ROW_HEIGHT = 10;
    private static final int LAYOUT_TEXT_COLOR = 0x333333;
    private static final boolean LAYOUT_TEXT_SHADOW = false;
    private final int LAYOUT_DESCRIPTION_OFFSET;

    private final ResourceLocation enchantmentResourceLocation;
    private final Enchantment enchantment;
    private final List<EmiStack> inputs;
    private final EmiIngredient canApplyTo;
    private final EmiIngredient incompatibleSlot;
    private final List<IconBoolStatEntry> iconStats;
    private final List<FormattedCharSequence> description;

    public EnchantmentEmiRecipe(ResourceLocation location, Enchantment enchantment) {
        enchantmentResourceLocation = location;
        this.enchantment = enchantment;

        inputs = IntStream.range(1, enchantment.getMaxLevel() + 1).mapToObj(this::getBookForLevel).toList();
        canApplyTo = EmiIngredient.of(BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new).filter(enchantment::canEnchant).map(EmiStack::of).toList());
        incompatibleSlot = EmiIngredient.of(BuiltInRegistries.ENCHANTMENT.entrySet().stream()
                .filter(e -> !e.getValue().equals(enchantment) && !enchantment.isCompatibleWith(e.getValue()))
                .map(e -> getBookForLevel(e.getKey().location(), e.getValue().getMaxLevel())).toList());
        iconStats = List.of(
                new IconBoolStatEntry(ICON_ENCH_TABLE, "discoverable", enchantment.isDiscoverable(), true),
                new IconBoolStatEntry(ICON_VILLAGER, "tradeable", enchantment.isTradeable(), true),
                new IconBoolStatEntry(ICON_TREASURE, "treasure", enchantment.isTreasureOnly(), false),
                new IconBoolStatEntry(ICON_CURSE, "curse", enchantment.isCurse(), false)
        );

        if (incompatibleSlot.isEmpty()) {
            LAYOUT_DESCRIPTION_OFFSET = LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * 5 + LAYOUT_X_OFFSET_SMALL;
        } else {
            LAYOUT_DESCRIPTION_OFFSET = LAYOUT_Y_OFFSET + 60;
        }

        String descriptionId = enchantment.getDescriptionId() + ".desc";
        Component descriptionTranslatable = Component.translatable(descriptionId).withStyle(ChatFormatting.ITALIC);
        if (descriptionTranslatable.getString().equals(descriptionId)) {
            description = List.of();
        } else {
            description = Minecraft.getInstance().font.split(descriptionTranslatable, getDisplayWidth() - LAYOUT_X_OFFSET_SMALL * 2);
        }
    }

    private EmiStack getBookForLevel(int level) {
        return getBookForLevel(enchantmentResourceLocation, level);
    }

    private static EmiStack getBookForLevel(ResourceLocation location, int level) {
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
        return List.of(canApplyTo, EmiIngredient.of(inputs));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return inputs;
    }

    @Override
    public int getDisplayWidth() {
        return 160;
    }

    @Override
    public int getDisplayHeight() {
        if (description.isEmpty()) {
            if (incompatibleSlot.isEmpty()) {
                return LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * 5;
            } else {
                return LAYOUT_Y_OFFSET + 60;
            }
        } else {
            return LAYOUT_DESCRIPTION_OFFSET + LAYOUT_ROW_HEIGHT * description.size();
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int row = 0;

        widgetHolder.addSlot(getBookForLevel(enchantment.getMaxLevel()), LAYOUT_X_OFFSET_SMALL, LAYOUT_Y_OFFSET);
        widgetHolder.add(new CustomEmiSlotWidget(canApplyTo, LAYOUT_X_OFFSET_SMALL, LAYOUT_Y_OFFSET + 20,
                false, Component.translatable("emienchants.property.applicable_to")));

        if (!incompatibleSlot.isEmpty()) {
            widgetHolder.add(new CustomEmiSlotWidget(incompatibleSlot, LAYOUT_X_OFFSET_SMALL, LAYOUT_Y_OFFSET + 40,
                    true, Component.translatable("emienchants.property.conflicts")));
        }

        MutableComponent title = Component.translatable(enchantment.getDescriptionId());
        if (enchantment.getMaxLevel() > 1) {
            title = title.append(String.format(" %d-%d", enchantment.getMinLevel(), enchantment.getMaxLevel()));
        }
        widgetHolder.addText(title, LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);

        widgetHolder.addText(Component.literal(enchantmentResourceLocation.getNamespace()).withStyle(ChatFormatting.DARK_BLUE),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);

        widgetHolder.addText(Component.translatable("emienchants.property.category", enchantment.category.name()),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);

        TextWidget rarityWidget = widgetHolder.addText(Component.translatable("emienchants.property.rarity",
                enchantment.getRarity().name(), enchantment.getRarity().getWeight()),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);
        widgetHolder.addTexture(ICON_INFO, LAYOUT_X_OFFSET + rarityWidget.getBounds().width() + 1,
                LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row, 7, 8, 7, 8, 7, 8, 7, 8);
        widgetHolder.addTooltipText(IntStream.range(1, enchantment.getMaxLevel() + 1).mapToObj(lvl ->
                        (Component) Component.translatable("emienchants.property.cost", lvl, enchantment.getMinCost(lvl), enchantment.getMaxCost(lvl))).toList(),
                LAYOUT_X_OFFSET, LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row++, rarityWidget.getBounds().width() + 9, 8);

        int iconSectionWidth = (getDisplayWidth() - LAYOUT_X_OFFSET - LAYOUT_X_OFFSET_SMALL) / iconStats.size();
        int iconXOffset = LAYOUT_X_OFFSET;
        int iconYOffset = LAYOUT_Y_OFFSET + LAYOUT_ROW_HEIGHT * row;
        for (IconBoolStatEntry stat : iconStats) {
            widgetHolder.addTexture(stat.icon, iconXOffset, iconYOffset, 8, 8, 8, 8, 8, 8, 8, 8);
            TextWidget statWidget = widgetHolder.addText(stat.getValueLabel(), iconXOffset + 10, iconYOffset, LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);
            widgetHolder.addTooltipText(List.of(Component.translatable(String.format("emienchants.property.%s.%s", stat.label, stat.value))),
                    iconXOffset, iconYOffset, statWidget.getBounds().width() + 10, 8);
            iconXOffset += iconSectionWidth;
        }

        row = 0;
        for (FormattedCharSequence line : description) {
            widgetHolder.addText(line, LAYOUT_X_OFFSET_SMALL, LAYOUT_DESCRIPTION_OFFSET + LAYOUT_ROW_HEIGHT * row++,
                    LAYOUT_TEXT_COLOR, LAYOUT_TEXT_SHADOW);
        }
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    private static record IconBoolStatEntry(ResourceLocation icon, String label, boolean value, boolean isPositive) {
        public Component getValueLabel() {
            return Component.translatable("emienchants.property.value." + value).withStyle(
                    Style.EMPTY.withColor(value ^ isPositive ? 0xAA0000 : 0x008800));
        }
    }
}
