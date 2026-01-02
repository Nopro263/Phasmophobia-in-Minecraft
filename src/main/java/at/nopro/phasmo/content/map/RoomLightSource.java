package at.nopro.phasmo.content.map;

import at.nopro.phasmo.game.RoomManager;
import at.nopro.phasmo.light.IngamePhasmoChunk;
import net.minestom.server.coordinate.Point;

public record RoomLightSource(Point point, RoomManager.Room room, IngamePhasmoChunk chunk) {
}
