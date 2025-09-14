package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.PhasmoEvent;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

import java.util.function.Consumer;

public class Ghost_Book implements Equipment {
    @Override
    public void handle(Event event, Entity entity, ItemReference r) {

    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getClosedBook();
    }
}
