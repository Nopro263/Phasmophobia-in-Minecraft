package at.nopro.phasmo.game;

import at.nopro.entityLoader.EntityLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;

public class GameContext {
    private final MapContext mapContext;
    private InstanceContainer instance;

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
    }

    public MapContext getMapContext() {
        return mapContext;
    }

    public Instance getInstance() {
        return instance;
    }
}
