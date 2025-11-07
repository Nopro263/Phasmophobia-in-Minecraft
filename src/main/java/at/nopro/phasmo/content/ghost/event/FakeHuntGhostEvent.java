package at.nopro.phasmo.content.ghost.event;

public class FakeHuntGhostEvent implements BaseGhostEvent { //TODO
    @Override
    public void start() {
        System.out.println("Fake hunt");
    }

    @Override
    public void tick(long d) {

    }

    @Override
    public void end() {

    }
}
