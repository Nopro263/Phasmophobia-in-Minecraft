package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;

public record SpiritBoxAnswerEvent(GameContext gameContext) implements PhasmoEvent {

    @Override
    public GameContext gameContext() {
        return gameContext;
    }
}
