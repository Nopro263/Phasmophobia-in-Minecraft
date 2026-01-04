package at.nopro.phasmo.content.ghost;

import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.event.*;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.RoomManager;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BaseGhost extends PhasmoEntity {
    private final List<GoalSelector> goalSelectors;
    private final Map<GoalSelector, Integer> goalPriorities;

    public BaseGhost(EntityType entityType, GameContext gameContext) {
        super(entityType, gameContext);

        setAutoViewable(false);

        goalSelectors = new ArrayList<>();
        goalPriorities = new HashMap<>();
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.15);
    }

    public String getPositionAsString() {
        return "[" + getPosition().x() + ", " + getPosition().y() + ", " + getPosition().z() + "]";
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return new BoundingBox(0.2, 1.2, 0.2);
    }

    @Override
    public boolean hasEntityCollision() {
        return false;
    }

    protected void addGoal(int priority, GoalSelector goal) {
        goalPriorities.put(goal, priority);
        goalSelectors.add(goal);
    }

    @Override
    public void spawn() {
        super.spawn();
        EntityAIGroup aiGroup = new EntityAIGroup();
        goalSelectors.sort((o1, o2) -> goalPriorities.get(o1) - goalPriorities.get(o2));
        aiGroup.getGoalSelectors().addAll(goalSelectors);
        addAIGroup(aiGroup);
    }

    protected void activateEMF5() {
        Random emfRandom = new Random();
        gameContext.getEventNode().addListener(EmfEvent.class, (event) -> {
            if (event.getActionType() == EmfEvent.ActionType.INTERACT ||
                    event.getActionType() == EmfEvent.ActionType.THROW) {
                if (emfRandom.nextBoolean()) {
                    event.setActionType(EmfEvent.ActionType.EMF_5);
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
            if (event.getPoint().distanceSquared(getPosition()) < 2) {
                showWhenInDOTS();
            }
        });
    }

    protected void activateUV() {
        //TODO
    }

    protected void activateBookWriting() {
        //TODO
    }

    protected void activateGhostOrbs() {
        //TODO
    }

    protected void activateSpiritBox() {
        gameContext.getEventNode().addListener(SpiritBoxQuestionEvent.class, (event) -> {
            RoomManager.Room playerRoom = gameContext.getRoomManager().getRoom(event.player().getPosition());
            RoomManager.Room ghostRoom = getRoom();

            if (ghostRoom.equals(playerRoom)) {
                gameContext.getEventNode().call(new SpiritBoxAnswerEvent(gameContext));
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
