package at.nopro.minestomTest.phasmo.ai;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhasmoCreature extends EntityCreature {
    private final List<InteractionHandler> interactionHandlers;

    public PhasmoCreature(EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    public PhasmoCreature(EntityType entityType, UUID uuid) {
        super(entityType, uuid);

        this.interactionHandlers = new ArrayList<>();

        getNavigator().setNodeGenerator(() -> new NodeGenerator(this));
        getNavigator().setNodeFollower(() -> new NodeFollower(this));
    }

    protected void addInteractionHandlers(List<InteractionHandler> interactionHandlers) {
        this.interactionHandlers.addAll(interactionHandlers);
    }

    protected void addInteractionHandlers(InteractionHandler ...interactionHandlers) {
        addInteractionHandlers(List.of(interactionHandlers));
    }

    public @Nullable InteractionHandler getInteractionHandlerFor(Pos blockPos, Block block) {
        for(InteractionHandler i : this.interactionHandlers) {
            if(i.canInteract(blockPos, block)) {
                return i;
            }
        }
        return null;
    }
}
