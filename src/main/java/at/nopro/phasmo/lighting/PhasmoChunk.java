package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhasmoChunk extends DynamicChunk {
    private final Set<NewLightingCompute.ExternalLight> externalLights;

    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);

        externalLights = new HashSet<>();
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        return NewLightingCompute.generateLightForChunk(this, externalLights);
    }

    public List<LightSource> getLightSources() {
        if (getChunkX() == 1 && getChunkZ() == -1) {
            return List.of(new FloodedLightSource(new BlockVec(20, -42, -2), new BlockVec(3, 2, 9), 14));
        }

        if (getChunkX() == 1 && getChunkZ() == 1) {
            return List.of(new RadialLightSource(new BlockVec(23, -42, 24), 15));
        }
        return List.of();
    }

    public List<PhasmoChunk> getNeighbours() {
        List<PhasmoChunk> chunks = new ArrayList<>(8);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk chunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (chunk == null || chunk == this) {
                    continue;
                }

                if (!( chunk instanceof PhasmoChunk pc )) {
                    throw new RuntimeException("chunk not the expected phasmo-chunk");
                }
                chunks.add(pc);
            }
        }
        return chunks;
    }

    @ApiStatus.Internal
    void addExternalLight(NewLightingCompute.ExternalLight externalLight) {
        externalLights.add(externalLight);
    }
}
