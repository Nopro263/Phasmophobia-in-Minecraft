package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.Player;

public class PlayerDieEvent implements PhasmoEvent {
    private final GameContext gameContext;
    private final Player player;
    private final boolean isNowAlive;

    public PlayerDieEvent(GameContext gameContext, Player player, boolean isNowAlive) {
        this.gameContext = gameContext;
        this.player = player;
        this.isNowAlive = isNowAlive;
    }

    public boolean isNowAlive() {
        return isNowAlive;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public GameContext getGameContext() {
        return this.gameContext;
    }
}
