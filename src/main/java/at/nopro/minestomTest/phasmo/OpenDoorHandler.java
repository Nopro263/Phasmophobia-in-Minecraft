package at.nopro.minestomTest.phasmo;

import at.nopro.minestomTest.phasmo.ai.InteractionHandler;
import at.nopro.minestomTest.phasmo.ai.PhasmoCreature;
import at.nopro.minestomTest.phasmo.utils.DoorUtils;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OpenDoorHandler extends InteractionHandler {
    private final GhostCreature creature;

    public OpenDoorHandler(GhostCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean canInteract(Pos blockPos, Block block) {
        return Arrays.stream(this.creature.getMapMeta().doors).anyMatch((b) -> b.key().equals(block.key()) && !DoorUtils.getOpen(block));
    }

    @Override
    public Block getInteractedBlock(Pos blockPos, Block block) {
        return DoorUtils.setOpen(block, true);
    }

    @Override
    public void interact(Instance instance, Pos blockPos, Block block) {
        if(!DoorUtils.getOpen(block)) {
            DoorUtils.toggleDoubleDoor(instance, blockPos);
        }
    }
}
