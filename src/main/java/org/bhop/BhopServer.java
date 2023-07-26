package org.bhop;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

import static org.bhop.Bhop.LOGGER;

@Mod.EventBusSubscriber(modid = Bhop.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BhopServer {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

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
