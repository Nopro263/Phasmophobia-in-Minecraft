package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.BlockVec;

public final class RadialLightSource implements PointLightSource {
    private final BlockVec source;
    private final int strength;

    public RadialLightSource(BlockVec source, int strength) {
        this.source = source;
        this.strength = strength;
    }

    @Override
    public BlockVec getSource() {
        return source;
    }

    public int getStrength() {
        return strength;
    }

    @Override
    public long getId() {
        return this.hashCode();
    }

    public int getLevelAtPosition(int x, int y, int z) {
        x -= source.blockX();
        y -= source.blockY();
        z -= source.blockZ();

        int d = (int) Math.sqrt(x * x + y * y + z * z);
        return strength - d;
    }
}
