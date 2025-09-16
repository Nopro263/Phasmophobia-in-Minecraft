package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.PhasmoEvent;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.item.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

public class Ghost_Book implements Equipment {
    @Override
    public void handle(Event event, Entity entity, ItemReference r) {
        if(event instanceof EntityAttackEvent attackEvent) {
            System.out.println(1);
            if(entity.equals(attackEvent.getTarget())) {
                System.out.println("HIT");
                if(Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_closed")) {
                    r.set(ItemProvider.getOpenBook());
                } else {
                    r.set(ItemProvider.getClosedBook());
                }
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getClosedBook();
    }
}
