package at.nopro.old_phasmo.event;

import at.nopro.old_phasmo.game.GameContext;

public record SpiritBoxAnswerEvent(GameContext gameContext) implements PhasmoEvent {

    @Override
    public GameContext gameContext() {
        return gameContext;
    }
}
