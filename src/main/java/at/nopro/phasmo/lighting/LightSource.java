package at.nopro.phasmo.lighting;

public interface LightSource {
    default boolean isActive() {
        return true;
    }

    long getId();
}
