package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.PhasmoEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

import java.util.function.Consumer;

public class Ghost_Book implements Equipment {
    @Override
    public void handle(Event event, Player player, Consumer<ItemStack> updateFunction) {

    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getClosedBook();
    }
}
