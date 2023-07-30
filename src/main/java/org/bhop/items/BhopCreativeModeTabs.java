package org.bhop.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.bhop.Bhop;
import org.bhop.blocks.BhopBlocks;

public class BhopCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Bhop.MODID);

    public static final RegistryObject<CreativeModeTab> BHOP_TAB = CREATIVE_MODE_TABS.register("bhop",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(BhopBlocks.CHECKPOINT_TRIGGER.get())).title(Component.translatable("creativetab.bhop_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(BhopBlocks.KILL_TRIGGER.get());
                        pOutput.accept(BhopBlocks.SPAWN_TRIGGER.get());
                        pOutput.accept(BhopBlocks.CHECKPOINT_TRIGGER.get());
                        pOutput.accept(BhopBlocks.END_TRIGGER.get());
                        pOutput.accept(BhopBlocks.TP_TRIGGER.get());
                        pOutput.accept(BhopItems.TRIGGERWRENCH.get());
                    })
                    .build());
}
