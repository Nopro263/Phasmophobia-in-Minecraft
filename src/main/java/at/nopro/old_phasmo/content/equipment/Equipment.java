package at.nopro.old_phasmo.content.equipment;

import at.nopro.old_phasmo.game.ItemReference;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public interface Equipment {
    void handle(Event event, Entity entity, ItemReference reference);

    ItemStack getDefault();
}
