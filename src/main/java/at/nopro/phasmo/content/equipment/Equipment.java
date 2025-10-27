package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public interface Equipment {
    void handle(Event event, Entity entity, ItemReference reference);

    ItemStack getDefault();
}
