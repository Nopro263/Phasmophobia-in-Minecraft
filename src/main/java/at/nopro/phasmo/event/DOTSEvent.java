package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.coordinate.Point;

public class DOTSEvent implements PhasmoEvent {
    private final GameContext gameContext;
    private Point point;

    public DOTSEvent(GameContext gameContext, Point point) {
        this.gameContext = gameContext;
        this.point = point;
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
