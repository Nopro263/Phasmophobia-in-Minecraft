package at.nopro.phasmo.lightingv3;

import at.nopro.phasmo.lighting.FloodedLightSource;
import at.nopro.phasmo.lighting.LightSource;
import at.nopro.phasmo.lighting.RadialLightSource;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IngamePhasmoChunk extends DynamicChunk {
    boolean toggle;
    private final SectionLight[] sectionLights;
    private final CachedPacket cachedLightPacket = new CachedPacket(this::createLightPacket);

    public IngamePhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);

        this.sectionLights = new SectionLight[getMaxSection() - getMinSection() + 2];

        for (int i = 0; i < sections.size() + 2; i++) {
            this.sectionLights[i] = new SectionLight();
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        calculateSkyLight();
        calculateVanLight();
    }

    public void calculateSkyLight() {
        int minY = instance.getCachedDimensionType().minY();

        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        for (int i = -1; i < sections.size() + 1; i++) {
            int sectionMinY = i * 16 + minY;

            int finalI = i;
            tasks.add(CompletableFuture.runAsync(() -> {
                SectionLight sectionLight = getSectionLight(finalI);

                byte[] dayLight = new byte[2048];

                LightCompute.computeSectionSkyLight(
                        dayLight,
                        motionBlockingHeightmap(),
                        15,
                        sectionMinY
                );

                sectionLight.setDaylightValue(dayLight);
            }));
        }
    }

    private UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    public void calculateVanLight() {
        int minY = instance.getCachedDimensionType().minY();
        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;

        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        VanLightSource vanLightSource;
        if (!( instance instanceof PhasmoInstance phasmoInstance )) {
            return;
        }
        vanLightSource = phasmoInstance.getGameContext().getMapContext().vanLightSource();

        for (int i = -1; i < sections.size() + 1; i++) {
            int sectionMinY = i * 16 + minY;

            int finalI = i;
            tasks.add(CompletableFuture.runAsync(() -> {
                SectionLight sectionLight = getSectionLight(finalI);

                byte[] vanLight = new byte[2048];

                LightCompute.computeSectionVanLight(
                        vanLight,
                        15,
                        sectionMinY,
                        chunkMinX,
                        chunkMinZ,
                        vanLightSource
                );

                sectionLight.setBlockAllOffValue(vanLight);
            }));
        }


        for (CompletableFuture<Void> task : tasks) {
            task.join();
        }
    }

    private SectionLight getSectionLight(int sectionIndex) {
        return this.sectionLights[sectionIndex + 1];
    }

    @Override
    public @NotNull LightData createLightData(boolean requiredFullChunk) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        //System.out.print("Chunk[" + chunkX + "," + chunkZ + "] started lighting");
        //long start = System.currentTimeMillis();

        for (int i = -1; i < sections.size() + 1; i++) {
            SectionLight sectionLight = getSectionLight(i);

            //recalculate light

            skyMask.set(i + 1);
            skyLights.add(sectionLight.getDaylight());

            blockMask.set(i + 1);
            blockLights.add(LightCompute.bake(
                    sectionLight.getBlockAllOff(),
                    sectionLight.getBlockHouseLightsCurrentlyOn(),
                    sectionLight.getBlockDynamic()
            ));
        }

        //long diff = System.currentTimeMillis() - start;
        //System.out.print("\r");
        //System.out.println("Chunk[" + chunkX + "," + chunkZ + "] finished in " + ( diff ) + "ms");

        return new LightData(
                skyMask,
                blockMask,
                emptySkyMask,
                emptyBlockMask,
                skyLights,
                blockLights
        );
    }

    public void toggle() {
        toggle = !toggle;
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

    private static final class SectionLight {
        private byte[] daylight;
        private byte[] blockAllOff;
        private byte[] blockDynamic;
        private byte[] blockHouseLightsCurrentlyOn;

        private boolean blockHouseLightsCurrentlyOnToggle = false;

        private SectionLight() {

        }

        public byte[] getDaylight() {
            return daylight != null ? daylight : LightCompute.EMPTY_CONTENT;
        }

        public byte[] getBlockAllOff() {
            return blockAllOff != null ? blockAllOff : LightCompute.EMPTY_CONTENT;
        }

        public byte[] getBlockHouseLightsCurrentlyOn() {
            return blockHouseLightsCurrentlyOnToggle ? blockHouseLightsCurrentlyOn : LightCompute.EMPTY_CONTENT;
        }

        public byte[] getBlockDynamic() {
            return blockDynamic != null ? blockDynamic : LightCompute.EMPTY_CONTENT;
        }

        public void setBlockDynamicValue(byte[] blockDynamic) {
            this.blockDynamic = blockDynamic;
        }

        public void setDaylightValue(byte[] daylight) {
            this.daylight = daylight;
        }

        public void setBlockAllOffValue(byte[] blockAllOff) {
            this.blockAllOff = blockAllOff;
        }

        public void setBlockHouseLightsCurrentlyOnValue(byte[] blockHouseLightsCurrentlyOn) {
            this.blockHouseLightsCurrentlyOn = blockHouseLightsCurrentlyOn;
        }

        public void setBlockHouseLightsCurrentlyOnToggle(boolean blockHouseLightsCurrentlyOnToggle) {
            this.blockHouseLightsCurrentlyOnToggle = blockHouseLightsCurrentlyOnToggle;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(daylight), Arrays.hashCode(blockAllOff), Arrays.hashCode(blockHouseLightsCurrentlyOn));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SectionLight) obj;
            return Arrays.equals(this.daylight, that.daylight) &&
                    Arrays.equals(this.blockAllOff, that.blockAllOff) &&
                    Arrays.equals(this.blockHouseLightsCurrentlyOn, that.blockHouseLightsCurrentlyOn);
        }

        @Override
        public String toString() {
            return "SectionLight[" +
                    "daylight=" + Arrays.toString(daylight) + ", " +
                    "blockAllOff=" + Arrays.toString(blockAllOff) + ", " +
                    "blockHouseLightsCurrentlyOn=" + Arrays.toString(blockHouseLightsCurrentlyOn) + ']';
        }
    }
}
