package at.nopro.minestomTest.ext;

import at.nopro.minestomTest.phasmo.MapMeta;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReadonlyInstanceView extends SharedInstance {
    public ReadonlyInstanceView(UUID uuid, InstanceContainer instanceContainer) {
        super(uuid, instanceContainer);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block, boolean doBlockUpdates) {
        super.setBlock(x,y,z,block,doBlockUpdates);
    }

    @Override
    public boolean placeBlock(BlockHandler.@NotNull Placement placement, boolean doBlockUpdates) {
        return false;
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace, boolean doBlockUpdates) {
        return false;
    }

    @Override
    public CompletableFuture<Void> saveChunksToStorage() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> saveChunkToStorage(Chunk chunk) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> saveInstance() {
        return CompletableFuture.completedFuture(null);
    }

    public void loadAll(MapMeta mapMeta) {
        int cx1 = CoordConversion.globalToChunk(mapMeta.corner1.x());
        int cz1 = CoordConversion.globalToChunk(mapMeta.corner1.z());

        int cx2 = CoordConversion.globalToChunk(mapMeta.corner2.x());
        int cz2 = CoordConversion.globalToChunk(mapMeta.corner2.z());

        for (int i = cx1; i <= cx2; i++) {
            for (int j = cz1; j <= cz2; j++) {
                loadOptionalChunk(i,j);
            }
        }
    }
}
