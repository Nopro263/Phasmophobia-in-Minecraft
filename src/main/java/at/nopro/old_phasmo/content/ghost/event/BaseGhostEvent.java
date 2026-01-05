package at.nopro.old_phasmo.content.ghost.event;

public interface BaseGhostEvent {
    void start();

    void tick(long d);

    void end();
}
