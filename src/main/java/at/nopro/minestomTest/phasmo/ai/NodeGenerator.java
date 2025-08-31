package at.nopro.minestomTest.phasmo.ai;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.generators.PreciseGroundNodeGenerator;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.OptionalDouble;

public class NodeGenerator extends PreciseGroundNodeGenerator {
    private final PhasmoCreature creature;
    public NodeGenerator(PhasmoCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean canMoveTowards(Block.@NotNull Getter getter, @NotNull Point startOrg, @NotNull Point endOrg, @NotNull BoundingBox boundingBox) {
        try {
            return super.canMoveTowards(new MyGetter(getter), startOrg, endOrg, boundingBox);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public OptionalDouble gravitySnap(Block.Getter getter, double pointOrgX, double pointOrgY, double pointOrgZ, BoundingBox boundingBox, double maxFall) {
        try {
            return super.gravitySnap(new MyGetter(getter), pointOrgX, pointOrgY, pointOrgZ, boundingBox, maxFall);
        } catch (NullPointerException e) {
            return OptionalDouble.empty();
        }
    }

    private class MyGetter implements Block.Getter {
        private final Block.Getter original;

        public MyGetter(Block.Getter original) {
            this.original = original;
        }

        @Override
        public @UnknownNullability Block getBlock(int i, int i1, int i2, @NotNull Condition condition) {
            Block originalBlock = this.original.getBlock(i, i1, i2, condition);

            Pos blockPos = new Pos(i, i1, i2);
            InteractionHandler handler = creature.getInteractionHandlerFor(blockPos, originalBlock);
            if(handler == null) {
                return originalBlock;
            }
            return handler.getInteractedBlock(blockPos, originalBlock);
        }
    }
}
