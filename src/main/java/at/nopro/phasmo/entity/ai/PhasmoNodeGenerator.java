package at.nopro.phasmo.entity.ai;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.block.Block;

import java.util.*;

public class PhasmoNodeGenerator implements NodeGenerator {
    private final PathCache pathCache;

    public PhasmoNodeGenerator(PathCache pathCache) {
        this.pathCache = pathCache;
    }

    @Override
    public Collection<? extends PNode> getWalkable(Block.Getter getter, Set<PNode> visited, PNode current, Point goal, BoundingBox boundingBox) {
        List<PNode> list = new ArrayList<>();
        for (byte l = -1; l <= 1; l++) {
            for (byte m = -1; m <= 1; m++) {
                for (byte n = -1; n <= 1; n++) {
                    if (pathCache.canMoveTo(
                            (short) current.blockX(),
                            (short) current.blockY(),
                            (short) current.blockZ(),
                            l, m, n)) {
                        PNode.Type type;
                        if (m == 0) {
                            type = PNode.Type.WALK;
                        } else if (m == -1) {
                            type = PNode.Type.FALL;
                        } else {
                            type = PNode.Type.JUMP;
                        }

                        Point newPos = new Pos(
                                current.blockX() + l + 0.5,
                                current.y() + m,
                                current.blockZ() + n + 0.5
                        );

                        list.add(new PNode(
                                newPos,
                                current.g() + 1,
                                this.heuristic(newPos, goal),
                                type,
                                current
                        ));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasGravitySnap() {
        return false;
    }

    @Override
    public OptionalDouble gravitySnap(Block.Getter getter, double x, double y, double z, BoundingBox boundingBox, double maxFall) {
        return OptionalDouble.empty();
    }

    @Override
    public boolean canMoveTowards(Block.Getter getter, Point start, Point end, BoundingBox boundingBox) {
        return pathCache.canMoveTo(
                (short) start.blockX(),
                (short) start.blockY(),
                (short) start.blockZ(),
                (byte) ( end.blockX() - start.blockX() ),
                (byte) ( end.blockY() - start.blockY() ),
                (byte) ( end.blockX() - start.blockX() )
        );
    }

    @Override
    public boolean pointInvalid(Block.Getter getter, Point point, BoundingBox boundingBox) {
        return pathCache.isInvalid(
                (short) point.blockX(),
                (short) point.blockY(),
                (short) point.blockZ()
        );
    }

    @Override
    public double heuristic(Point node, Point target) {
        return NodeGenerator.super.heuristic(node, target);
    }
}
