package com.example.elementalia.data;

import com.example.elementalia.registry.ModBlocks;
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

        // Ice Book — packed ice corners, powder snow bucket on the centre row,
        // leather binding, book core.
        this.shaped(RecipeCategory.MISC, ModItems.ICE_BOOK.get())
                .define('P', Items.PACKED_ICE)
                .define('S', Items.POWDER_SNOW_BUCKET)
                .define('L', Items.LEATHER)
                .define('K', Items.BOOK)
                .pattern("PSP")
                .pattern("LKL")
                .pattern("PSP")
                .unlockedBy(getHasName(Items.PACKED_ICE), this.has(Items.PACKED_ICE))
                .save(this.output);

        // Earth Book — deepslate corners, amethyst clusters on the centre row,
        // leather binding, book core.
        this.shaped(RecipeCategory.MISC, ModItems.EARTH_BOOK.get())
                .define('D', Items.DEEPSLATE)
                .define('A', Items.AMETHYST_CLUSTER)
                .define('L', Items.LEATHER)
                .define('K', Items.BOOK)
                .pattern("DAD")
                .pattern("LKL")
                .pattern("DAD")
                .unlockedBy(getHasName(Items.AMETHYST_CLUSTER), this.has(Items.AMETHYST_CLUSTER))
                .save(this.output);

        // Wind Book — feathers in the corners, breeze rods on the centre row,
        // leather binding, book core.
        this.shaped(RecipeCategory.MISC, ModItems.WIND_BOOK.get())
                .define('F', Items.FEATHER)
                .define('B', Items.BREEZE_ROD)
                .define('L', Items.LEATHER)
                .define('K', Items.BOOK)
                .pattern("FBF")
                .pattern("LKL")
                .pattern("FBF")
                .unlockedBy(getHasName(Items.BREEZE_ROD), this.has(Items.BREEZE_ROD))
                .save(this.output);

        // Tome Altar — 8 obsidian surrounding a single book.
        this.shaped(RecipeCategory.MISC, ModBlocks.TOME_RITUAL_ITEM.get())
                .define('O', Items.OBSIDIAN)
                .define('K', Items.BOOK)
                .pattern("OOO")
                .pattern("OKO")
                .pattern("OOO")
                .unlockedBy(getHasName(Items.OBSIDIAN), this.has(Items.OBSIDIAN))
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
