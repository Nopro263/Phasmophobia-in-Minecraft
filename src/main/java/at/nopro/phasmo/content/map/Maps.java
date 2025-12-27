package at.nopro.phasmo.content.map;

import at.nopro.phasmo.game.MapContext;
import at.nopro.phasmo.light.VanLightSource;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;

import java.util.List;

public class Maps {
    public static final MapContext TANGLEWOOD_DRIVE = new MapContext(
            "maps/6_tanglewood",
            17000,
            new Pos(22.0, -42, 2.0),
            new Pos(-15, -46, -12),
            new Pos(15, -41, 15),
            List.of(-42, -46),
            new Pos(22, -41, -2),
            Direction.WEST,
            new VanLightSource(
                    new Pos(20, -42, -2),
                    new Pos(23, -40, 6)
            )
    );
}
