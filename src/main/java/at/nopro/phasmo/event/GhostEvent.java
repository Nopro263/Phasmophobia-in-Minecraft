package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.coordinate.Point;

public class GhostEvent implements PhasmoEvent {
    private final GameContext gameContext;
    private Point origin;
    private ActionType actionType;

    public GhostEvent(GameContext gameContext, ActionType actionType, Point origin) {
        this.gameContext = gameContext;
        this.actionType = actionType;
        this.origin = origin;
    }

    @Override
    public GameContext getGameContext() {
        return gameContext;
    }

    public int getEmfLevel() {
        return actionType.emf;
    }

    public int getWeightedEmfLevel(Point point) {
        double distsqrd = point.distance(origin);
        final int SCALE = 4;
        return Math.min(Math.max((int) ((((actionType.emf + 1) * SCALE) - distsqrd) / SCALE), 0), 5);
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Point getOrigin() {
        return origin;
    }

    public enum ActionType {
        INTERACT(2),
        THROW(3),
        GHOST_EVENT(4),
        EMF_5(5);

        private final int emf;
        ActionType(int emf) {
            this.emf = emf;
        }
    }
}
