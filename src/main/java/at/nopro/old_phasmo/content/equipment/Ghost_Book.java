package at.nopro.old_phasmo.content.equipment;

import at.nopro.old_phasmo.content.ItemProvider;
import at.nopro.old_phasmo.event.BeforePickupEvent;
import at.nopro.old_phasmo.event.PlaceEquipmentEvent;
import at.nopro.old_phasmo.game.GameContext;
import at.nopro.old_phasmo.game.GameManager;
import at.nopro.old_phasmo.game.ItemReference;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;

import java.util.Objects;

public class Ghost_Book implements Equipment {
    @Override
    public void handle(Event event, Entity entity, ItemReference r) {
        if (event instanceof BeforePickupEvent pickupEvent) {
            if (Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_written")) {
                pickupEvent.setCancelled(true);
                return;
            }

            if (Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_open")) {
                r.set(ItemProvider.getClosedBook());
            }
        } else if (event instanceof PlayerBlockInteractEvent interactEvent) {
            GameContext g = GameManager.getGame(interactEvent.getInstance());
            r.set(ItemProvider.getOpenBook());

            Point pos = interactEvent.getBlockPosition();

            if (interactEvent.getBlockFace() == BlockFace.TOP || interactEvent.getBlockFace() == BlockFace.BOTTOM) {
                pos = pos.add(interactEvent.getCursorPosition());
            } else {
                pos = pos.add(interactEvent.getBlockFace().toDirection().mul(0.5)); // move more to middle
                pos = pos.add(interactEvent.getCursorPosition().withY(0)); // where in the block with Y = 0
            }


            EventDispatcher.call(new PlaceEquipmentEvent(
                    g,
                    pos.asPos(),
                    this,
                    r
            ));
        }
    }

    public void write(ItemReference r) {
        if (Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_written")) {
            return;
        }
        r.set(ItemProvider.getWrittenBook());
    }

    public boolean canWrite(ItemReference r) {
        return Objects.equals(r.get().get(DataComponents.ITEM_MODEL), "phasmo:book_open");
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getClosedBook();
    }
}
