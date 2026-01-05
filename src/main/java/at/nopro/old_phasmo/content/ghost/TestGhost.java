package at.nopro.old_phasmo.content.ghost;

import at.nopro.old_phasmo.entity.goals.GhostEventGoal;
import at.nopro.old_phasmo.entity.goals.StartHuntGoal;
import at.nopro.old_phasmo.entity.goals.ThrowItemGoal;
import at.nopro.old_phasmo.entity.goals.WanderGoal;
import at.nopro.old_phasmo.game.GameContext;
import net.minestom.server.entity.EntityType;

public class TestGhost extends BaseGhost {
    public TestGhost(GameContext gameContext) {
        super(EntityType.SKELETON, gameContext);

        addGoal(10, new StartHuntGoal(this));
        addGoal(20, new ThrowItemGoal(this));
        addGoal(30, new GhostEventGoal(this));
        addGoal(40, new WanderGoal(this));

        activateEMF5();
        activateFreezing();
        activateDOTS();
        activateSpiritBox();
        activateBookWriting();
    }
}
