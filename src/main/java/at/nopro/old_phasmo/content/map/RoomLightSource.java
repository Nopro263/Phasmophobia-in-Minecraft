package at.nopro.old_phasmo.content.map;

import at.nopro.old_phasmo.game.RoomManager;
import at.nopro.old_phasmo.light.IngamePhasmoChunk;
import net.minestom.server.coordinate.Point;

public record RoomLightSource(Point point, RoomManager.Room room, IngamePhasmoChunk chunk) {
}
