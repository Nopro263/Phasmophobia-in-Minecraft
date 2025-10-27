package at.nopro.phasmo.event;

import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.coordinate.Pos;

public class PlaceEquipmentEvent implements PhasmoEvent {
    private final GameContext gameContext;
    private final Pos pos;
    private final Equipment equipment;
    private final ItemReference itemReference;

    public PlaceEquipmentEvent(GameContext gameContext, Pos pos, Equipment equipment, ItemReference itemReference) {
        this.gameContext = gameContext;
        this.pos = pos;
        this.equipment = equipment;
        this.itemReference = itemReference;
    }

    @Override
    public GameContext getGameContext() {
        return gameContext;
    }

    public Pos getPos() {
        return pos;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public ItemReference getItemReference() {
        return itemReference;
    }
}
