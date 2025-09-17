package at.nopro.phasmo;

import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.MapContext;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class Listeners {
    public static void init() {
        addListener(AsyncPlayerPreLoginEvent.class, asyncPlayerPreLoginEvent -> {
            //asyncPlayerPreLoginEvent.setGameProfile(new GameProfile(UUID.randomUUID(), asyncPlayerPreLoginEvent.getGameProfile().name()));
        });
        addListener(AsyncPlayerConfigurationEvent.class, event -> {
            GameContext context = GameManager.getGame("default");

            event.setSpawningInstance(context.getInstance());
            event.getPlayer().setRespawnPoint(context.getMapContext().spawnPoint().asPos());

            if(((TextComponent)event.getPlayer().getName()).content().equals("CAM")) {
                context.setCamPlayer(event.getPlayer());
                event.getPlayer().setRespawnPoint(context.getMapContext().spawnPoint().add(0,0,3).asPos().withLookAt(context.getMapContext().spawnPoint()));
                context.getCamPlayer().setAutoViewable(false);
            }
        });
        addListener(PlayerLoadedEvent.class, event -> {
            GameContext context = GameManager.getGame(event.getPlayer().getInstance());

            context.getDisplayManager().sendAllCached(event.getPlayer());

            if(context.getCamPlayer() != null) {
                PlayerInfoRemovePacket pirp = new PlayerInfoRemovePacket(context.getCamPlayer().getUuid());

                if (context.getCamPlayer() == event.getPlayer()) {
                    context.getCamPlayer().setGameMode(GameMode.SPECTATOR);
                    context.getCamPlayer().addEffect(new Potion(PotionEffect.NIGHT_VISION, 1, -1));
                    for (Player player : event.getInstance().getPlayers()) {
                        player.sendPacket(pirp);
                    }
                } else {
                    event.getPlayer().sendPacket(pirp);
                }
            }
        });

        addListener(PlayerBlockInteractEvent.class, event -> {
            GameContext context = GameManager.getGame(event.getPlayer().getInstance());
            MapContext map = context.getMapContext();

            if(event.getBlockPosition().samePoint(map.nvButtonVan())) {
                boolean nowEnabled = !context.getCameraManager().hasNightVisionEnabled();
                context.getCameraManager().setNightVision(nowEnabled);
            }
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
