package at.nopro.phasmo.core;

import at.nopro.phasmo.Main;
import at.nopro.phasmo.gameplay.lobby.LobbyInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;

import java.io.IOException;
import java.util.Objects;

public final class CoreListeners {
    private CoreListeners() {
    }

    public static void init() {
        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, CoreListeners::onPing);
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, CoreListeners::onConfiguration);
    }

    private static void onPing(ServerListPingEvent serverListPingEvent) {
        serverListPingEvent.setStatus(new Status(
                getDescription(),
                getIcon(),
                Status.VersionInfo.DEFAULT,
                Status.PlayerInfo.builder(Status.PlayerInfo.onlineCount()).maxPlayers(20).build(),
                false
        ));
    }

    private static Component getDescription() {
        return Component.text("Phasmophobia").color(TextColor.color(255, 255, 255)).append(
                Component.text(" in Minecraft").color(TextColor.color(0x03, 0x67, 0x6a))
        );
    }

    private static byte[] getIcon() {
        try {
            return Objects.requireNonNull(Main.class.getResourceAsStream("icon.png")).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Server icon reading error", e);
        }
    }


    private static void onConfiguration(AsyncPlayerConfigurationEvent playerConfigurationEvent) {
        playerConfigurationEvent.setClearChat(true);
        playerConfigurationEvent.setHardcore(false);
        playerConfigurationEvent.setSpawningInstance(LobbyInstance.INSTANCE);
    }
}
