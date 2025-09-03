package at.nopro.phasmo;

import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Listeners {
    public static void init() {
        addListener(AsyncPlayerConfigurationEvent.class, event -> {
            GameContext context = GameManager.getGame("default");

            event.setSpawningInstance(context.getInstance());
            event.getPlayer().setRespawnPoint(context.getMapContext().spawnPoint().asPos());
        });
    }

    private static <E extends Event> EventNode<@NotNull Event> addListener(Class<E> eventType, Consumer<E> listener) {
        return MinecraftServer.getGlobalEventHandler().addListener(eventType, listener);
    }
}
