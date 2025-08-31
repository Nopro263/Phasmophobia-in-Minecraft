package at.nopro.minestomTest.phasmo;

import at.nopro.minestomTest.phasmo.utils.Utils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;

import java.util.function.Predicate;

public class ClosestVisibleEntitySelector extends ClosestEntityTarget {
    public ClosestVisibleEntitySelector(EntityCreature entityCreature, double range) {
        super(entityCreature, range, (entity -> entity instanceof Player && Utils.isLineOfSight(entityCreature, entity) ));
    }
}
