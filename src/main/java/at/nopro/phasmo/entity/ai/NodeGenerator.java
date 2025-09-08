package at.nopro.phasmo.entity.ai;

import java.util.Collection;

public interface NodeGenerator {
    boolean isValid(Path.Node node);
    Collection<Path.Node> getWalkable(Path.Node node);
}
