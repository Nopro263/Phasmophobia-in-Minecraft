package at.nopro.phasmo.event;

import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;

public class BeforePickupEvent implements PhasmoEvent, EntityEvent, CancellableEvent {
    private final ItemEntity entity;
    private final GameContext gameContext;
    private final Player player;
    private boolean cancelled;

    public BeforePickupEvent(ItemEntity entity, GameContext gameContext, Player player) {
        this.entity = entity;
        this.gameContext = gameContext;
        this.player = player;
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public ItemEntity getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
