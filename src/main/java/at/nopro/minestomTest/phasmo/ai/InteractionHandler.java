package at.nopro.minestomTest.phasmo.ai;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public abstract class InteractionHandler {
    /**
     * */
    public abstract boolean canInteract(Pos blockPos, Block block);

    /**
     * */
    public void interact(Instance instance, Pos blockPos, Block block) {
        instance.setBlock(blockPos, getInteractedBlock(blockPos, block));
    }

    /**
     * */
    public abstract Block getInteractedBlock(Pos blockPos, Block block);
}
