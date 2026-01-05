package at.nopro.old_phasmo.entity.goals;

import at.nopro.old_phasmo.content.ghost.BaseGhost;
import net.minestom.server.entity.ai.GoalSelector;

public class ThrowItemGoal extends GoalSelector {
    public ThrowItemGoal(BaseGhost entityCreature) {
        super(entityCreature);
    }

    @Override
    public boolean shouldStart() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long l) {

    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void end() {

    }
}
