package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;

public class EMF_Reader implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case GhostEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getEMFReader(0);
    }

    private void handle(GhostEvent ghostEvent, Entity entity, ItemReference r) {
        if (entity.getPosition().distanceSquared(ghostEvent.getOrigin()) <= 25) {
            r.set(ItemProvider.getEMFReader(ghostEvent.getEmfLevel()));

            ghostEvent.getGameContext().getScheduler().run(r.hashCode() + "", (isFirstRun) -> {
                if (isFirstRun) return TaskSchedule.seconds(1);

                int level = 5 - r.get().get(DataComponents.DAMAGE);

                r.set(ItemProvider.getEMFReader(Math.max(0, level - 1)));

                if (level - 1 <= 0) {
                    return TaskSchedule.stop();
                }
                return TaskSchedule.seconds(1);
            });
        }
    }
}
