package at.nopro.phasmo.game;

import at.nopro.entityLoader.EntityLoader;
import at.nopro.minestomTest.phasmo.MapMeta;
import at.nopro.phasmo.entity.ai.PathCache;
import at.nopro.phasmo.entity.ai.PhasmoEntity;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;

public class GameContext {
    private final MapContext mapContext;
    private InstanceContainer instance;
    private PathCache pathCache;
    public PhasmoEntity entity;

    public GameContext(MapContext mapContext) {
        this.mapContext = mapContext;
        this.load();
    }

    private void load() {
        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(new EntityLoader(mapContext.worldPath()));
        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);

        instance.setTime(mapContext.time());

        int cx1 = CoordConversion.globalToChunk(mapContext.lowerEnd().x());
        int cz1 = CoordConversion.globalToChunk(mapContext.lowerEnd().z());

        int cx2 = CoordConversion.globalToChunk(mapContext.upperEnd().x());
        int cz2 = CoordConversion.globalToChunk(mapContext.upperEnd().z());

        long start = System.currentTimeMillis();

        for (int i = cx1; i <= cx2; i++) {
            for (int j = cz1; j <= cz2; j++) {
                instance.loadChunk(i,j).join();
            }
        }

        System.out.println("Loaded chunks in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();

        pathCache = PathCache.compute(
                (short) mapContext.lowerEnd().blockX(),
                (short) mapContext.lowerEnd().blockY(),
                (short) mapContext.lowerEnd().blockZ(),
                (short) mapContext.upperEnd().blockX(),
                (short) mapContext.upperEnd().blockY(),
                (short) mapContext.upperEnd().blockZ(),
                instance
        );

        System.out.println("Generated pathfinding map in " + (System.currentTimeMillis() - start) + "ms");

        this.entity = new PhasmoEntity(EntityType.SKELETON, this);
        this.entity.setInstance(instance, new Pos(-8, -42, 3));
    }

    public MapContext getMapContext() {
        return mapContext;
    }

    public Instance getInstance() {
        return instance;
    }

    public PathCache getPathCache() {
        return pathCache;
    }
}
