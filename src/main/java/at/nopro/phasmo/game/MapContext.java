package at.nopro.phasmo.game;


import at.nopro.phasmo.lightingv3.VanLightSource;
import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.Direction;

import java.util.List;

public record MapContext(
        String worldPath,
        long time,
        Point spawnPoint,
        Point lowerEnd,
        Point upperEnd,
        List<Integer> validLevels,
        Point nvButtonVan,
        Direction mapUpIsDirection,
        VanLightSource vanLightSource
) {
}
