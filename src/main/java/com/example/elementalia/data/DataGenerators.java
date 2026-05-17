package com.example.elementalia.data;

import com.example.elementalia.Elementalia;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

/**
 * Wires up all data providers so that {@code ./gradlew runClientData}
 * regenerates recipes, advancements, and language files into
 * {@code src/generated/resources/}.
 *
 * NeoGradle 7.1 uses the {@code clientData} run type (fires {@link GatherDataEvent.Client})
 * for all data generation — there is no separate {@code data} run type.
 */
@EventBusSubscriber(modid = Elementalia.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        var output         = event.getGenerator().getPackOutput();
        var lookupProvider = event.getLookupProvider();

        // Server-side data: recipes
        event.addProvider(new ModRecipeProvider.Runner(output, lookupProvider));

        // Server-side data: advancements
        event.addProvider(new AdvancementProvider(output, lookupProvider,
                List.of(new ModAdvancementProvider())));

        // Client-side data: language
        event.addProvider(new ModLanguageProvider(output, Elementalia.MODID, "en_us"));

        // Client-side data: items/*.json pointers (1.21.4 requirement — each item
        // needs an assets/<ns>/items/<id>.json telling the renderer which model to use)
        event.addProvider(new ModModelProvider(output));
    }
}
