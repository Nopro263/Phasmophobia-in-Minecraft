package at.nopro.phasmo.content.map;

import at.nopro.phasmo.game.MapContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;

import java.util.List;

public class Maps {
    public static final MapContext TANGLEWOOD_DRIVE = new MapContext(
            "/home/noah/.local/share/multimc/instances/1.21.8_2/.minecraft/saves/6_tanglewood",
            17000,
            new Pos(22.0, -42, 2.0),
            new Pos(-15,-43,-12),
            new Pos(15,-41,15),
            List.of(-42),
            new Pos(22,-41,-2),
            Direction.WEST
    );
}
