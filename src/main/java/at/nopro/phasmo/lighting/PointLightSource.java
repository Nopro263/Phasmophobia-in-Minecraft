package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.Point;

public interface PointLightSource extends LightSource {
    Point getSource();
}
