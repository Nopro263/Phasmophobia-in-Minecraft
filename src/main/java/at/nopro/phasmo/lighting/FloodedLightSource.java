package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;

public final class FloodedLightSource implements LightSource {
    private final BlockVec position;
    private final BlockVec position2;
    private final BlockVec size;
    private final int level;

    public FloodedLightSource(Point position, Point size, int level) {
        this.position = position.asBlockVec();
        this.size = size.asBlockVec();
        this.level = level;
        this.position2 = position.add(size).asBlockVec();
    }

    public BlockVec getPosition() {
        return position;
    }

    public BlockVec getPosition2() {
        return position2;
    }

    public BlockVec getSize() {
        return size;
    }

    @Override
    public int getSourceLevel() {
        return level;
    }

    public boolean inRange(int x, int y, int z) {
        return position.blockX() <= x && x <= position2.blockX() &&
                position.blockY() <= y && y <= position2.blockY() &&
                position.blockZ() <= z && z <= position2.blockZ();
    }

    @Override
    public int modifyLevelAtPoint(Point point, int originalLevel) {
        return inRange(point.blockX(), point.blockY(), point.blockZ()) ? 15 : 0;
    }

    @Override
    public boolean canPropagateFrom(Point point, int level) {
        return inRange(point.blockX(), point.blockY(), point.blockZ());
    }
}
