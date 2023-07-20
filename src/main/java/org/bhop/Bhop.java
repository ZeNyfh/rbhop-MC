package org.bhop;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
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
import org.bhop.blocks.BhopBlocks;
import org.bhop.items.BhopCreativeModeTabs;
import org.bhop.items.BhopItems;
import org.joml.Vector3d;
import org.slf4j.Logger;

import java.awt.event.KeyEvent;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Bhop.MODID)
public class Bhop {
    public static final String MODID = "bhop";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Minecraft mc = Minecraft.getInstance();
    private static final double GAIN_VAR = 0.1;
    private static final double TICK_RATE = 0.05; // 1/20d

    public static List<Player> players;
    public static int rtvCount = 0;

    public Bhop() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BhopCreativeModeTabs.register(modEventBus);

        BhopBlocks.register(modEventBus);
        BhopItems.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MC-rbhop has loaded, have fun ig.");
    }


    public static Vec3 motion = Vec3.ZERO;
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
            if (player == null) {
                return;
            }

            player.setSprinting(false);
            motion = player.getDeltaMovement();
            if (player.input.jumping || !player.onGround()) {
                airAccelerate(player);
            } else if (player.onGround()) {
                nextAirMotion = Vec3.ZERO;
            }
        } catch (Exception ignored) {
        }
    }




    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
    private static final double pi = Math.PI;
    private static final double tau = 2*Math.PI;
    private static Vec3 nextAirMotion = Vec3.ZERO;
    private static double prevRotationYawHead = 0;
    public static void airAccelerate(LocalPlayer player) {
        // this will update for every tick in the onTick() event
        final double frictionFactor = 0.8;
        if (player.horizontalCollision || player.minorHorizontalCollision){
            // Apply the friction force to reduce the movement
            Vec3 deltaM = player.getDeltaMovement();
            nextAirMotion = new Vec3(deltaM.x * frictionFactor, deltaM.y, deltaM.z * frictionFactor);
        }
        if (nextAirMotion != Vec3.ZERO) {
            player.setDeltaMovement(nextAirMotion.x, motion.y, nextAirMotion.z);
        }
        if (player.onGround()) {
            player.jumpFromGround();
        }
        motion = player.getDeltaMovement();
        double units = Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2));

        double yaw = Math.atan2(-player.getLookAngle().x, player.getLookAngle().z);

        double ycos = Math.cos(yaw);
        double ysin = -Math.sin(yaw);
        double optimalScore = clamp(Math.abs((yaw - prevRotationYawHead + pi) % tau - pi) / Math.atan2(GAIN_VAR, units), 0, 2);
        optimalScore = 1-Math.abs(1-optimalScore);
        // This clamps it to 0 -> 1 -> 0 instead of 0 -> 2 but impossible to know if over or under strafing
        int D = player.input.right ? 1 : 0;
        int A = player.input.left ? 1 : 0;
        int W = player.input.up ? 1 : 0;
        int S = player.input.down ? 1 : 0;
        int DmA = A-D;
        int SmW = W-S;
        String stringUnits = String.format("%.2f", units * 50.0);
        optimalScore = optimalScore * 100.0;
        player.displayClientMessage(Component.literal("Units: " + stringUnits + " | " + "Gauge score: " + optimalScore), true); // change to be GUIs, can be toggled with client side commands too
        Vec3 KeyAngleData = Vec3.ZERO;
        if (DmA != 0 || SmW != 0) {
            KeyAngleData = new Vec3(DmA * ycos + SmW * ysin, 0, SmW * ycos - DmA * ysin).normalize();
        }

        Vec3 PlayerSpeed = player.getDeltaMovement();
        double DotProduct = PlayerSpeed.dot(KeyAngleData);
        if (DotProduct < GAIN_VAR) {
            PlayerSpeed = PlayerSpeed.add(KeyAngleData.scale(GAIN_VAR - DotProduct));
        }
        player.setDeltaMovement(PlayerSpeed);
        nextAirMotion = player.getDeltaMovement();
        prevRotationYawHead = yaw;
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
