package at.nopro.phasmo;

import at.nopro.phasmo.game.CameraManager;
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
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import static at.nopro.phasmo.Configuration.config;

public class Listeners {
    public static void init() {
        if (config.devMode) {
            addListener(AsyncPlayerPreLoginEvent.class, asyncPlayerPreLoginEvent -> {
                UUID uuid;
                if (asyncPlayerPreLoginEvent.getGameProfile().uuid().equals(CameraManager.getCamPlayerUUID())) {
                    uuid = asyncPlayerPreLoginEvent.getGameProfile().uuid();
                } else {
                    uuid = UUID.randomUUID();
                }
                asyncPlayerPreLoginEvent.setGameProfile(new GameProfile(uuid, asyncPlayerPreLoginEvent.getGameProfile().name()));
            });
        }
        addListener(AsyncPlayerConfigurationEvent.class, event -> {
            GameContext context = GameManager.getGame("default");

            event.setSpawningInstance(context.getInstance());
            event.getPlayer().setRespawnPoint(context.getMapContext().spawnPoint().asPos());

            if (( (TextComponent) event.getPlayer().getName() ).content().equals(CameraManager.getCamPlayerName()) &&
                    event.getPlayer().getUuid().equals(CameraManager.getCamPlayerUUID())) {

                context.setCamPlayer(event.getPlayer());
                event.getPlayer().setRespawnPoint(context.getMapContext().spawnPoint().add(0, 0, 3).asPos().withLookAt(context.getMapContext().spawnPoint()));
                context.getCamPlayer().setAutoViewable(false);
            }
        });
        addListener(PlayerLoadedEvent.class, event -> {
            GameContext context = GameManager.getGame(event.getPlayer().getInstance());

            context.getDisplayManager().sendAllCached(event.getPlayer());
            boolean isCamera = false;

            if (context.getCamPlayer() != null) {
                PlayerInfoRemovePacket pirp = new PlayerInfoRemovePacket(context.getCamPlayer().getUuid());

                if (context.getCamPlayer() == event.getPlayer()) {
                    isCamera = true;
                    context.getCamPlayer().setGameMode(GameMode.SPECTATOR);
                    context.getCamPlayer().addEffect(new Potion(PotionEffect.NIGHT_VISION, 1, -1));
                    for (Player player : event.getInstance().getPlayers()) {
                        player.sendPacket(pirp);
                    }
                } else {
                    event.getPlayer().sendPacket(pirp);
                }
            }

            if (!isCamera) {
                context.getPlayerManager().initPlayerData(event.getPlayer());
                context.getDisplayManager().drawSanity();
            }
        });

        addListener(PlayerBlockInteractEvent.class, event -> {
            GameContext context = GameManager.getGame(event.getPlayer().getInstance());
            MapContext map = context.getMapContext();

            if (event.getBlockPosition().samePoint(map.nvButtonVan())) {
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
