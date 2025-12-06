package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.List;

public class PhasmoChunk extends DynamicChunk {
    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        return LightingCompute.generateLightForChunk(this);
    }

    public List<LightSource> getLightSources() {
        if (getChunkX() == 1 && getChunkZ() == -1) {
            return List.of(new FloodedLightSource(new BlockVec(20, -42, -2), new BlockVec(3, 2, 1)));
        }

        if (getChunkX() == 1 && getChunkZ() == 0) {
            return List.of(new FloodedLightSource(new BlockVec(20, -42, 0), new BlockVec(3, 2, 7)));
        }
        return List.of();
    }
}
