package at.nopro.phasmo.game;

import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.event.SanityDrainEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    public final Tag<Color> PLAYERCOLORTAG = Tag.String("color").map(Color::decode, (c) -> c.getRGB() + "");
    public final Tag<Integer> SANITY = Tag.Integer("sanity");
    public final Tag<Boolean> ALIVE = Tag.Boolean("alive");

    private final GameContext gameContext;

    public PlayerManager(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public final Color[] PLAYER_COLOR = new Color[] {
            Color.RED,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA
    };

    public record PlayerData(Color color, int sanity, boolean alive) {}

    public PlayerData getPlayerData(Player player) {
        return new PlayerData(
                player.getTag(PLAYERCOLORTAG),
                player.getTag(SANITY),
                player.getTag(ALIVE)
        );
    }

    public void initPlayerData(Player player) {
        List<Color> textColors = new ArrayList<>(List.of(PLAYER_COLOR));
        for(Player p : gameContext.getInstance().getPlayers()) {
            if(p == player) {
                continue;
            }

            PlayerData playerData = getPlayerData(p);
            textColors.remove(playerData.color);
        }

        if(textColors.isEmpty()) {
            throw new RuntimeException("too many players in lobby");
        }

        player.setTag(SANITY, 50);
        player.setTag(ALIVE, true);
        player.setTag(PLAYERCOLORTAG, textColors.getFirst());
    }

    public void onGhostEvent(GhostEvent ghostEvent) {
        RoomManager.Room room = gameContext.getRoomManager().getRoom(ghostEvent.getOrigin());
        if(room == null) {
            return;
        }
        for(Player player : room.getPlayers()) {
            if(player.getTag(ALIVE)) {
                int oldSanity = player.getTag(SANITY);
                int newSanity = Math.max(oldSanity-2, 0);
                if(oldSanity != newSanity) {
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

    public void onSanityDrain(SanityDrainEvent event) {
        if(event.isCancelled()) {
            return;
        }
        event.getPlayer().setTag(SANITY, event.getNewSanity());
        gameContext.getDisplayManager().drawSanity();
    }
}
