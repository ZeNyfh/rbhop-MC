package org.bhop;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3d;
import org.slf4j.Logger;

import java.awt.event.KeyEvent;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Bhop.MODID)
public class Bhop {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "bhop";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "bhop" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "bhop" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "bhop:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "bhop:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Directly reference a log4j logger.
    public static final Minecraft mc = Minecraft.getInstance();
    private static final double GAIN_VAR = 0.11;
    private static final double TICK_RATE = 0.05; // 1/20d

    public static List<Player> players;
    public static int rtvCount = 0;

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    public Bhop() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }


    private static Vec3 horizontalMotion = Vec3.ZERO;
    private static double verticalMotion = 0;
    public static Vec3 motion = new Vec3(horizontalMotion.x, verticalMotion, horizontalMotion.z);
    public static boolean physEnabled = true;
    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        try {
            LocalPlayer player = mc.player;
            assert player != null;
            if (KeyEvent.KEY_PRESSED == 75) { // K, toggle key
                physEnabled = !physEnabled;
                assert mc.player != null;
                mc.player.displayClientMessage(Component.literal("bhop physics are now: " + physEnabled), true);
            }
            if (mc.player.input.jumping) {
                if (player.onGround()) {
                    horizontalMotion = new Vec3(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                    verticalMotion = -0.5;
                }
            }
        } catch (Exception ignored){}
    }


    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event){
//        players.remove(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
//      players.add(event.getPlayer());
        event.getEntity().displayClientMessage(Component.literal("Thank you for using the bhop mod based on the Roblox bhop physics!\nyou can use the \"K\" key on your keyboard to disable the physics at any time."),false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        try {
            LocalPlayer player = mc.player;
            if (!physEnabled) {
                assert player != null;
                if (player.input.jumping) {
                    player.jumpFromGround();
                }
                return;
            }
            assert player != null;
            player.setSprinting(false);
            motion = player.getDeltaMovement();
            airAccelerate(player);
        } catch (Exception ignored){}
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
    private static final double pi = Math.PI;
    private static final double tau = pi*2;
    private static Vec3 nextAirMotion = Vec3.ZERO;
    public static void airAccelerate(LocalPlayer player) {
        // this will update for every tick in the onTick() event
        if (player.onGround() && player.input.jumping) {
            nextAirMotion = Vec3.ZERO;
            return;
        }
        if (player.horizontalCollision) {
            double frictionFactor = 0.8; // adjust to whatever value seems right
            Vec3 frictionForce = player.getDeltaMovement().multiply(new Vec3(-frictionFactor, 1, -frictionFactor));
            nextAirMotion = nextAirMotion.add(frictionForce);
        }

        if (nextAirMotion != Vec3.ZERO) {
            player.setDeltaMovement(nextAirMotion.x, motion.y, nextAirMotion.z);
            motion = player.getDeltaMovement();
        }
        double units = Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2));
        if (player.onGround()) {
            player.jumpFromGround();
        }
        motion = player.getDeltaMovement();
        double yaw = Math.toRadians(player.getRotationVector().x); // this COULD be wrong
        double ycos = Math.cos(yaw);
        double ysin = -Math.sin(yaw);
        //double optimalScore = clamp(Math.abs((yaw - Math.toRadians(player.prevRotationYawHead) + pi) % tau - pi) / Math.atan2(GAIN_VAR, units), 0, 2);
        // optimalScore = 1-Math.abs(1-optimalScore);
        // This clamps it to 0 -> 1 instead of 0 -> 2 but impossible to know if over or under strafing
        int D = player.input.right ? 1 : 0;
        int A = player.input.left ? 1 : 0;
        int W = player.input.up ? 1 : 0;
        int S = player.input.down ? 1 : 0;
        int DmA = A-D;
        int SmW = W-S;
        units = units * 50.0;
        //optimalScore = optimalScore * 100.0;
        player.displayClientMessage(Component.literal("Units: " + units + ""), true); // change to be GUIs, can be toggled with client side commands too

        Vec3 KeyAngleData = Vec3.ZERO;
        if (DmA != 0 || SmW != 0) {
            KeyAngleData = new Vec3(DmA * ycos + SmW * ysin, 0, SmW * ycos - DmA * ysin).normalize();
        }
        float PlayerSpeed = player.getSpeed();
        double DotProduct = PlayerSpeed * (KeyAngleData.x + KeyAngleData.z);
        if (DotProduct < GAIN_VAR) {
            PlayerSpeed += (float) (KeyAngleData.x + KeyAngleData.z) * (GAIN_VAR - DotProduct);
        }
        player.setSpeed(PlayerSpeed);
        nextAirMotion = player.getDeltaMovement();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
