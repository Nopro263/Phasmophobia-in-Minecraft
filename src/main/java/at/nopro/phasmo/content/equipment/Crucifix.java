package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.event.StartHuntEvent;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public class Crucifix implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case StartHuntEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getCrucifix(false);
    }

    private void handle(StartHuntEvent startHuntEvent, Entity entity, ItemReference r) {
        if (!( entity instanceof ItemEntity )) return;
        if (isBurned(r.get())) return;

        startHuntEvent.setCancelled(true);
        r.set(ItemProvider.getCrucifix(true));
        System.out.println("Hunt cancelled");
    }

    private boolean isBurned(ItemStack itemStack) {
        return itemStack.get(DataComponents.ITEM_MODEL).equals("minecraft:netherite_pickaxe");
    }
}
