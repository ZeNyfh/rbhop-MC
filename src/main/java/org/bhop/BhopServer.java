package org.bhop;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.bhop.blocks.BhopBlocks;

import static org.bhop.Bhop.LOGGER;

@Mod.EventBusSubscriber(modid = Bhop.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BhopServer {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            event.getServer().getPlayerList().getPlayers().forEach(BhopServer::triggerCollideCheck);
            // any other trigger block checks and server side code here
        }
    }

    private static void triggerCollideCheck(ServerPlayer player) {
        if (player.isCreative()) return;
        BlockPos blockPos = player.getOnPos();
        Level world = player.level();
        BlockState blockState1 = world.getBlockState(blockPos.above(1)); // foot level
        BlockState blockState2 = world.getBlockState(blockPos.above(2)); // head level
        if (!player.isCreative()) {
            if (blockState1.getBlock().equals(BhopBlocks.KILL_TRIGGER.get()) || blockState2.getBlock().equals(BhopBlocks.KILL_TRIGGER.get())) player.kill();
            // other trigger blocks here
        }
    }

    @SubscribeEvent
    public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
        LOGGER.info("Bhop server has started");
    }

    @SubscribeEvent
    public static void onServerPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent
    public static void onServerPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {

    }
}
