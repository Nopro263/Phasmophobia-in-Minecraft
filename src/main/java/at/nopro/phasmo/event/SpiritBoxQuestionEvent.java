package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.Player;

public record SpiritBoxQuestionEvent(GameContext gameContext, Player player) implements PhasmoEvent {

    @Override
    public GameContext gameContext() {
        return gameContext;
    }
}
