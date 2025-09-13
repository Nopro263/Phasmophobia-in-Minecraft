package at.nopro.phasmo;

import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class Listeners {
    public static void init() {
        addListener(AsyncPlayerPreLoginEvent.class, asyncPlayerPreLoginEvent -> {
            asyncPlayerPreLoginEvent.setGameProfile(new GameProfile(UUID.randomUUID(), asyncPlayerPreLoginEvent.getGameProfile().name()));
        });
        addListener(AsyncPlayerConfigurationEvent.class, event -> {
            GameContext context = GameManager.getGame("default");

            event.setSpawningInstance(context.getInstance());
            event.getPlayer().setRespawnPoint(context.getMapContext().spawnPoint().asPos());
        });

        addListener(PlayerBlockPlaceEvent.class, event -> {
            event.setCancelled(true);
        });

        addListener(PlayerBlockBreakEvent.class, event -> {
            event.setCancelled(true);
        });

        addListener(PlayerStartDiggingEvent.class, event -> {
            event.setCancelled(true);
        });
    }

    private static <E extends Event> EventNode<@NotNull Event> addListener(Class<E> eventType, Consumer<E> listener) {
        return MinecraftServer.getGlobalEventHandler().addListener(eventType, listener);
    }
}
