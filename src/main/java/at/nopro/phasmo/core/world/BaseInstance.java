package at.nopro.phasmo.core.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.world.DimensionType;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class BaseInstance extends InstanceContainer {
    private boolean readonly;

    public BaseInstance(DimensionType dimensionType) {
        super(UUID.randomUUID(), DimensionTypes.getKeyFor(dimensionType));
        MinecraftServer.getInstanceManager().registerInstance(this);
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
}
