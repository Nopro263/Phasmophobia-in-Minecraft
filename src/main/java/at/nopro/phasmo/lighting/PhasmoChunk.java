package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PhasmoChunk extends DynamicChunk {
    private final Map<Chunk, Set<NewLightingCompute.ExternalLight>> externalLights;
    private Set<NewLightingCompute.ExternalLight> previousExternalLights;
    private PhasmoChunk currentModifyingChunk;
    private LightData oldLightData;
    private final CachedPacket cachedLightPacket = new CachedPacket(this::createLightPacket);

    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);

        externalLights = new HashMap<>();
    }

    private UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    @ApiStatus.Internal
    void addExternalLight(NewLightingCompute.ExternalLight externalLight) {
        if (externalLight.owner() != currentModifyingChunk) {
            throw new RuntimeException("huh???");
        }
        externalLights.putIfAbsent(externalLight.owner(), new HashSet<>()).add(externalLight);
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
    void removeAllExternalLightsComingFromChunk(PhasmoChunk phasmoChunk) {
        previousExternalLights = externalLights.get(phasmoChunk);
        currentModifyingChunk = phasmoChunk;
        externalLights.put(phasmoChunk, new HashSet<>());
    }

    @ApiStatus.Internal
    void invalidateChunkIfLightsChanged() {
        if (previousExternalLights == null) {
            cachedLightPacket.invalidate();
            resendLight();
        } else {
            int diff = Math.abs(previousExternalLights.size() - externalLights.get(currentModifyingChunk).size());

            if (diff >= 1) {
                cachedLightPacket.invalidate();
                resendLight();
                System.out.println(diff);
            }
        }
    }

    public void resendLight() {
        if (this.isLoaded()) {
            this.sendPacketToViewers(cachedLightPacket);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        cachedLightPacket.invalidate();
    }

    @Override
    protected @NotNull LightData createLightData(boolean requiredFullChunk) {
        Set<NewLightingCompute.ExternalLight> combinedLights = new HashSet<>();
        for (Set<NewLightingCompute.ExternalLight> e : externalLights.values()) {
            combinedLights.addAll(e);
        }
        oldLightData = NewLightingCompute.generateLightForChunk(this, combinedLights, oldLightData);
        return oldLightData;
    }
}
