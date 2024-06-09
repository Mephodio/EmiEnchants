package pm.meh.emienchants;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

@EmiEntrypoint
public class EnchantmentEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory ENCHANTS_CATEGORY = new EmiRecipeCategory(
            new ResourceLocation(Common.MOD_ID, "enchants"), EmiStack.of(Items.ENCHANTED_BOOK));

    @Override
    public void register(EmiRegistry emiRegistry) {
        emiRegistry.addCategory(ENCHANTS_CATEGORY);

        BuiltInRegistries.ENCHANTMENT.entrySet().forEach(enchantmentEntry -> {
            ResourceLocation location = enchantmentEntry.getKey().location();
            Enchantment enchantment = enchantmentEntry.getValue();

            emiRegistry.addRecipe(new EnchantmentEmiRecipe(location, enchantment));
        });
    }
}
