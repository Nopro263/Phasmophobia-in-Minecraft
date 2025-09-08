package at.nopro.phasmo.entity.ai;

import net.minestom.server.coordinate.Point;

import java.util.ArrayDeque;
import java.util.Queue;

public class Path {
    public static final Path INVALID = new Path(State.INVALID, null);
    public static final Path NOT_FOUND = new Path(State.NOT_FOUND, null);

    private State state;
    private Queue<Node> nodes;

    public Path(State state, Queue<Node> nodes) {
        this.state = state;
        this.nodes = nodes;
    }

    public static Path fromEndNode(Node endNode) {
        ArrayDeque<Node> nodes = new ArrayDeque<>();

        Node current = endNode;
        while (current != null) {

            nodes.addFirst(current);

            current = current.parent;
        }

        return new Path(State.VALID, nodes);
    }

    public static class Node {
        public double f() {
            return g + h;
        }
        public double g;
        public double h;

        public int x;
        public int y;
        public int z;
        public Node parent;

        public Node(double g, double h, int x, int y, int z) {
            this.g = g;
            this.h = h;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Node(double g, double h, Point point) {
            this.g = g;
            this.h = h;
            this.x = point.blockX();
            this.y = point.blockY();
            this.z = point.blockZ();
        }

        public boolean isAtPoint(Point point) {
            return point.blockX() == x && point.blockY() == y && point.blockZ() == z;
        }
    }

    public enum State {
        INVALID,
        VALID,
        NOT_FOUND
    }
}
