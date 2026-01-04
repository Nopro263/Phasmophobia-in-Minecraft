package at.nopro.phasmo.event;

import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.coordinate.Pos;

public record PlaceEquipmentEvent(GameContext gameContext, Pos pos, Equipment equipment,
                                  ItemReference itemReference) implements PhasmoEvent {
}
