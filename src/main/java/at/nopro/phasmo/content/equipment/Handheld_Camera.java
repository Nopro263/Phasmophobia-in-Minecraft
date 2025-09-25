package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.Utils;
import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;

public class Handheld_Camera implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case PlayerMoveEvent e -> handle(e,en,r);
            case EntityTeleportEvent e -> handle(e,en,r);
            default -> {}
        }
    }

    private void handle(PlayerMoveEvent e, Entity en, ItemReference r) {
        GameContext g = GameManager.getGame(e.getInstance());
        g.getCameraManager().teleport(Utils.addInDirection(en.getPosition(), 0.75));
    }

    private void handle(EntityTeleportEvent e, Entity en, ItemReference r) {
        GameContext g = GameManager.getGame(e.getEntity().getInstance());
        g.getCameraManager().teleport(Utils.addInDirection(e.getNewPosition().sub(0,g.getCamPlayer().getEyeHeight()-0.1,0), 0.3));
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getHandheldCamera();
    }
}
