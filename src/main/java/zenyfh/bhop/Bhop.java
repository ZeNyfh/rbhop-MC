package zenyfh.bhop;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("bhop")
public class Bhop {

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Minecraft mc = Minecraft.getInstance();
    private static final double GAIN_VAR = 0.11;
    private static final double TICK_RATE = 0.05; // 1/20d
    private static final KeyBinding JUMP_KEY = new KeyBinding("key.jump", GLFW.GLFW_KEY_SPACE, "key.categories.movement");
    private static final KeyBinding FORWARD_KEY = new KeyBinding("key.w", GLFW.GLFW_KEY_W, "key.categories.movement");
    private static final KeyBinding BACK_KEY = new KeyBinding("key.s", GLFW.GLFW_KEY_S, "key.categories.movement");
    private static final KeyBinding LEFT_KEY = new KeyBinding("key.a", GLFW.GLFW_KEY_A, "key.categories.movement");
    private static final KeyBinding RIGHT_KEY = new KeyBinding("key.d", GLFW.GLFW_KEY_D, "key.categories.movement");
    private static final KeyBinding TOGGLE_KEY = new KeyBinding("key.k", GLFW.GLFW_KEY_K, "key.categories.miscellaneous");
    public static List<PlayerEntity> players;
    public static int rtvCount = 0;
    KeyBinding[] keyBindings = {JUMP_KEY, FORWARD_KEY, BACK_KEY, LEFT_KEY, RIGHT_KEY};

    public Bhop() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Bhop.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        for (KeyBinding key : keyBindings){
            ClientRegistry.registerKeyBinding(key);
        }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("bhop", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) { // handling custom client-side commands
        if (event.getMessage().toLowerCase().startsWith("/test")) {
            assert mc.player != null;
            mc.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("This is a test"),false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) { // handling custom server-side commands
//        if (event.getMessage().toLowerCase().startsWith("/rtv")) {
//            rtvCount++;
//            String message = players.size() - rtvCount + " more votes required to rock the vote.";
//            if (rtvCount == players.size()){
//                message = "A vote has been started.";
//            }
//            for (PlayerEntity player : players){
//                player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("§8[§9Notice §8]§f " + player.getName() + " wants to rock the vote! " + message), false);
//            }
//            event.setCanceled(true);
//        }
    }

    private static Vector3d horizontalMotion = Vector3d.ZERO;
    private static double verticalMotion = 0;
    public static Vector3d motion = new Vector3d(horizontalMotion.x, verticalMotion, horizontalMotion.z);
    public static boolean physEnabled = true;
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (TOGGLE_KEY.isPressed()){
            physEnabled = !physEnabled;
            assert mc.player != null;
            mc.player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("bhop physics are now: " + physEnabled), true);
        }
        if (JUMP_KEY.isKeyDown()) {
            PlayerEntity player = mc.player;
            assert player != null;
            if (player.isOnGround()) {
                horizontalMotion = new Vector3d(player.getMotion().x, 0, player.getMotion().z);
                verticalMotion = -0.5;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event){
//        players.remove(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
//      players.add(event.getPlayer());
        event.getPlayer().sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Thank you for using the bhop mod based on the Roblox bhop physics!\nyou can use the \"K\" key on your keyboard to disable the physics at any time."),false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (!player.isUser()) { //Disable this check if you have no respect for replication or like chaos
            return;
        }
        if (!physEnabled) {
            if (JUMP_KEY.isKeyDown() && player.isOnGround()) {
                player.jump();
            }
            return;
        }
        player.setSprinting(false);
        assert mc.player != null;
        motion = player.getMotion();
        airAccelerate(player);
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
    private static final double pi = Math.PI;
    private static final double tau = pi*2;
    private static Vector3d nextAirMotion = Vector3d.ZERO;
    public static void airAccelerate(PlayerEntity player) {
        // this will update for every tick in the onTick() event
        if (player.isOnGround() && !JUMP_KEY.isKeyDown()) {
            nextAirMotion = Vector3d.ZERO;
            return;
        }
        if (player.collidedHorizontally){
            nextAirMotion = Vector3d.ZERO;
        }
        if (nextAirMotion != Vector3d.ZERO) {
            player.setMotion(nextAirMotion.x, motion.y, nextAirMotion.z);
            motion = player.getMotion();
        }
        double units = Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2));
        if (player.isOnGround()) {
            player.jump();
        }
        motion = player.getMotion();
        double yaw = Math.toRadians(player.rotationYawHead);
        double ycos = Math.cos(yaw);
        double ysin = -Math.sin(yaw);
        double optimalScore = clamp(Math.abs((yaw - Math.toRadians(player.prevRotationYawHead) + pi) % tau - pi) / Math.atan2(GAIN_VAR, units), 0, 2);
        // optimalScore = 1-Math.abs(1-optimalScore);
        // This clamps it to 0 -> 1 instead of 0 -> 2 but impossible to know if over or under strafing
        int D = RIGHT_KEY.isKeyDown() ? 1 : 0;
        int A = LEFT_KEY.isKeyDown() ? 1 : 0;
        int W = FORWARD_KEY.isKeyDown() ? 1 : 0;
        int S = BACK_KEY.isKeyDown() ? 1 : 0;
        int DmA = A-D;
        int SmW = W-S;
        units = units * 50.0;
        optimalScore = optimalScore * 100.0;
        player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(String.format("Units: %.2f | Gauge score: %.2f%%", units, optimalScore)), true); // change to be GUIs, can be toggled with client side commands too

        Vector3d KeyAngleData = Vector3d.ZERO;
        if (DmA != 0 || SmW != 0) {
            KeyAngleData = new Vector3d(DmA * ycos + SmW * ysin, 0, SmW * ycos - DmA * ysin).normalize();
        }
        Vector3d PlayerSpeed = player.getMotion();
        double DotProduct = PlayerSpeed.dotProduct(KeyAngleData);
        if (DotProduct < GAIN_VAR) {
            PlayerSpeed = PlayerSpeed.add(KeyAngleData.scale(GAIN_VAR - DotProduct));
        }
        player.setMotion(PlayerSpeed);
        nextAirMotion = player.getMotion();
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
