package org.bhop;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.bhop.blocks.BhopBlocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.bhop.Bhop.LOGGER;

@Mod.EventBusSubscriber(modid = Bhop.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BhopServer {
    private static HashMap<Player, Boolean> isInRun = new HashMap<>();
    private static HashMap<Player, Float> playerTime = new HashMap<>();
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                triggerCollideCheck(player);

            }
        }
    }

    private static void triggerCollideCheck(ServerPlayer player) {
        if (player.isCreative()) return;
        BlockPos blockPos = player.getOnPos();
        Level world = player.level();
        List<Block> blocks = Arrays.asList(world.getBlockState(blockPos.above(1)).getBlock(), world.getBlockState(blockPos.above(2)).getBlock());
        if (blocks.contains(BhopBlocks.KILL_TRIGGER.get())) player.kill();

        if (blocks.contains(BhopBlocks.SPAWN_TRIGGER.get())) {
            if (!player.onGround()) { // needs check for is jumping
                isInRun.put(player, true);
                playerTime.put(player, playerTime.get(player) + 0.05F);
            } else {
                isInRun.put(player, false);
            }
        }

        if (blocks.contains(BhopBlocks.END_TRIGGER.get())) {
            if (isInRun.get(player)) {
                sendTimerMessage(player, playerTime.get(player));

                isInRun.put(player, false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
        LOGGER.info("Bhop server has started");
    }

    @SubscribeEvent
    public static void onServerPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        isInRun.put(event.getEntity(), false);
        playerTime.put(event.getEntity(), 0F);
    }

    @SubscribeEvent
    public static void onServerPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        isInRun.remove(event.getEntity());
        playerTime.remove(event.getEntity());
    }

    private static void sendTimerMessage(ServerPlayer player, Float time) {
        try {
            MinecraftServer server = player.getServer();
            assert server != null;
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                serverPlayer.sendSystemMessage(Component.literal("§8[§2Timer§8]§f" + player.getName().getString() + " finished with a time of " + time + " Seconds"));
            }
        } catch (Exception ignored){}
    }
}
