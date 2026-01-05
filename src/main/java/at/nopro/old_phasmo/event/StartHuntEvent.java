package at.nopro.old_phasmo.event;

import at.nopro.old_phasmo.game.GameContext;
import net.minestom.server.event.trait.CancellableEvent;

public class StartHuntEvent implements PhasmoEvent, CancellableEvent {
    private final GameContext gameContext;
    private boolean cancelled;

    public StartHuntEvent(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
