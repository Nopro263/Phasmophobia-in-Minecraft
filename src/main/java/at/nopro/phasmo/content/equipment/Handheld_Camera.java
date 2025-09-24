package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public class Handheld_Camera implements Equipment {
    @Override
    public void handle(Event event, Entity entity, ItemReference reference) {

    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getHandheldCamera();
    }
}
