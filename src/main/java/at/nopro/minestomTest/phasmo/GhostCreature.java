package at.nopro.minestomTest.phasmo;

import at.nopro.minestomTest.phasmo.ai.NodeFollower;
import at.nopro.minestomTest.phasmo.ai.NodeGenerator;
import at.nopro.minestomTest.phasmo.ai.PhasmoCreature;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FollowTargetGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.List;

public class GhostCreature extends PhasmoCreature {
    private final MapMeta mapMeta;
    public GhostCreature(MapMeta mapMeta) {
        super(EntityType.ZOMBIE);

        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        this.mapMeta = mapMeta;

        addAIGroup(
                List.of(new FollowTargetPlayerGoal(this),
                        new GhostStrollGoal(this, 20)),
                List.of(new ClosestVisibleEntitySelector(this, 10))
        );

        addInteractionHandlers(
                new OpenDoorHandler(this),
                new CloseDoorHandler(this)
        );

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach((p) -> {
                if(getNavigator().getGoalPosition() == null) {
                    return;
                }
                p.sendPacket(new ParticlePacket(Particle.FALLING_SPORE_BLOSSOM, getNavigator().getGoalPosition().x() - 0.5,
                                                                                getNavigator().getGoalPosition().y(),
                                                                                getNavigator().getGoalPosition().z() - 0.5,
                        0.5f,0,0.5f,1,100));
            });
            return TaskSchedule.millis(100);
        });
    }

    public MapMeta getMapMeta() {
        return mapMeta;
    }
}
