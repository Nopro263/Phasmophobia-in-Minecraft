package at.nopro.old_phasmo.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class InteractionEntity extends Entity {
    private final Entity displayEntity;
    private Consumer<PlayerEntityInteractEvent> interact = (e) -> {
    };
    private Consumer<EntityAttackEvent> attack = (e) -> {
    };

    public InteractionEntity(int width) {
        super(EntityType.INTERACTION);

        this.displayEntity = new Entity(EntityType.BLOCK_DISPLAY);

        InteractionMeta interactionMeta = getEntityMeta();
        interactionMeta.setResponse(true);
        interactionMeta.setHasNoGravity(true);
        interactionMeta.setWidth(width / 16f);
        interactionMeta.setHeight(width / 16f);

        BlockDisplayMeta blockDisplayMeta = (BlockDisplayMeta) displayEntity.getEntityMeta();
        blockDisplayMeta.setHasNoGravity(true);
        blockDisplayMeta.setScale(new Vec(width / 16d, width / 16d, width / 16d));
    }

    @Override
    public @NotNull InteractionMeta getEntityMeta() {
        return (InteractionMeta) super.getEntityMeta();
    }

    @Override
    public @NotNull CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        CompletableFuture<Void> future = super.setInstance(instance, spawnPosition);
        instance.eventNode().addListener(EntityAttackEvent.class, (event) -> {
            if (event.getTarget() == this) {
                this.attack.accept(event);
            }
        });
        instance.eventNode().addListener(PlayerEntityInteractEvent.class, (event) -> {
            if (event.getTarget() == this) {
                this.interact.accept(event);
            }
        });

        future.join();

        return displayEntity.setInstance(instance, spawnPosition.sub(getEntityMeta().getWidth() / 2, 0, getEntityMeta().getWidth() / 2));
    }

    public void setBlock(Block block) {
        BlockDisplayMeta blockDisplayMeta = (BlockDisplayMeta) displayEntity.getEntityMeta();
        blockDisplayMeta.setBlockState(block);
    }

    public void setInteract(Consumer<PlayerEntityInteractEvent> eventConsumer) {
        this.interact = eventConsumer;
    }

    public void setAttack(Consumer<EntityAttackEvent> attack) {
        this.attack = attack;
    }
}
