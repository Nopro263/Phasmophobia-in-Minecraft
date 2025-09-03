package at.nopro.minestomTest.phasmo.equipment;

import at.nopro.minestomTest.phasmo.item.ItemManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class Book implements Equipment {
    @Override
    public ItemStack getDefaultItemStack() {
        return ItemStack.builder(Material.BOOK).glowing(true)
                .customName(Component.text("Book")).build();
    }

    @Override
    public void registerEvents(EventNode<@NotNull PlayerEvent> eventNode) {
        eventNode.addListener(PlayerUseItemOnBlockEvent.class, (event) -> {
            if(event.getBlockFace() == BlockFace.TOP) {
                if(!canPlaceAt(event.getInstance(), event.getPosition())) {
                    return;
                }
                placeBook(event.getInstance(), event.getPosition());
                event.getPlayer().setItemInHand(event.getHand(), ItemStack.AIR);
            }
        });
    }

    private boolean canPlaceAt(Instance instance, Point point) {
        return ItemManager.canPlace(ItemManager.ItemType.BOOK_OPEN, instance, point.add(0,1,0));
    }

    private void placeBook(Instance instance, Point point) {
        ItemManager.place(ItemManager.ItemType.BOOK_OPEN, instance, point.add(0,1,0));
    }

    @Override
    public EquipmentManager.EquipmentType type() {
        return EquipmentManager.EquipmentType.Book;
    }
}
