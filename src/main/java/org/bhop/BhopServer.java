package org.bhop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
public class BhopServer {
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void executeServerSide() {
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                player.sendSystemMessage(Component.literal("test"));
            }
        } catch (Exception e){e.printStackTrace();}
    }
}
