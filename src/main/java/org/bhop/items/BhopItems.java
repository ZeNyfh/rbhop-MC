package org.bhop.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bhop.Bhop;

public class BhopItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Bhop.MODID);

    public static final RegistryObject<Item> KILLTRIGGER = ITEMS.register("killtrigger", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPAWNTRIGGER = ITEMS.register("spawntrigger", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHECKPOINTTRIGGER = ITEMS.register("checkpointtrigger", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENDTRIGGER = ITEMS.register("endtrigger", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TPTRIGGER = ITEMS.register("tptrigger", () -> new Item(new Item.Properties()));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
