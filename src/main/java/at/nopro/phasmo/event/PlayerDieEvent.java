package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.Player;

public record PlayerDieEvent(GameContext gameContext, Player player, boolean isNowAlive) implements PhasmoEvent {
}
