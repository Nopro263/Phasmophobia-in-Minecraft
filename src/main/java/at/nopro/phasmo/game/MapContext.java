package at.nopro.phasmo.game;


import net.minestom.server.coordinate.Point;

public record MapContext(
        String worldPath,
        long time,
        Point spawnPoint,
        Point lowerEnd,
        Point upperEnd,
        Point nvButtonVan
) {}
