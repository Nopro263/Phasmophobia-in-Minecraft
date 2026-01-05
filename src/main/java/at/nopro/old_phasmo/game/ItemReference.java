package at.nopro.old_phasmo.game;

import at.nopro.old_phasmo.entity.ItemEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemReference {
    private Supplier<ItemStack> getter;
    private Consumer<ItemStack> setter;
    private Entity containingEntity;
    private boolean active;

    public ItemReference() {
        active = true;
    }

    @ApiStatus.Internal
    public void setInPlayerInventory(Player player, int slot) {
        if (slot == -1) {
            getter = () -> player.getInventory().getCursorItem();
            setter = itemStack -> player.getInventory().setCursorItem(itemStack);
        } else {
            getter = () -> player.getInventory().getItemStack(slot);
            setter = itemStack -> player.getInventory().setItemStack(slot, itemStack);
        }
        containingEntity = player;
    }

    @ApiStatus.Internal
    public void setAsEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            getter = itemEntity::getItem;
            setter = itemEntity::setItem;
            containingEntity = itemEntity;
            return;
        }

        throw new RuntimeException("Not an Item-Entity");
    }

    @ApiStatus.Internal
    public void deactivate() {
        active = false;
    }

    public ItemStack get() {
        return getter.get();
    }

    public void set(ItemStack itemStack) {
        setter.accept(itemStack);
    }

    public @NotNull Entity getContainingEntity() {
        return containingEntity;
    }
}
