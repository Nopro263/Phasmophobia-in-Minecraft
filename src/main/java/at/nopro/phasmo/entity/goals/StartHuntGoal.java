package at.nopro.phasmo.entity.goals;

import at.nopro.phasmo.content.ghost.BaseGhost;
import at.nopro.phasmo.entity.ai.InvalidPositionException;
import at.nopro.phasmo.event.PlayerDieEvent;
import at.nopro.phasmo.event.StartHuntEvent;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.GoalSelector;

import java.time.Duration;

public class StartHuntGoal extends GoalSelector {
    protected final BaseGhost ghost;
    protected long HUNT_TIMEOUT = Duration.ofSeconds(30).toMillis();
    protected long GRACE_PERIOD = Duration.ofMinutes(1).toMillis();
    protected long HUNT_DURATION = Duration.ofSeconds(20).toMillis();
    private long lastTriedHunt;
    private long lastEndedHunt;
    private Player lastDeadPlayer;
    private Player targetPlayer;
    private Point roamTargetPos;

    public StartHuntGoal(BaseGhost entityCreature) {
        super(entityCreature);
        ghost = entityCreature;

        ghost.getGameContext().getEventNode().addListener(PlayerDieEvent.class, e -> {
            if (!e.isNowAlive()) {
                lastDeadPlayer = e.getPlayer();
            }
        });
    }

    @Override
    public boolean shouldStart() {
        if (canStart()) {
            lastTriedHunt = System.currentTimeMillis();
            StartHuntEvent event = new StartHuntEvent(ghost.getGameContext());
            ghost.getGameContext().getEventNode().call(event);
            if (event.isCancelled()) {
                System.out.println("Hunt failed");
            }
            return !event.isCancelled();
        }
        return false;
    }

    private boolean canStart() {
        if (System.currentTimeMillis() - lastTriedHunt < HUNT_TIMEOUT) {
            return false;
        }
        if (ghost.getRoom().getPlayers().isEmpty()) {
            return false;
        }
        if (ghost.getGameContext().getPlayerManager().getAverageSanity() >= 50) {
            return false;
        }
        return System.currentTimeMillis() - lastEndedHunt >= GRACE_PERIOD;
    }

    @Override
    public void start() {
        System.out.println("Hunt started");
        ghost.setAutoViewable(true);
        lastDeadPlayer = null;
    }

    @Override
    public void tick(long l) {
        ghost.setAutoViewable(true);
        if (targetPlayer == null) {
            var entity = ghost.getInstance().getNearbyEntities(ghost.getPosition(), 10).stream().filter((e) -> e.hasLineOfSight(ghost) && e instanceof Player p && ghost.getGameContext().getPlayerManager().isAlive(p)).findFirst();
            targetPlayer = (Player) entity.orElse(null);
        }

        if (targetPlayer != null) {
            if (ghost.getPosition().distanceSquared(targetPlayer.getPosition()) < 4) {
                ghost.getGameContext().getPlayerManager().showKillAnimation(targetPlayer);
                ghost.getGameContext().getPlayerManager().kill(targetPlayer);
            }
            try {
                ghost.goTo(targetPlayer.getPosition());
                return;
            } catch (InvalidPositionException _) {
                targetPlayer = null;
            }
        }

        if (roamTargetPos == null || roamTargetPos.sameBlock(ghost.getPosition())) {
            roamTargetPos = ghost.getGameContext().getPathCache().getRandomBlock((v) -> ghost.getGameContext().getRoomManager().getRoom(v) != null);
            roamTargetPos = roamTargetPos.add(0.5, 0, 0.5);
        }

        try {
            ghost.goTo(roamTargetPos);
        } catch (InvalidPositionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldEnd() {
        if (System.currentTimeMillis() - lastTriedHunt > HUNT_DURATION) {
            return true;
        }
        return lastDeadPlayer != null;
    }

    @Override
    public void end() {
        ghost.setAutoViewable(false);
        lastEndedHunt = System.currentTimeMillis();
        System.out.println("Hunt ended");
    }
}
