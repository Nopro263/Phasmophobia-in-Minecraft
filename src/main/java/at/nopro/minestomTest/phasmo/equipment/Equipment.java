package at.nopro.minestomTest.phasmo.equipment;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Equipment {
    ItemStack getDefaultItemStack();
    void registerEvents(EventNode<@NotNull PlayerEvent> eventNode);
    EquipmentManager.EquipmentType type();
}
