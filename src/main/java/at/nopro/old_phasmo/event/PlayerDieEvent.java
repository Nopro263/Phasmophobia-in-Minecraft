package at.nopro.old_phasmo.event;

import at.nopro.old_phasmo.game.GameContext;
import net.minestom.server.entity.Player;

public record PlayerDieEvent(GameContext gameContext, Player player, boolean isNowAlive) implements PhasmoEvent {
}
