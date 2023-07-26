package org.bhop.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bhop.Bhop;
import org.bhop.items.BhopItems;

import java.util.function.Supplier;

public class BhopBlocks {
    public final static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Bhop.MODID);

    public static final BlockBehaviour.Properties TRIGGER_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.GLASS)
            .strength(-1F, -1F) // Adjust strength values as needed
            .noCollission()
            .noOcclusion()
            .isViewBlocking((blockState, world, pos) -> false)
            .isSuffocating((blockState, world, pos) -> false);
    public static final RegistryObject<Block> KILL_TRIGGER = registerBlock("kill_trigger",
            () -> new Block(TRIGGER_PROPERTIES));
    public static final RegistryObject<Block> SPAWN_TRIGGER = registerBlock("spawn_trigger",
            () -> new Block(TRIGGER_PROPERTIES));
    public static final RegistryObject<Block> TP_TRIGGER = registerBlock("end_trigger",
            () -> new Block(TRIGGER_PROPERTIES));
    public static final RegistryObject<Block> END_TRIGGER = registerBlock("tp_trigger",
            () -> new Block(TRIGGER_PROPERTIES));
    public static final RegistryObject<Block> CHECKPOINT_TRIGGER = registerBlock("checkpoint_trigger",
            () -> new Block(TRIGGER_PROPERTIES));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return BhopItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
