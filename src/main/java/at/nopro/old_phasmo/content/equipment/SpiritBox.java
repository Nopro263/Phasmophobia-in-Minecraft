package at.nopro.old_phasmo.content.equipment;

import at.nopro.old_phasmo.content.ItemProvider;
import at.nopro.old_phasmo.event.SpiritBoxAnswerEvent;
import at.nopro.old_phasmo.event.SpiritBoxQuestionEvent;
import at.nopro.old_phasmo.game.GameContext;
import at.nopro.old_phasmo.game.GameManager;
import at.nopro.old_phasmo.game.ItemReference;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;

public class SpiritBox implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case SpiritBoxAnswerEvent e -> handle(e, en, r);
            case PlayerChatEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    private void handle(SpiritBoxAnswerEvent event, Entity en, ItemReference r) {
        r.set(ItemProvider.getSpiritBox(true));
        event.gameContext().getScheduler().run(this.hashCode() + "spiritBox", (first) -> {
            if (first) return TaskSchedule.seconds(3);
            r.set(ItemProvider.getSpiritBox(false));
            return TaskSchedule.stop();
        });
    }

    private void handle(PlayerChatEvent event, Entity en, ItemReference r) {
        boolean inRange = en.getPosition().distanceSquared(event.getPlayer().getPosition()) < 25;
        System.out.println(inRange);
        if (!inRange) {
            return;
        }
        GameContext gameContext = GameManager.getGame(event.getInstance());

        gameContext.getEventNode().call(new SpiritBoxQuestionEvent(gameContext, event.getPlayer()));
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getSpiritBox(false);
    }
}
