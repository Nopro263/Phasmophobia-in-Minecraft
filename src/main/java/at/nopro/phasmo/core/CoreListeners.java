package at.nopro.phasmo.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;

public final class CoreListeners {
    private CoreListeners() {
    }

    public static void init() {
        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, CoreListeners::onPing);
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
        return null; //TODO find a suitable icon
    }
}
