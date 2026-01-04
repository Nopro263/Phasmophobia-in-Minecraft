package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;

public class SanityDrainEvent implements PhasmoEvent, CancellableEvent {
    private final GameContext gameContext;
    private Player player;
    private int newSanity;
    private int oldSanity;
    private boolean cancelled;

    public SanityDrainEvent(GameContext gameContext, Player player, int newSanity, int oldSanity) {
        this.gameContext = gameContext;
        this.player = player;
        this.newSanity = newSanity;
        this.oldSanity = oldSanity;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getOldSanity() {
        return oldSanity;
    }

    public void setOldSanity(int oldSanity) {
        this.oldSanity = oldSanity;
    }

    public int getNewSanity() {
        return newSanity;
    }

    public void setNewSanity(int newSanity) {
        this.newSanity = newSanity;
    }

    @Override
    public GameContext gameContext() {
        return this.gameContext;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
