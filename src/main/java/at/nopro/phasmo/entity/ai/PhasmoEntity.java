package at.nopro.phasmo.entity.ai;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;

public class PhasmoEntity extends EntityCreature {
    private final GameContext gameContext;

    public PhasmoEntity(EntityType entityType, GameContext gameContext) {
        super(entityType);

        this.gameContext = gameContext;

        this.getNavigator().setNodeGenerator(() -> new PhasmoNodeGenerator(gameContext.getPathCache()));
        this.getNavigator().setNodeFollower(() -> new PhasmoNodeFollower(this));
    }
}
