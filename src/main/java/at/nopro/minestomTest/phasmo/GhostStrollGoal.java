package at.nopro.minestomTest.phasmo;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GhostStrollGoal extends GoalSelector {
    private static final long DELAY;
    private final GhostCreature creature;
    private final Random random = new Random();
    private long lastStroll;
    private Point lastPos;

    public GhostStrollGoal(GhostCreature entityCreature, int radius) {
        super(entityCreature);
        this.creature = entityCreature;
    }

    @Override
    public boolean shouldStart() {
        if(entityCreature.getChunk() == null || !entityCreature.getChunk().isLoaded()) {
            return false;
        }
        return System.nanoTime() - this.lastStroll >= DELAY;
    }

    @Override
    public boolean shouldEnd() {
        if(this.entityCreature.getPosition().distanceSquared(this.lastPos) < 1E-3 * 1E-3) {
            //System.out.println("stuck");
            return true;
        }
        return creature.getNavigator().getGoalPosition().distanceSquared(this.entityCreature.getPosition()) < 2;
    }

    @Override
    public void start() {
        int i = 0;
        while (i++ < 20) {
            int x = random.nextInt((int) creature.getMapMeta().corner1.x(), (int) creature.getMapMeta().corner2.x());
            int z = random.nextInt((int) creature.getMapMeta().corner1.z(), (int) creature.getMapMeta().corner2.z());
            Pos pos = new Pos(x, creature.getMapMeta().corner1.y() - 1, z);
            Block block = creature.getInstance().getBlock(pos);
            if (Arrays.stream(creature.getMapMeta().floor).anyMatch((b) -> block.key().equals(b.key()))) {
                creature.getNavigator().setPathTo(pos.add(0,1,0));
                break;
            }
        }
    }

    public void tick(long time) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach((p) -> p.sendActionBar(Component.text("stroll")));
        this.lastPos = this.entityCreature.getPosition();
    }

    public void end() {
        this.lastStroll = System.nanoTime();
    }

    static {
        DELAY = TimeUnit.MILLISECONDS.toNanos(2500L);
    }
}
