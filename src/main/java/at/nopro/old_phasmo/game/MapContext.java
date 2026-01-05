package at.nopro.old_phasmo.game;


import at.nopro.old_phasmo.light.VanLightSource;
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
