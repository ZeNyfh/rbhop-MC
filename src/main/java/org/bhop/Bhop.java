package org.bhop;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.bhop.blocks.BhopBlocks;
import org.bhop.items.BhopItems;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;


import static org.bhop.items.BhopCreativeModeTabs.CREATIVE_MODE_TABS;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Bhop.MODID)
public class Bhop {
    public static final String MODID = "bhop";
    public static Minecraft mc = null;
    public static Lazy<KeyMapping> TOGGLEKEY = null;
    public static Lazy<KeyMapping> RESTARTKEY = null;
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final double GAIN_VAR = 0.1;
    private static final double pi = Math.PI;
    private static final double tau = 2 * Math.PI;
    public static Vec3 motion = Vec3.ZERO;
    public static boolean physEnabled = true;
    private static Vec3 nextAirMotion = Vec3.ZERO;
    private static double prevRotationYawHead = 0;
    public Bhop() {
        // server and client side
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BhopItems.register(modEventBus);
        BhopBlocks.register(modEventBus);

        // server side
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            MinecraftForge.EVENT_BUS.addListener(BhopServer::onServerTick);
            MinecraftForge.EVENT_BUS.addListener(BhopServer::onServerSetup);
        });

        // client side
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
            mc = Minecraft.getInstance();
            TOGGLEKEY = Lazy.of(() -> new KeyMapping("key.bhop.toggle_bhop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.bhop.bhop_binds"));
            RESTARTKEY = Lazy.of(() -> new KeyMapping("key.bhop.restart", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.bhop.bhop_binds"));
            modEventBus.addListener(this::commonSetup);
            CREATIVE_MODE_TABS.register(modEventBus);
            modEventBus.addListener(this::registerBindings);
            MinecraftForge.EVENT_BUS.register(this); // This line should only be present on the client side
        });
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static void airAccelerate(LocalPlayer player) {
        // this will update for every tick in the onTick() event
        final double frictionFactor = 0.8;

        if (player.horizontalCollision || player.minorHorizontalCollision) {
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
        optimalScore = 1 - Math.abs(1 - optimalScore);
        // This clamps it to 0 -> 1 -> 0 instead of 0 -> 2 but impossible to know if over or under strafing
        int D = player.input.right ? 1 : 0;
        int A = player.input.left ? 1 : 0;
        int W = player.input.up ? 1 : 0;
        int S = player.input.down ? 1 : 0;
        int DmA = A - D;
        int SmW = W - S;
        String stringUnits = String.format("%.2f", units * 50.0);
        optimalScore = optimalScore * 100.0;
        player.displayClientMessage(generateScoreMessage((int) optimalScore, "Units: " + stringUnits), true); // change to be GUIs, can be toggled with client side commands too
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
    @SubscribeEvent
    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLEKEY.get());
        event.register(RESTARTKEY.get());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onClientPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        LOGGER.info("test");
        event.getEntity().displayClientMessage(Component.literal("Thank you for using the bhop mod based on the Roblox bhop physics!\nyou can use the \"" +  ((char) TOGGLEKEY.get().getKey().getValue()) + "\" key on your keyboard to disable the physics at any time."), false);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            try {
                LocalPlayer player = mc.player;
                if (player == null) return;
                if (RESTARTKEY.get().isDown()) {
                    RESTARTKEY.get().setDown(false);
                    player.kill(); // REPLACE WITH NBT TP LOGIC
                }
                if (TOGGLEKEY.get().isDown()) {
                    TOGGLEKEY.get().setDown(false);
                    physEnabled = !physEnabled;
                    mc.player.displayClientMessage(Component.literal("bhop physics are now: " + physEnabled), true);
                }
                if (!physEnabled || player.getAbilities().flying) return;
                ClientAccel(player);
            } catch (Exception e) {
                mc.player.sendSystemMessage(Component.literal(e.getLocalizedMessage()));
            }
        }
    }

    private static TextColor getScoreColor(int optimalScore) {
        int green = (int) (255 * (1 - Math.abs(optimalScore - 50) / 50.0));
        int red = (int) (255 * (Math.abs(optimalScore - 50) / 50.0));
        return TextColor.fromRgb(red << 16 | green << 8);
    }

    private static MutableComponent generateScoreMessage(int optimalScore, String units) {
        TextColor scoreColor = getScoreColor(optimalScore);
        return Component.literal(units).setStyle(Style.EMPTY.withColor(scoreColor));
    }

    private static void ClientAccel(LocalPlayer player) {
        player.setSprinting(false);
        motion = player.getDeltaMovement();
        if (player.onGround()) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 1, 0, false, true));
        }
        if ((player.input.jumping || !player.onGround()) && !player.getAbilities().flying) {
            airAccelerate(player);
        } else if (player.onGround() || player.getAbilities().flying) {
            nextAirMotion = Vec3.ZERO;
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MC-rbhop has loaded, have fun ig.");
    }

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
