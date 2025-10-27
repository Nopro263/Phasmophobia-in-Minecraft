package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.EntityEvent;

public class AfterDropEvent implements PhasmoEvent, EntityEvent {
    private final Entity entity;
    private final GameContext gameContext;
    private final Player player;

    public AfterDropEvent(Entity entity, GameContext gameContext, Player player) {
        this.entity = entity;
        this.gameContext = gameContext;
        this.player = player;
    }

    @Override
    public GameContext getGameContext() {
        return gameContext;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }
}
