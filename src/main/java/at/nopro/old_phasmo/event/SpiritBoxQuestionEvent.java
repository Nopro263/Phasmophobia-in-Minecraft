package at.nopro.old_phasmo.event;

import at.nopro.old_phasmo.game.GameContext;
import net.minestom.server.entity.Player;

public record SpiritBoxQuestionEvent(GameContext gameContext, Player player) implements PhasmoEvent {

    @Override
    public GameContext gameContext() {
        return gameContext;
    }
}
