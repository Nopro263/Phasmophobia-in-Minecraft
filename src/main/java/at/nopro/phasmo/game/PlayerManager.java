package at.nopro.phasmo.game;

import at.nopro.phasmo.event.EmfEvent;
import at.nopro.phasmo.event.PlayerDieEvent;
import at.nopro.phasmo.event.SanityDrainEvent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    public final Tag<Color> PLAYERCOLORTAG = Tag.String("color").map(Color::decode, (c) -> c.getRGB() + "");
    public final Tag<Integer> SANITY = Tag.Integer("sanity");
    public final Tag<Boolean> ALIVE = Tag.Boolean("alive");
    public final Color[] PLAYER_COLOR = new Color[]{
            Color.RED,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA
    };
    private final GameContext gameContext;
    private int averageSanity;

    public PlayerManager(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void initPlayerData(Player player) {
        List<Color> textColors = new ArrayList<>(List.of(PLAYER_COLOR));
        for (Player p : gameContext.getInstance().getPlayers()) {
            if (p == player) {
                continue;
            }

            PlayerData playerData = getPlayerData(p);
            textColors.remove(playerData.color);
        }

        if (textColors.isEmpty()) {
            throw new RuntimeException("too many players in lobby");
        }

        player.setTag(SANITY, 50);
        player.setTag(ALIVE, true);
        player.setTag(PLAYERCOLORTAG, textColors.getFirst());
    }

    public PlayerData getPlayerData(Player player) {
        return new PlayerData(
                player.getTag(PLAYERCOLORTAG),
                player.getTag(SANITY),
                player.getTag(ALIVE)
        );
    }

    public List<Player> getAlivePlayers() {
        return gameContext.getInstance().getPlayers().stream().filter(this::isAlive).toList();
    }

    public boolean isAlive(Player player) {
        return player.getTag(ALIVE) != null && player.getTag(ALIVE);
    }

    @ApiStatus.Internal
    public void onSanityDrain(SanityDrainEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.getPlayer().setTag(SANITY, event.getNewSanity());
        gameContext.getDisplayManager().drawSanity();
    }

    @ApiStatus.Internal
    public void onPlayerDie(PlayerDieEvent event) {
        gameContext.getDisplayManager().drawSanity();
    }

    public int getAverageSanity() {
        return averageSanity;
    }

    public void setSanity(Player player, int sanity) {
        player.setTag(SANITY, sanity);
    }

    @ApiStatus.Internal
    public void onGhostEvent(EmfEvent emfEvent) {
        RoomManager.Room room = gameContext.getRoomManager().getRoom(emfEvent.getOrigin());
        if (room == null) {
            return;
        }
        for (Player player : room.getAlivePlayers()) {
            if (player.getTag(ALIVE)) {
                int oldSanity = player.getTag(SANITY);
                int newSanity = Math.max(oldSanity - 2, 0);
                if (oldSanity != newSanity) {
                    gameContext.getEventNode().call(new SanityDrainEvent(
                            gameContext,
                            player,
                            newSanity,
                            oldSanity
                    ));
                }
            }
        }
    }

    @ApiStatus.Internal
    public void setAverageSanity(int sanity) {
        averageSanity = sanity;
    }

    public void showKillAnimation(Player player) {
        player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE, 1));
        player.addEffect(new Potion(PotionEffect.DARKNESS, 0, 5));
    }

    public void kill(Player player) {
        player.setTag(ALIVE, false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlying(true);
        player.setAutoViewable(false);
        player.addEffect(new Potion(PotionEffect.WITHER, 1, Potion.INFINITE_DURATION));
        gameContext.getEventNode().call(new PlayerDieEvent(gameContext, player, false));
    }

    public void revive(Player player) {
        player.setTag(ALIVE, true);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlying(false);
        player.setAutoViewable(true);
        player.removeEffect(PotionEffect.WITHER);
        gameContext.getEventNode().call(new PlayerDieEvent(gameContext, player, true));
    }

    public record PlayerData(Color color, int sanity, boolean alive) {
    }
}
