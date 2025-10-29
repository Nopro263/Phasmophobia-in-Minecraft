package at.nopro.phasmo.content.ghost;

import at.nopro.phasmo.entity.goals.GhostEventGoal;
import at.nopro.phasmo.entity.goals.StartHuntGoal;
import at.nopro.phasmo.entity.goals.ThrowItemGoal;
import at.nopro.phasmo.entity.goals.WanderGoal;
import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.EntityType;

public class TestGhost extends BaseGhost {
    public TestGhost(GameContext gameContext) {
        super(EntityType.WITHER_SKELETON, gameContext);

        addGoal(10, new StartHuntGoal(this));
        addGoal(20, new ThrowItemGoal(this));
        addGoal(30, new GhostEventGoal(this));
        addGoal(40, new WanderGoal(this));

        activateEMF5();
        activateFreezing();
        activateDOTS();
    }
}
