package at.nopro.phasmo.content.ghost;

import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.EntityType;

import java.util.Random;

public class BaseGhost extends PhasmoEntity {
    public BaseGhost(EntityType entityType, GameContext gameContext) {
        super(entityType, gameContext);
    }

    protected void activateEMF5() {
        Random emfRandom = new Random();
        gameContext.getEventNode().addListener(GhostEvent.class, (event) -> {
            if(event.getActionType() == GhostEvent.ActionType.INTERACT ||
                event.getActionType() == GhostEvent.ActionType.THROW) {
                if(emfRandom.nextBoolean()) {
                    event.setActionType(GhostEvent.ActionType.EMF_5);
                }
            }
        });
    }
}
