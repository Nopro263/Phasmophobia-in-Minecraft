package at.nopro.old_phasmo.entity.goals;

import at.nopro.old_phasmo.content.ghost.BaseGhost;
import at.nopro.old_phasmo.content.ghost.event.*;
import at.nopro.old_phasmo.event.EmfEvent;
import net.minestom.server.entity.ai.GoalSelector;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class GhostEventGoal extends GoalSelector {
    private static final List<BaseGhostEvent> ghostEvents = List.of(
            new FlickerLightsGhostEvent(),
            new DestroyLightsGhostEvent(),
            new FakeHuntGhostEvent(),
            new MistGhostEvent(),
            new SoundGhostEvent()
    );
    protected final BaseGhost ghost;
    private final Random random;
    protected long TIMEOUT = Duration.ofSeconds(2).toMillis();
    protected long DURATION = Duration.ofSeconds(5).toMillis();
    private long lastEvent;
    private long eventStart;
    private BaseGhostEvent ghostEvent;

    public GhostEventGoal(BaseGhost entityCreature) {
        super(entityCreature);
        ghost = entityCreature;
        random = new Random();
    }

    @Override
    public boolean shouldStart() {
        return System.currentTimeMillis() - lastEvent >= TIMEOUT + DURATION && random.nextDouble() < 0.1;
    }

    @Override
    public void start() {
        System.out.println("Ghost Event");
        ghostEvent = ghostEvents.get(random.nextInt(ghostEvents.size()));
        DURATION = random.nextLong(Duration.ofSeconds(4).toMillis(), Duration.ofSeconds(10).toMillis());

        lastEvent = System.currentTimeMillis();
        eventStart = System.currentTimeMillis();

        ghost.getGameContext().getEventNode().call(new EmfEvent(ghost.getGameContext(), EmfEvent.ActionType.GHOST_EVENT, ghost.getPosition()));

        ghostEvent.start();
    }

    @Override
    public void tick(long l) {
        ghostEvent.tick(l);
    }

    @Override
    public boolean shouldEnd() {
        return System.currentTimeMillis() - eventStart >= DURATION;
    }

    @Override
    public void end() {
        ghostEvent.end();
        System.out.println("Ghost Event end");
    }
}
