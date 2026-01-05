package at.nopro.old_phasmo.event;


import at.nopro.old_phasmo.game.GameContext;
import net.minestom.server.event.Event;

public interface PhasmoEvent extends Event {
    GameContext gameContext();
}
