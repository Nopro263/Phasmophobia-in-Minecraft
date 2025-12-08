package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

public class ConeLightSource implements PointLightSource {
    private final int strength;
    private final int maxDist;
    private final int k;
    protected Pos source;

    public ConeLightSource(Pos source, int strength, int k) {
        this.source = source;
        this.strength = strength;
        this.maxDist = strength;
        this.k = k;
    }

    @Override
    public long getId() {
        return this.hashCode();
    }

    @Override
    public Pos getSource() {
        return source;
    }

    public int getStrength() {
        return strength;
    }

    public int getLevelAtPosition(int x, int y, int z) {
        // CONE - thx chatgpt
        Vec V = source.direction();
        Vec W = new Vec(x, y, z).sub(source);

        double h = W.dot(V);

        if (h <= 0 || h > maxDist) {
            return 0;
        }


        double d = W.lengthSquared();
        double s = d - h * h;

        double maxS2 = ( k * h ) * ( k * h );

        if (s <= maxS2) {
            return (int) ( strength - Math.sqrt(d) );
        } else {
            return 0;
        }
    }
}
