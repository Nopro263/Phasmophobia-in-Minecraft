package at.nopro.minestomTest.phasmo;

import at.nopro.minestomTest.phasmo.utils.Utils;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.FollowTargetGoal;
import net.minestom.server.entity.pathfinding.Navigator;

import java.time.Duration;

public class FollowTargetPlayerGoal extends GoalSelector {
    private Point lastTargetPos;
    private Point lastPos;

    public FollowTargetPlayerGoal(EntityCreature entityCreature) {
        super(entityCreature);
    }

    public boolean shouldStart() {
        Entity target = this.entityCreature.getTarget();
        if (target == null) {
            target = this.findTarget();
        }

        if (target == null) {
            return false;
        } else {
            this.entityCreature.setTarget(target);
            return true;
        }
    }

    public void start() {
        Navigator navigator = this.entityCreature.getNavigator();

        assert this.entityCreature.getTarget() != null;

        this.lastTargetPos = this.entityCreature.getTarget().getPosition();

        navigator.setPathTo(this.lastTargetPos);
    }

    public void tick(long time) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach((p) -> p.sendActionBar(Component.text("follow  " + this.lastTargetPos)));
        Entity target = this.entityCreature.getTarget();
        Navigator navigator = this.entityCreature.getNavigator();
        assert target != null;

        if(Utils.isLineOfSight(this.entityCreature, target)) {
            this.lastTargetPos = target.getPosition();
            navigator.setPathTo(this.lastTargetPos);
        }

        this.lastPos = this.entityCreature.getPosition();
    }

    public boolean shouldEnd() {
        Entity target = this.entityCreature.getTarget();
        if(target == null) {
            return true;
        }
        if(this.entityCreature.getPosition().distanceSquared(this.lastPos) < 1E-3 * 1E-3) {
            //System.out.println("stuck");
            return true;
        }

        boolean b = !Utils.isLineOfSight(this.entityCreature, target) && this.lastTargetPos.distanceSquared(this.entityCreature.getPosition()) < 1;
        return this.lastTargetPos == null || target.isRemoved() || b;
    }

    public void end() {
        this.entityCreature.getNavigator().setPathTo((Point)null);
        this.entityCreature.setTarget(null);
    }
}
