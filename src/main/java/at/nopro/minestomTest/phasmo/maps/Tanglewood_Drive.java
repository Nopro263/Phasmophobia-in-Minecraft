package at.nopro.minestomTest.phasmo.maps;

import at.nopro.minestomTest.phasmo.MapMeta;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public class Tanglewood_Drive extends MapMeta {
    public Tanglewood_Drive() {
        super(new Pos(-13,-42,-9), new Pos(14,-42,14), new Block[]{Block.LIGHT_GRAY_CONCRETE}, new Block[]{ Block.DARK_OAK_DOOR});
    }
}
