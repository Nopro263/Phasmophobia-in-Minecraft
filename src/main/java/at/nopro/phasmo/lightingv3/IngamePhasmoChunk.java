package at.nopro.phasmo.lightingv3;

import at.nopro.phasmo.lighting.FloodedLightSource;
import at.nopro.phasmo.lighting.LightSource;
import at.nopro.phasmo.lighting.RadialLightSource;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IngamePhasmoChunk extends DynamicChunk {
    private final Map<Chunk, Set<LightingCompute.ExternalLight>> externalLights;
    private Set<LightingCompute.ExternalLight> previousExternalLights;
    private IngamePhasmoChunk currentModifyingChunk;
    private LightData oldLightData;
    boolean toggle;
    private final CachedPacket cachedLightPacket = new CachedPacket(this::createLightPacket);
    private LightData bakedLightData;

    public IngamePhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);

        externalLights = new HashMap<>();
    }

    private UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    public void bake() {
        bakedLightData = createLightData(true);
    }

    @Override
    public @NotNull LightData createLightData(boolean requiredFullChunk) {
        System.out.print("Chunk[" + chunkX + "," + chunkZ + "] started lighting");
        long start = System.currentTimeMillis();

        for (Section section : sections) {

        }

        long diff = System.currentTimeMillis() - start;
        System.out.print("\r");
        System.out.println("Chunk[" + chunkX + "," + chunkZ + "] finished in " + ( diff ) + "ms");

        return oldLightData;
    }

    public void toggle() {
        toggle = !toggle;
    }

    @ApiStatus.Internal
    void addExternalLight(LightingCompute.ExternalLight externalLight) {
        if (externalLight.owner() != currentModifyingChunk) {
            throw new RuntimeException("huh???");
        }
        externalLights.putIfAbsent(externalLight.owner(), new HashSet<>()).add(externalLight);
    }

    public LightData getBakedLightData() {
        return bakedLightData;
    }

    public List<IngamePhasmoChunk> getNeighbours() {
        List<IngamePhasmoChunk> chunks = new ArrayList<>(8);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk chunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (chunk == null || chunk == this) {
                    continue;
                }

                if (!( chunk instanceof IngamePhasmoChunk pc )) {
                    throw new RuntimeException("chunk not the expected phasmo-chunk");
                }
                chunks.add(pc);
            }
        }
        return chunks;
    }

    @ApiStatus.Internal
    void removeAllExternalLightsComingFromChunk(IngamePhasmoChunk phasmoChunk) {
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

    public List<LightSource> getLightSources() {
        if (getChunkX() == 1 && getChunkZ() == -1) {
            return List.of(new FloodedLightSource(new BlockVec(20, -42, -2), new BlockVec(3, 2, 9), 14));
        }

        if (getChunkX() == 1 && getChunkZ() == 1) {
            List<LightSource> lightSources = new ArrayList<>();
            lightSources.add(new RadialLightSource(new BlockVec(23, -42, 24), 5));
            if (toggle) {
                lightSources.add(new RadialLightSource(new BlockVec(23, -41, 24), 15));
            }
            return lightSources;
        }
        return List.of();
    }

    public @NotNull LightData OldcreateLightData(boolean requiredFullChunk) {
        System.out.print("Chunk[" + chunkX + "," + chunkZ + "] started lighting");
        long start = System.currentTimeMillis();

        Set<LightingCompute.ExternalLight> combinedLights = new HashSet<>();
        for (Set<LightingCompute.ExternalLight> e : externalLights.values()) {
            combinedLights.addAll(e);
        }
        oldLightData = LightingCompute.generateLightForChunk(this, combinedLights, oldLightData);

        long diff = System.currentTimeMillis() - start;
        System.out.print("\r");
        System.out.println("Chunk[" + chunkX + "," + chunkZ + "] finished in " + ( diff ) + "ms");

        return oldLightData;
    }

    private record Light(Point point, int level) {
    }
}
