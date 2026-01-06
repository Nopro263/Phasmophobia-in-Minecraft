package at.nopro.phasmo.utils;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.UUID;

public final class Utils {
    private Utils() {
    }

    public static UUID uuidFromObject(Object object) {
        return UUID.nameUUIDFromBytes(Integer.toHexString(object.hashCode()).getBytes());
    }

    public static String asString(BlockVec vec) {
        return "[" + vec.blockX() + ", " + vec.blockY() + ", " + vec.blockZ() + "]";
    }

    public static String asString(Vec vec) {
        return "[" + vec.x() + ", " + vec.y() + ", " + vec.z() + "]";
    }

    public static String asString(Pos pos) {
        return "[" + pos.x() + ", " + pos.y() + ", " + pos.z() + "] (" + pos.yaw() + ", " + pos.pitch() + ")";
    }
}
