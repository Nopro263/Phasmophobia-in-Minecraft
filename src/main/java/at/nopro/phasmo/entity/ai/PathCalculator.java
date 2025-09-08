package at.nopro.phasmo.entity.ai;

import net.minestom.server.coordinate.Point;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class PathCalculator {
    public static Path calculate(NodeGenerator nodeGenerator, Point start, Point end) {
        PriorityQueue<Path.Node> open = new PriorityQueue<>();
        Set<Path.Node> closed = new HashSet<>();

        Path.Node startNode = new Path.Node(0,0, start);

        if(!nodeGenerator.isValid(startNode)) {
            return Path.INVALID;
        }

        open.add(startNode);

        while (!open.isEmpty()) {
            Path.Node current = open.poll();

            if(current.isAtPoint(end)) {
                return Path.fromEndNode(current);
            }

            closed.add(current);

            for(Path.Node node : nodeGenerator.getWalkable(current)) {
                if(closed.contains(node)) {
                    continue;
                }

                double new_g = current.g + 1;

                if (open.contains(node) && new_g >= node.g) {
                    continue;
                }

                node.parent = current;
                node.g = new_g;

                if (!open.contains(node)) {
                    open.add(node);
                }
            }
        }

        return Path.NOT_FOUND;
    }
}
