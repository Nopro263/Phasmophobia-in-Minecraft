package at.nopro.phasmo.gameplay.editor;

import at.nopro.phasmo.core.world.BaseInstance;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerPickBlockEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.world.DimensionType;

public class EditorInstance extends BaseInstance {
    public EditorInstance(DimensionType dimensionType) {
        super(dimensionType);
        setReadonly(false);

        setChunkSupplier(LightingChunk::new);

        eventNode().addListener(PlayerSpawnEvent.class, this::onPlayerSpawn);
        eventNode().addListener(PlayerPickBlockEvent.class, this::onBlockPick);
    }

    private void onPlayerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        playerSpawnEvent.getPlayer().setGameMode(GameMode.CREATIVE);
        playerSpawnEvent.getPlayer().teleport(new Pos(0, 17, 0));
    }

    private void onBlockPick(PlayerPickBlockEvent pickBlockEvent) {
        pickBlockEvent.getPlayer().getInventory().addItemStack(ItemStack.of(pickBlockEvent.getBlock().registry().material()));
    }
}
