package at.nopro.old_phasmo.event;

import at.nopro.old_phasmo.content.equipment.Equipment;
import at.nopro.old_phasmo.game.GameContext;
import at.nopro.old_phasmo.game.ItemReference;
import net.minestom.server.coordinate.Pos;

public record PlaceEquipmentEvent(GameContext gameContext, Pos pos, Equipment equipment,
                                  ItemReference itemReference) implements PhasmoEvent {
}
