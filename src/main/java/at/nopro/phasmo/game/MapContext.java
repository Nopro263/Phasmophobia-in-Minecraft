package at.nopro.phasmo.game;


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
        Direction mapUpIsDirection
) {
}
