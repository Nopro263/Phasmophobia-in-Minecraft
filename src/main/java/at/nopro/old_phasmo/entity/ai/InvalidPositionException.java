package at.nopro.old_phasmo.entity.ai;

import net.minestom.server.coordinate.Point;

public class InvalidPositionException extends Exception {
    private final Point point;

    public InvalidPositionException(String message, Point point) {
        super(message + "[" + point.x() + ", " + point.y() + ", " + point.z() + "]");
        this.point = point;
    }
}
