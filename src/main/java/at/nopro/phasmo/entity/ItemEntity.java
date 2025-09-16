package at.nopro.phasmo.entity;

import at.nopro.phasmo.content.ItemModelProvider;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemEntity extends Entity {
    private Entity interactionEntity;

    public ItemEntity(ItemStack itemStack) {
        super(EntityType.ITEM_DISPLAY);

        this.interactionEntity = new Entity(EntityType.INTERACTION);
        load(itemStack);
    }

    private void load(ItemStack itemStack) {
        ItemModelProvider.ItemModel itemModel = ItemModelProvider.getItemModel(itemStack.get(DataComponents.ITEM_MODEL));
        BoundingBox bb = itemModel.boundingBox();
        assert bb != null;
        assert bb.width() == bb.depth();

        if(this.getEntityMeta() instanceof ItemDisplayMeta itemDisplayMeta) {
            itemDisplayMeta.setItemStack(itemStack);
            itemDisplayMeta.setDisplayContext(ItemDisplayMeta.DisplayContext.GROUND);

            itemDisplayMeta.setTranslation(itemModel.translation());
            itemDisplayMeta.setLeftRotation(itemModel.leftRotation());
            itemDisplayMeta.setRightRotation(itemModel.rightRotation());
            itemDisplayMeta.setScale(itemModel.scale());

            itemDisplayMeta.setHasNoGravity(true);

            setBoundingBox(bb);
        }

        if(this.interactionEntity.getEntityMeta() instanceof InteractionMeta interactionMeta) {
            interactionMeta.setWidth((float) bb.width());
            interactionMeta.setHeight((float) bb.height());
            interactionMeta.setResponse(true);
        }
    }

    public ItemStack getItem() {
        return ((ItemDisplayMeta) this.getEntityMeta()).getItemStack();
    }

    public void setItem(ItemStack itemStack) {
        load(itemStack);
    }

    @Override
    public CompletableFuture<Void> setInstance(Instance instance, Pos spawnPosition) {
        super.setInstance(instance, spawnPosition).join();
        this.addPassenger(interactionEntity);
        return interactionEntity.setInstance(instance, spawnPosition);
    }

    @Override
    public CompletableFuture<Void> teleport(Pos position, Vec velocity, long @Nullable [] chunks, int flags, boolean shouldConfirm) {
        interactionEntity.teleport(position, velocity, chunks, flags, shouldConfirm);
        return super.teleport(position, velocity, chunks, flags, shouldConfirm);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || interactionEntity.equals(obj);
    }
}
