package at.nopro.minestomTest.phasmo.equipment;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class Flashlight implements Equipment{
    @Override
    public ItemStack getDefaultItemStack() {
        return ItemStack.builder(Material.STICK).glowing(true)
                .customName(Component.text("Flashlight")).build();
    }

    @Override
    public void registerEvents(EventNode<@NotNull PlayerEvent> eventNode) {
        eventNode.addListener(PlayerUseItemEvent.class, (event) -> {
            System.out.println("use");
        });

        eventNode.addListener(PlayerHandAnimationEvent.class, (event) -> {
            System.out.println("hand");
        });

        eventNode.addListener(PlayerSwapItemEvent.class, (event) -> {
            System.out.println("swap");
        });
    }

    @Override
    public EquipmentManager.EquipmentType type() {
        return EquipmentManager.EquipmentType.Flashlight;
    }
}
