package at.nopro.minestomTest.phasmo.utils;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

public class DoorUtils {
    private DoorUtils() {}

    public static void toggleSingleDoor(Instance instance, Point pos) {
        Block first = instance.getBlock(pos);

        if(!isDoor(first)) {
            throw new RuntimeException("not a door");
        }

        Block second;
        Point secondPos;
        boolean open = !getOpen(first);

        if(getHalf(first) == Half.lower) {
            secondPos = pos.add(0,1,0);
        } else {
            secondPos = pos.add(0, -1, 0);
        }
        second = instance.getBlock(secondPos);

        instance.setBlock(pos, setOpen(first, open));
        instance.setBlock(secondPos, setOpen(second, open));
    }

    public static void toggleDoubleDoor(Instance instance, Point pos) {
        Block first = instance.getBlock(pos);

        if(!isDoor(first)) {
            throw new RuntimeException("not a door");
        }

        Direction direction = getFacing(first);
        Direction nextDoorDirection = nextDoorDirection(direction, getHinge(first));
        Point nextDoorPos = pos.add(nextDoorDirection.vec());

        toggleSingleDoor(instance, pos);
        if(isDoor(instance.getBlock(nextDoorPos))) {
            toggleSingleDoor(instance, nextDoorPos);
        }
    }

    public static boolean isDoor(Block block) {
        String p = block.getProperty("open");
        return p != null;
    }

    public static boolean getOpen(Block block) {
        String p = block.getProperty("open");
        if(!isDoor(block)) {
            throw new RuntimeException("not a door");
        }
        return "true".equals(p);
    }

    public static Half getHalf(Block block) {
        return get(Half.class, "half", block);
    }

    public static Hinge getHinge(Block block) {
        return get(Hinge.class, "hinge", block);
    }

    public static Direction getFacing(Block block) {
        String p = block.getProperty("facing");
        if(!isDoor(block)) {
            throw new RuntimeException("not a door");
        }
        return Direction.valueOf(p.toUpperCase());
    }

    public static Block setOpen(Block block, boolean open) {
        if(!isDoor(block)) {
            throw new RuntimeException("not a door");
        }
        return block.withProperty("open", open + "");
    }

    public static Block setHalf(Block block, Half half) {
        return set(Half.class, "half", half, block);
    }

    public static Block setHinge(Block block, Hinge hinge) {
        return set(Hinge.class, "hinge", hinge, block);
    }

    public static Block setFacing(Block block, Direction facing) {
        if(!isDoor(block)) {
            throw new RuntimeException("not a door");
        }
        return block.withProperty("facing", facing.name().toLowerCase());
    }


    private static <T extends Enum<T>> T get(Class<T> tEnum, String property, Block block) {
        if(!isDoor(block)) {
            throw new RuntimeException("not a door");
        }
        return Enum.valueOf(tEnum, block.getProperty(property));
    }

    private static <T extends Enum<T>> Block set(Class<T> tEnum, String property, T value, Block block) {
        if(!isDoor(block)) {
            throw new RuntimeException("not a door");
        }
        return block.withProperty(property, value.name());
    }

    public enum Half {
        upper,
        lower
    }

    public enum Hinge {
        left,
        right
    }

    private static Direction nextDoorDirection(Direction doorDirection, Hinge hinge) {
        Direction direction = switch (doorDirection) {
            case DOWN, UP -> null;
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
        };

        assert direction != null;

        if(hinge == Hinge.right) {
            direction = direction.opposite();
        }
        return direction;
    }
}
