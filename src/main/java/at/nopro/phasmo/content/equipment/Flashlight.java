package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.AfterDropEvent;
import at.nopro.phasmo.event.AfterPickupEvent;
import at.nopro.phasmo.game.ItemReference;
import at.nopro.phasmo.light.PhasmoInstance;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;

public class Flashlight implements Equipment { // TODO rewrite light engine to allow directional light sources
    private static final int DIST = 15;

    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case PlayerMoveEvent e -> handle(e, en, r);
            case PlayerChangeHeldSlotEvent e -> handle(e, en, r);
            case AfterDropEvent e -> handle(e, en, r);
            case AfterPickupEvent e -> handle(e, en, r);
            case InstanceTickEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getFlashlight();
    }

    private void handle(InstanceTickEvent e, Entity entity, ItemReference r) {
        PhasmoInstance instance = (PhasmoInstance) e.getInstance();


    }
}
