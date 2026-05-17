package com.example.elementalia.data;

import com.example.elementalia.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // Fire Book — shaped 3×3:
        //   B R B
        //   L K L
        //   B R B
        // B = Blaze Powder, R = Blaze Rod, L = Leather, K = Book
        this.shaped(RecipeCategory.MISC, ModItems.FIRE_BOOK.get())
                .define('B', Items.BLAZE_POWDER)
                .define('R', Items.BLAZE_ROD)
                .define('L', Items.LEATHER)
                .define('K', Items.BOOK)
                .pattern("BRB")
                .pattern("LKL")
                .pattern("BRB")
                .unlockedBy(getHasName(Items.BLAZE_ROD), this.has(Items.BLAZE_ROD))
                .save(this.output);
    }

    /** DataProvider.Runner wrapper required by RecipeProvider in 1.21.4. */
    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries,
                                                      RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Elementalia Recipes";
        }
    }
}
