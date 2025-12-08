package at.nopro.phasmo.lightingv2;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.light.LightCompute;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class PhasmoChunk extends LightingChunk {
    private static final ExecutorService pool = Executors.newWorkStealingPool();
    private static final Set<Key> DIFFUSE_SKY_LIGHT;

    static {
        DIFFUSE_SKY_LIGHT = Set.of(Block.COBWEB.key(), Block.ICE.key(), Block.HONEY_BLOCK.key(), Block.SLIME_BLOCK.key(), Block.WATER.key(), Block.ACACIA_LEAVES.key(), Block.AZALEA_LEAVES.key(), Block.BIRCH_LEAVES.key(), Block.DARK_OAK_LEAVES.key(), Block.FLOWERING_AZALEA_LEAVES.key(), Block.JUNGLE_LEAVES.key(), Block.CHERRY_LEAVES.key(), Block.OAK_LEAVES.key(), Block.SPRUCE_LEAVES.key(), Block.SPAWNER.key(), Block.BEACON.key(), Block.END_GATEWAY.key(), Block.CHORUS_PLANT.key(), Block.CHORUS_FLOWER.key(), Block.FROSTED_ICE.key(), Block.SEAGRASS.key(), Block.TALL_SEAGRASS.key(), Block.LAVA.key());
    }

    private final ReentrantLock packetGenerationLock = new ReentrantLock();
    private LightData partialLightData;
    private LightData fullLightData;
    private int highestBlock;
    private int[] occlusionMap;

    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    /*
     * Calls calculateInternal and calculateExternal on Light
     * Calls Light.getNeighbors for calculateExternal
     * */
    private static Set<Chunk> flushQueue(Instance instance, Set<Point> queue, LightType type, QueueType queueType) {
        Set<Light> sections = ConcurrentHashMap.newKeySet();
        Set<Point> newQueue = ConcurrentHashMap.newKeySet();
        Set<Chunk> responseChunks = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> tasks = new ArrayList();
        Light.LightLookup lightLookup = (x, y, z) -> {
            Chunk chunk = instance.getChunk(x, z);
            if (chunk == null) {
                return null;
            } else if (chunk instanceof PhasmoChunk lighting) {
                if (y - lighting.getMinSection() >= 0 && y - lighting.getMaxSection() < 0) {
                    Section section = lighting.getSection(y);
                    Light var10000;
                    switch (type.ordinal()) {
                        case 0 -> var10000 = section.skyLight();
                        case 1 -> var10000 = section.blockLight();
                        default -> throw new MatchException(null, null);
                    }

                    return var10000;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        };
        Light.PaletteLookup paletteLookup = (x, y, z) -> {
            Chunk chunk = instance.getChunk(x, z);
            if (chunk == null) {
                return null;
            } else if (chunk instanceof PhasmoChunk lighting) {
                return y - lighting.getMinSection() >= 0 && y - lighting.getMaxSection() < 0 ? chunk.getSection(y).blockPalette() : null;
            } else {
                return null;
            }
        };

        for (Point point : queue) {
            Chunk chunk = instance.getChunk(point.blockX(), point.blockZ());
            if (chunk instanceof PhasmoChunk lightingChunk) {
                Section section = chunk.getSection(point.blockY());
                responseChunks.add(chunk);
                Light var10000;
                switch (type.ordinal()) {
                    case 0 -> var10000 = section.skyLight();
                    case 1 -> var10000 = section.blockLight();
                    default -> throw new MatchException(null, null);
                }

                Light light = var10000;
                Palette blockPalette = section.blockPalette();
                CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                    try {
                        Set<Point> toAdd;
                        switch (queueType) {
                            case INTERNAL ->
                                    toAdd = LightInterceptor.calculateInternal(light, blockPalette, chunk.getChunkX(), point.blockY(), chunk.getChunkZ(), lightingChunk.getOcclusionMap(), chunk.getInstance().getCachedDimensionType().maxY(), lightLookup);
                            case EXTERNAL ->
                                    toAdd = LightInterceptor.calculateExternal(light, blockPalette, Light.getNeighbors(chunk, point.blockY()), lightLookup, paletteLookup);
                            default -> throw new RuntimeException("");
                        }


                        sections.add(light);
                        light.flip();
                        newQueue.addAll(toAdd);
                    } catch (Exception e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }

                }, pool);
                tasks.add(task);
            }
        }

        tasks.forEach(CompletableFuture::join);
        if (!newQueue.isEmpty()) {
            Set<Chunk> newResponse = flushQueue(instance, newQueue, type, QueueType.EXTERNAL);
            responseChunks.addAll(newResponse);
        }

        return responseChunks;
    }

    public static List<Chunk> relight(Instance instance, Collection<Chunk> chunks) {
        Set<Point> sections = new HashSet();
        synchronized (instance) {
            for (Chunk chunk : chunks) {
                if (chunk instanceof PhasmoChunk lighting) {
                    for (int sectionIndex = lighting.getMinSection(); sectionIndex < lighting.getMaxSection(); ++sectionIndex) {
                        Section section = chunk.getSection(sectionIndex);
                        section.blockLight().invalidate();
                        section.skyLight().invalidate();
                        sections.add(new Vec(chunk.getChunkX(), sectionIndex, chunk.getChunkZ()));
                    }

                    lighting.invalidate();
                }
            }

            HashSet<Point> blockSections = new HashSet();

            for (Point point : sections) {
                blockSections.addAll(getNearbyRequired(instance, point, LightType.BLOCK));
            }

            HashSet<Point> skySections = new HashSet();

            for (Point point : sections) {
                skySections.addAll(getNearbyRequired(instance, point, LightType.SKY));
            }

            relight(instance, blockSections, LightType.BLOCK);
            relight(instance, skySections, LightType.SKY);
            HashSet<Chunk> chunksToRelight = new HashSet();

            for (Point point : blockSections) {
                chunksToRelight.add(instance.getChunk(point.blockX(), point.blockZ()));
            }

            for (Point point : skySections) {
                chunksToRelight.add(instance.getChunk(point.blockX(), point.blockZ()));
            }

            return new ArrayList(chunksToRelight);
        }
    }

    private static Set<Point> getNearbyRequired(Instance instance, Point point, LightType type) {
        Set<Point> collected = new HashSet();
        collected.add(point);
        int highestRegionPoint = instance.getCachedDimensionType().minY() - 1;

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; ++x) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; ++z) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck instanceof PhasmoChunk lighting) {
                    lighting.getOcclusionMap();
                    highestRegionPoint = Math.max(highestRegionPoint, lighting.highestBlock);
                }
            }
        }

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; ++x) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; ++z) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck != null) {
                    for (int y = point.blockY() - 1; y <= point.blockY() + 1; ++y) {
                        Point sectionPosition = new Vec(x, y, z);
                        int sectionHeight = instance.getCachedDimensionType().minY() + 16 * y;
                        if (( sectionHeight + 16 <= highestRegionPoint || type != LightType.SKY ) && sectionPosition.blockY() < chunkCheck.getMaxSection() && sectionPosition.blockY() >= chunkCheck.getMinSection()) {
                            Section s = chunkCheck.getSection(sectionPosition.blockY());
                            if (( type != LightType.BLOCK || s.blockLight().requiresUpdate() ) && ( type != LightType.SKY || s.skyLight().requiresUpdate() )) {
                                collected.add(sectionPosition);
                            }
                        }
                    }
                }
            }
        }

        return collected;
    }

    private static Set<Point> collectRequiredNearby(Instance instance, Point point, LightType type) {
        Set<Point> found = new HashSet();
        ArrayDeque<Point> toCheck = new ArrayDeque();
        toCheck.add(point);
        found.add(point);

        while (!toCheck.isEmpty()) {
            Point current = toCheck.poll();
            Set<Point> nearby = getNearbyRequired(instance, current, type);
            nearby.forEach((p) -> {
                if (!found.contains(p)) {
                    found.add(p);
                    toCheck.add(p);
                }

            });
        }

        return found;
    }


    private static Set<Chunk> relightSection(Instance instance, int chunkX, int sectionY, int chunkZ, LightType type) {
        Chunk c = instance.getChunk(chunkX, chunkZ);
        if (c == null) {
            return Set.of();
        } else if (!( c instanceof PhasmoChunk )) {
            return Set.of();
        } else {
            synchronized (instance) {
                Set<Point> collected = collectRequiredNearby(instance, new Vec(chunkX, sectionY, chunkZ), type);
                return relight(instance, collected, type);
            }
        }
    }

    private static Set<Chunk> relight(Instance instance, Set<Point> queue, LightType type) {
        return flushQueue(instance, queue, type, QueueType.INTERNAL);
    }

    private boolean checkSkyOcclusion(Block block) {
        if (block == Block.AIR) {
            return false;
        } else if (DIFFUSE_SKY_LIGHT.contains(block.key())) {
            return true;
        } else {
            Shape shape = block.registry().occlusionShape();
            boolean occludesTop = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.TOP);
            boolean occludesBottom = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.BOTTOM);
            return occludesBottom || occludesTop;
        }
    }

    @Override
    public int[] getOcclusionMap() {
        if (this.occlusionMap != null) {
            return this.occlusionMap;
        } else {
            int[] occlusionMap = new int[256];
            int minY = this.instance.getCachedDimensionType().minY();
            this.highestBlock = minY - 1;
            synchronized (this) {
                int startY = Heightmap.getHighestBlockSection(this);

                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        int height;
                        for (height = startY; height >= minY; --height) {
                            Block block = this.getBlock(x, height, z, Condition.TYPE);
                            if (block != Block.AIR) {
                                this.highestBlock = Math.max(this.highestBlock, height);
                            }

                            if (this.checkSkyOcclusion(block)) {
                                break;
                            }
                        }

                        occlusionMap[z << 4 | x] = height + 1;
                    }
                }
            }

            this.occlusionMap = occlusionMap;
            return occlusionMap;
        }
    }

    /*
     * Checks what sections have to be updated and calls relightSection on them
     * */
    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        this.packetGenerationLock.lock();

        try {
            if (requiredFullChunk) {
                if (this.fullLightData != null) {
                    return this.fullLightData;
                }
            } else if (this.partialLightData != null) {
                return this.partialLightData;
            }

            BitSet skyMask = new BitSet();
            BitSet blockMask = new BitSet();
            BitSet emptySkyMask = new BitSet();
            BitSet emptyBlockMask = new BitSet();
            List<byte[]> skyLights = new ArrayList();
            List<byte[]> blockLights = new ArrayList();
            int chunkMin = this.instance.getCachedDimensionType().minY();
            int highestNeighborBlock = this.instance.getCachedDimensionType().minY();

            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    Chunk neighborChunk = this.instance.getChunk(this.chunkX + i, this.chunkZ + j);
                    if (neighborChunk instanceof PhasmoChunk light) {
                        light.getOcclusionMap();
                        highestNeighborBlock = Math.max(highestNeighborBlock, light.highestBlock);
                    }
                }
            }

            int index = 0;

            for (Section section : this.sections) {
                boolean wasUpdatedBlock = false;
                boolean wasUpdatedSky = false;
                if (section.blockLight().requiresUpdate()) {
                    relightSection(this.instance, this.chunkX, index + this.minSection, this.chunkZ, LightType.BLOCK);
                    wasUpdatedBlock = true;
                } else if (requiredFullChunk || section.blockLight().requiresSend()) {
                    wasUpdatedBlock = true;
                }

                if (section.skyLight().requiresUpdate()) {
                    relightSection(this.instance, this.chunkX, index + this.minSection, this.chunkZ, LightType.SKY);
                    wasUpdatedSky = true;
                } else if (requiredFullChunk || section.skyLight().requiresSend()) {
                    wasUpdatedSky = true;
                }

                int sectionMinY = index * 16 + chunkMin;
                ++index;
                if (wasUpdatedSky && this.instance.getCachedDimensionType().hasSkylight() && sectionMinY <= highestNeighborBlock + 16) {
                    byte[] skyLight = section.skyLight().array();
                    if (skyLight.length != 0 && skyLight != LightCompute.EMPTY_CONTENT) {
                        skyLights.add(skyLight);
                        skyMask.set(index);
                    } else {
                        emptySkyMask.set(index);
                    }
                }

                if (wasUpdatedBlock) {
                    byte[] blockLight = section.blockLight().array();
                    if (blockLight.length != 0 && blockLight != LightCompute.EMPTY_CONTENT) {
                        blockLights.add(blockLight);
                        blockMask.set(index);
                    } else {
                        emptyBlockMask.set(index);
                    }
                }
            }

            LightData lightData = new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
            if (requiredFullChunk) {
                this.fullLightData = lightData;
            } else {
                this.partialLightData = lightData;
            }

            LightData var26 = lightData;
            return var26;
        } finally {
            this.packetGenerationLock.unlock();
        }
    }

    enum LightType {
        SKY,
        BLOCK
    }

    private enum QueueType {
        INTERNAL,
        EXTERNAL
    }
}
