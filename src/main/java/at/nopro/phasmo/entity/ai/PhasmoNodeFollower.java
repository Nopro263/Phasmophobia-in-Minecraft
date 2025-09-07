package at.nopro.phasmo.entity.ai;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.followers.NodeFollower;
import org.jetbrains.annotations.Nullable;

public class PhasmoNodeFollower implements NodeFollower {
    private final PhasmoEntity entity;

    public PhasmoNodeFollower(PhasmoEntity entity) {
        this.entity = entity;
    }

    @Override
    public void moveTowards(Point point, double v, Point lookAt) {
        this.entity.teleport(point.asPos());
    }

    @Override
    public void jump(@Nullable Point point, @Nullable Point point1) {
        throw new RuntimeException("No jump");
    }

    @Override
    public boolean isAtPoint(Point point) {
        return this.entity.getPosition().distanceSquared(point) < 0.5;
    }

    @Override
    public double movementSpeed() {
        return 1;
    }
}
