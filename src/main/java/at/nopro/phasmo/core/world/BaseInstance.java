package at.nopro.phasmo.core.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.world.DimensionType;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BaseInstance extends InstanceContainer {
    private boolean readonly;
    private WorldMeta worldMeta;

    public BaseInstance(DimensionType dimensionType) {
        this(UUID.randomUUID(), dimensionType);
    }

    public BaseInstance(UUID uuid, DimensionType dimensionType) {
        super(uuid, DimensionTypes.getKeyFor(dimensionType));
        MinecraftServer.getInstanceManager().registerInstance(this);

        setGenerator((u) -> {
            u.modifier().fillHeight(0, 15, Block.DIRT);
            u.modifier().fillHeight(15, 16, Block.GRASS_BLOCK);
        });
    }

    @Override
    public boolean placeBlock(BlockHandler.@NonNull Placement placement, boolean doBlockUpdates) {
        if (isReadonly()) return false;
        return super.placeBlock(placement, doBlockUpdates);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public boolean breakBlock(@NonNull Player player, @NonNull Point blockPosition, @NonNull BlockFace blockFace, boolean doBlockUpdates) {
        if (isReadonly()) return false;
        return super.breakBlock(player, blockPosition, blockFace, doBlockUpdates);
    }

    public CompletableFuture<Void> save() {
        return CompletableFuture.allOf(
                saveChunksToStorage(),
                saveInstance()
        );
    }

    @Override
    public @NonNull CompletableFuture<Void> saveInstance() {
        if (isReadonly()) return CompletableFuture.completedFuture(null);
        return super.saveInstance();
    }

    @Override
    public @NonNull CompletableFuture<Void> saveChunksToStorage() {
        if (isReadonly()) return CompletableFuture.completedFuture(null);
        return super.saveChunksToStorage();
    }

    public WorldMeta getWorldMeta() {
        return worldMeta;
    }

    protected void setWorldMeta(WorldMeta worldMeta) {
        this.worldMeta = worldMeta;
    }

    public void onLoad() {
    }
}
