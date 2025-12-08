package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.Point;

public interface LightSource {
    int getSourceLevel();

    Point getPosition();

    default int modifyLevelAtPoint(Point point, int originalLevel) {
        return originalLevel;
    }

    default boolean canPropagateFrom(Point point, int level) {
        return true;
    }
}
