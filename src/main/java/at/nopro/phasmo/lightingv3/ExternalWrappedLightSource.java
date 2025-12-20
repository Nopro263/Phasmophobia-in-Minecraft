package at.nopro.phasmo.lightingv3;

import at.nopro.phasmo.lighting.LightSource;
import net.minestom.server.coordinate.Point;

public class ExternalWrappedLightSource implements LightSource {
    private final LightSource lightSource;
    private final IngamePhasmoChunk[] owners;

    public ExternalWrappedLightSource(LightSource lightSource, IngamePhasmoChunk... owners) {
        this.lightSource = lightSource;
        this.owners = owners;
    }

    @Override
    public int getSourceLevel() {
        return lightSource.getSourceLevel();
    }

    @Override
    public Point getPosition() {
        return lightSource.getPosition();
    }

    @Override
    public int modifyLevelAtPoint(Point point, int originalLevel) {
        for (IngamePhasmoChunk owner : owners) {
            if (point.chunkX() == owner.getChunkX() && point.chunkZ() == owner.getChunkZ()) {
                return 0;
            }
        }
        return LightSource.super.modifyLevelAtPoint(point, originalLevel);
    }

    @Override
    public boolean canPropagateFrom(Point point, int level) {
        for (IngamePhasmoChunk owner : owners) {
            if (point.chunkX() == owner.getChunkX() && point.chunkZ() == owner.getChunkZ()) {
                return false;
            }
        }
        return LightSource.super.canPropagateFrom(point, level);
    }

    public IngamePhasmoChunk[] getOwners() {
        return owners;
    }
}
