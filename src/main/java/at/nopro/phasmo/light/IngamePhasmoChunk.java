package at.nopro.phasmo.light;

import at.nopro.phasmo.content.map.RoomLightSource;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IngamePhasmoChunk extends DynamicChunk {
    boolean toggle;
    private final SectionLight[] sectionLights;
    private final CachedPacket cachedLightPacket = new CachedPacket(this::createLightPacket);
    private final List<RoomLightSource> roomLightSources = new ArrayList<>();

    public IngamePhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);

        this.sectionLights = new SectionLight[getMaxSection() - getMinSection() + 2];

        for (int i = 0; i < sections.size() + 2; i++) {
            this.sectionLights[i] = new SectionLight();
        }
    }

    @Override
    public @NotNull LightData createLightData(boolean requiredFullChunk) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        //System.out.print("Chunk[" + chunkX + "," + chunkZ + "] started light");
        //long start = System.currentTimeMillis();

        for (int i = -1; i < sections.size() + 1; i++) {
            SectionLight sectionLight = getSectionLight(i);

            //recalculate light

            skyMask.set(i + 1);
            skyLights.add(sectionLight.getDaylight().getLightData());

            blockMask.set(i + 1);
            blockLights.add(LightCompute.bake(
                    sectionLight.getBlockAllOff().getLightData(),
                    sectionLight.getBlockHouseLightsCurrentlyOn().getLightData(),
                    sectionLight.getBlockDynamic().getLightData()
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

    public void clearLight() {
        for (SectionLight light : sectionLights) {
            light.clear();
        }
    }

    private UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    public void calculateInitialLight() {
        calculateSkyLight();
        calculateVanLight();
        calculateRoomLight();
    }

    private SectionLight getSectionLight(int sectionIndex) {
        return this.sectionLights[sectionIndex + 1];
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

        Set<LightCompute.SectionPos> sectionsToResend = new HashSet<>();

        for (int i = 0; i < sections.size(); i++) {
            int sectionMinY = i * 16 + minY;

            int finalI = i;
            tasks.add(CompletableFuture.runAsync(() -> {
                SectionLight sectionLight = getSectionLight(finalI);
                Palette blockPalette = sections.get(finalI).blockPalette();

                sectionsToResend.addAll(LightCompute.computeSectionVanLight(
                        sectionLight.getBlockAllOff().getLightData(),
                        10,
                        sectionMinY,
                        chunkMinX,
                        chunkMinZ,
                        blockPalette,
                        vanLightSource,
                        (x, y, z) -> {
                            Chunk chunk = phasmoInstance.getChunkAt(x, z);
                            if (chunk == null) return null;
                            return Objects.requireNonNullElse(chunk.getBlock(x, y, z), Block.AIR);
                        },
                        (x, y, z) -> {
                            if (phasmoInstance.getChunkAt(x, z) == null) {
                                phasmoInstance.loadChunk(x, z).join();
                            }
                            if (!( phasmoInstance.getChunkAt(x, z) instanceof IngamePhasmoChunk phasmoChunk ))
                                return null;
                            return phasmoChunk.getSectionLight(( y - minY ) >> 4).getBlockAllOff();
                        },
                        (x, y, z) -> {
                            if (phasmoInstance.getChunkAt(x, z) == null) {
                                phasmoInstance.loadChunk(x, z).join();
                            }
                            if (!( phasmoInstance.getChunkAt(x, z) instanceof IngamePhasmoChunk phasmoChunk ))
                                return null;


                            return phasmoChunk.getSection(y >> 4).blockPalette();
                        }
                ));
            }));
        }


        for (CompletableFuture<Void> task : tasks) {
            task.join();
        }

        for (LightCompute.SectionPos sectionPos : sectionsToResend) {
            if (!( phasmoInstance.getChunk(sectionPos.sectionX(), sectionPos.sectionZ()) instanceof IngamePhasmoChunk phasmoChunk )) {
                throw new RuntimeException("uuuuuh");
            }

            phasmoChunk.invalidate();
            phasmoChunk.resendLight();
        }
    }

    public void calculateSkyLight() {
        int minY = instance.getCachedDimensionType().minY();

        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        for (int i = -1; i < sections.size() + 1; i++) {
            int sectionMinY = i * 16 + minY;

            int finalI = i;
            tasks.add(CompletableFuture.runAsync(() -> {
                SectionLight sectionLight = getSectionLight(finalI);

                LightCompute.computeSectionSkyLight(
                        sectionLight.getDaylight().getLightData(),
                        motionBlockingHeightmap(),
                        15,
                        sectionMinY
                );
            }));
        }
    }

    public void calculateRoomLight() {
        int minY = instance.getCachedDimensionType().minY();
        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;

        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        if (!( instance instanceof PhasmoInstance phasmoInstance )) {
            return;
        }

        Set<LightCompute.SectionPos> sectionsToResend = new HashSet<>();

        List<Point> lightSources = roomLightSources.stream().map(RoomLightSource::point).toList();

        for (int i = 0; i < sections.size(); i++) {
            int sectionMinY = i * 16 + minY;

            int finalI = i;
            tasks.add(CompletableFuture.runAsync(() -> {
                SectionLight sectionLight = getSectionLight(finalI);
                Palette blockPalette = sections.get(finalI).blockPalette();

                sectionsToResend.addAll(LightCompute.computeSectionRoomLight(
                        sectionLight.getBlockHouseLightsCurrentlyOn().getLightData(),
                        7,
                        sectionMinY,
                        chunkMinX,
                        chunkMinZ,
                        blockPalette,
                        lightSources,
                        (x, y, z) -> {
                            Chunk chunk = phasmoInstance.getChunkAt(x, z);
                            if (chunk == null) return null;
                            return Objects.requireNonNullElse(chunk.getBlock(x, y, z), Block.AIR);
                        },
                        (x, y, z) -> {
                            if (phasmoInstance.getChunkAt(x, z) == null) {
                                phasmoInstance.loadChunk(x, z).join();
                            }
                            if (!( phasmoInstance.getChunkAt(x, z) instanceof IngamePhasmoChunk phasmoChunk ))
                                return null;
                            return phasmoChunk.getSectionLight(( y - minY ) >> 4).getBlockHouseLightsCurrentlyOn();
                        },
                        (x, y, z) -> {
                            if (phasmoInstance.getChunkAt(x, z) == null) {
                                phasmoInstance.loadChunk(x, z).join();
                            }
                            if (!( phasmoInstance.getChunkAt(x, z) instanceof IngamePhasmoChunk phasmoChunk ))
                                return null;


                            return phasmoChunk.getSection(y >> 4).blockPalette();
                        }
                ));
            }));
        }


        for (CompletableFuture<Void> task : tasks) {
            task.join();
        }

        for (LightCompute.SectionPos sectionPos : sectionsToResend) {
            if (!( phasmoInstance.getChunk(sectionPos.sectionX(), sectionPos.sectionZ()) instanceof IngamePhasmoChunk phasmoChunk )) {
                throw new RuntimeException("uuuuuh");
            }

            phasmoChunk.invalidate();
            phasmoChunk.resendLight();
        }
    }

    @ApiStatus.Internal
    public void addRoomLightSource(RoomLightSource roomLightSource) {
        roomLightSources.add(roomLightSource);
    }

    public void resendLight() {
        if (this.isLoaded()) {
            this.sendPacketToViewers(cachedLightPacket);
        }
    }

    public void resendLightTo(Player player) {
        if (this.isLoaded()) {
            player.sendPacket(cachedLightPacket);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        cachedLightPacket.invalidate();
    }

    private static final class SectionLight {
        private final Light daylight;
        private final Light blockAllOff;
        private final Light blockDynamic;
        private final Light blockHouseLightsCurrentlyOn;

        private boolean blockHouseLightsCurrentlyOnToggle = false;

        private SectionLight() {
            daylight = new Light();
            blockAllOff = new Light();
            blockDynamic = new Light();
            blockHouseLightsCurrentlyOn = new Light();
        }

        public Light getDaylight() {
            return daylight;
        }

        public Light getBlockAllOff() {
            return blockAllOff;
        }

        public Light getBlockDynamic() {
            return blockDynamic;
        }

        public Light getBlockHouseLightsCurrentlyOn() {
            return blockHouseLightsCurrentlyOn;
        }

        public boolean isBlockHouseLightsCurrentlyOn() { //TODO
            return blockHouseLightsCurrentlyOnToggle;
        }

        public void setBlockHouseLightsCurrentlyOn(boolean blockHouseLightsCurrentlyOn) { //Breaker status
            this.blockHouseLightsCurrentlyOnToggle = blockHouseLightsCurrentlyOn;
        }

        public void clear() {
            daylight.clear();
            blockAllOff.clear();
            blockDynamic.clear();
            blockHouseLightsCurrentlyOn.clear();
        }
    }
}
