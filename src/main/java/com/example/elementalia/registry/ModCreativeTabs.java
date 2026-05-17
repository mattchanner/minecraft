package com.example.elementalia.registry;

import com.example.elementalia.Elementalia;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Elementalia.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ELEMENTALIA_TAB =
            CREATIVE_MODE_TABS.register("elementalia_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.elementalia"))
                    .icon(() -> ModItems.FIRE_BOOK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FIRE_BOOK.get());
                        output.accept(ModItems.ICE_BOOK.get());
                        output.accept(ModItems.EARTH_BOOK.get());
                        output.accept(ModItems.WIND_BOOK.get());
                        output.accept(ModBlocks.TOME_RITUAL_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
