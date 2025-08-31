package at.nopro.minestomTest.phasmo.ai;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.followers.GroundNodeFollower;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class NodeFollower extends GroundNodeFollower {
    private final PhasmoCreature creature;

    public NodeFollower(PhasmoCreature creature) {
        super(creature);
        this.creature = creature;
    }

    @Override
    public void moveTowards(@NotNull Point direction, double speed, @NotNull Point lookAt) {
        super.moveTowards(direction, speed, lookAt);

        Instance instance = this.creature.getInstance();

        Pos blockPos = this.creature.getPosition();
        Block block = instance.getBlock(blockPos);

        InteractionHandler handler = this.creature.getInteractionHandlerFor(blockPos, block);
        if(handler != null) {
            handler.interact(instance, blockPos, block);
        } else {
            blockPos = blockPos.add(blockPos.direction());
            block = instance.getBlock(blockPos);

            handler = this.creature.getInteractionHandlerFor(blockPos, block);
            if(handler != null) {
                handler.interact(instance, blockPos, block);
            }
        }

        Pos previousPosition = this.creature.getPreviousPosition();
        if(previousPosition.sameBlock(blockPos)) {
            return;
        }

        //System.out.println(previousPosition.asBlockVec() + " -> " + blockPos.asBlockVec());

        block = instance.getBlock(previousPosition);

        handler = this.creature.getInteractionHandlerFor(previousPosition, block);
        if(handler != null) {
            handler.interact(instance, previousPosition, block);
        }
    }
}
