package at.nopro.phasmo.content.ghost;

import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.event.DOTSEvent;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.event.TemperatureEvent;
import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.EntityType;
import net.minestom.server.timer.TaskSchedule;

import java.util.Random;

public class BaseGhost extends PhasmoEntity {
    public BaseGhost(EntityType entityType, GameContext gameContext) {
        super(entityType, gameContext);

        setAutoViewable(false);
    }

    protected void activateEMF5() {
        Random emfRandom = new Random();
        gameContext.getEventNode().addListener(GhostEvent.class, (event) -> {
            if (event.getActionType() == GhostEvent.ActionType.INTERACT ||
                    event.getActionType() == GhostEvent.ActionType.THROW) {
                if (emfRandom.nextBoolean()) {
                    event.setActionType(GhostEvent.ActionType.EMF_5);
                }
            }
        });
    }

    protected void activateFreezing() {
        gameContext.getEventNode().addListener(TemperatureEvent.class, (event) -> {
            if (getRoom() == event.getRoom()) {
                event.setTemperature(-10);
            }
        });
    }

    protected void activateDOTS() {
        gameContext.getEventNode().addListener(DOTSEvent.class, (event) -> {
            if (event.getPoint().sameBlock(getPosition()) || event.getPoint().sameBlock(getPosition().add(0, 1, 0))) {
                showWhenInDOTS();
            }
        });
    }


    private void showWhenInDOTS() {
        setAutoViewable(true);
        gameContext.getScheduler().run(this.hashCode() + "GhostDOTS", (first) -> {
            if (first) return TaskSchedule.tick(10);

            setAutoViewable(false);
            return TaskSchedule.stop();
        });
    }
}
