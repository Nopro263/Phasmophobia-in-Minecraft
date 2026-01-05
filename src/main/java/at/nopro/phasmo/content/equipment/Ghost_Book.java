package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.item.ItemStack;

import java.util.Objects;

public class Ghost_Book implements Equipment { //TODO make book placeable, book closes when picked up
    @Override
    public void handle(Event event, Entity entity, ItemReference r) {
        if (event instanceof EntityAttackEvent attackEvent) {
            if (entity.equals(attackEvent.getTarget())) {
                if (Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_closed")) {
                    r.set(ItemProvider.getOpenBook());
                } else {
                    r.set(ItemProvider.getClosedBook());
                }
            }
        }
    }

    public void write(ItemReference r) {
        if (Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_written")) {
            return;
        }
        r.set(ItemProvider.getWrittenBook());
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getClosedBook();
    }
}
