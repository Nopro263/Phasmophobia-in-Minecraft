package at.nopro.minestomTest.phasmo;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public abstract class MapMeta {
    public final Pos corner1;
    public final Pos corner2;
    public final Block[] floor;
    public final Block[] doors;

    public MapMeta(Pos corner1, Pos corner2, Block[] floor, Block[] doors) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.floor = floor;
        this.doors = doors;
    }
}
