package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.coordinate.Point;

public class GhostEvent implements PhasmoEvent {
    private final GameContext gameContext;
    private int emfLevel;
    private Point origin;

    public GhostEvent(GameContext gameContext, int emfLevel, Point origin) {
        this.gameContext = gameContext;
        this.emfLevel = emfLevel;
        this.origin = origin;
    }

    @Override
    public GameContext getGameContext() {
        return gameContext;
    }

    public int getEmfLevel() {
        return emfLevel;
    }

    public int getWeightedEmfLevel(Point point) {
        double distsqrd = point.distance(origin);
        final int SCALE = 4;
        return Math.min(Math.max((int) ((((emfLevel + 1) * SCALE) - distsqrd) / SCALE), 0), 5);
    }

    public void setEmfLevel(int emfLevel) {
        this.emfLevel = emfLevel;
    }
}
