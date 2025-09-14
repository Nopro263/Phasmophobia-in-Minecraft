package at.nopro.phasmo.game;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemReference {
    private Supplier<ItemStack> getter;
    private Consumer<ItemStack> setter;
    private boolean active;

    public ItemReference() {
        active = true;
    }

    @ApiStatus.Internal
    public void setInPlayerInventory(Player player, int slot) {
        getter = () -> player.getInventory().getItemStack(slot);

        setter = itemStack -> player.getInventory().setItemStack(slot, itemStack);
    }

    @ApiStatus.Internal
    public void setAsEntity(Entity entity) {
        if(!(entity.getEntityMeta() instanceof ItemEntityMeta itemEntityMeta )) {
            throw new RuntimeException("Not an Item-Entity");
        }

        getter = itemEntityMeta::getItem;

        setter = itemEntityMeta::setItem;
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
}
