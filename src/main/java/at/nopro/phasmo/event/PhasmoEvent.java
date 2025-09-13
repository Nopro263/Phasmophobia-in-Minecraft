package at.nopro.phasmo.event;


import at.nopro.phasmo.game.GameContext;
import net.minestom.server.event.Event;

public interface PhasmoEvent extends Event {
    GameContext getGameContext();
}
