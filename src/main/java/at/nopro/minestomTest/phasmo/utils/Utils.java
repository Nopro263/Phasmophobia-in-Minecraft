package at.nopro.minestomTest.phasmo.utils;

import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.entity.Entity;

public class Utils {
    private Utils() {}

    public static boolean isLineOfSight(Entity looker, Entity entity) {
        return CollisionUtils.isLineOfSightReachingShape(looker.getInstance(), looker.getChunk(), looker.getPosition().add(0, looker.getEyeHeight(), 0), entity.getPosition(), entity.getBoundingBox());
    }
}
