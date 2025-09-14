package at.nopro.phasmo.entity;

import at.nopro.phasmo.content.ItemModelProvider;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemEntity extends Entity {
    private Entity interactionEntity;

    public ItemEntity(ItemStack itemStack) {
        this(itemStack, -1,-1);
    }

    public ItemEntity(ItemStack itemStack, float width, float height) {
        super(EntityType.ITEM);

        if (width == -1 || height == -1) {
            BoundingBox bb = ItemModelProvider.getItemBoundingBox(itemStack.get(DataComponents.ITEM_MODEL));
            assert bb != null;
            assert bb.width() == bb.depth();

            if(width == -1) width = (float) bb.width();
            if(height == -1) height = (float) bb.height();
        }

        if(this.getEntityMeta() instanceof ItemEntityMeta itemEntityMeta) {
            itemEntityMeta.setItem(itemStack);
        }

        interactionEntity = new Entity(EntityType.INTERACTION);
        if(interactionEntity.getEntityMeta() instanceof InteractionMeta interactionMeta) {
            interactionMeta.setResponse(true);
            interactionMeta.setHeight(height);
            interactionMeta.setWidth(width);
        }
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
}
