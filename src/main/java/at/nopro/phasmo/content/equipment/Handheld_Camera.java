package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.Utils;
import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.PlaceEquipmentEvent;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;

public class Handheld_Camera implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case PlayerMoveEvent e -> handle(e, en, r);
            case EntityTeleportEvent e -> handle(e, en, r);
            case PlayerBlockInteractEvent e -> handle(e, en, r);
            case PlayerChangeHeldSlotEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    private void handle(PlayerMoveEvent e, Entity en, ItemReference r) {
        GameContext g = GameManager.getGame(e.getInstance());
        g.getCameraManager().teleport(Utils.addInDirection(en.getPosition(), 0.75));
    }

    private void handle(EntityTeleportEvent e, Entity en, ItemReference r) {
        GameContext g = GameManager.getGame(e.getEntity().getInstance());
        g.getCameraManager().teleport(Utils.addInDirection(e.getNewPosition().sub(0, g.getCamPlayer().getEyeHeight() - 0.1, 0), 0.3));
    }

    private void handle(PlayerBlockInteractEvent e, Entity en, ItemReference r) {
        GameContext g = GameManager.getGame(e.getInstance());

        Point pos = e.getBlockPosition();

        if (e.getBlockFace() == BlockFace.TOP || e.getBlockFace() == BlockFace.BOTTOM) {
            pos = pos.add(e.getCursorPosition());
        } else {
            pos = pos.add(e.getBlockFace().toDirection().mul(0.5)); // move more to middle
            pos = pos.add(e.getCursorPosition().withY(0)); // where in the block with Y = 0
        }

        Pos result = pos
                .add(0, 1.62, 0)
                .asPos().withYaw(e.getPlayer().getPosition().yaw());


        EventDispatcher.call(new PlaceEquipmentEvent(
                g,
                result,
                this,
                r
        ));
    }

    private void handle(PlayerChangeHeldSlotEvent e, Entity en, ItemReference r) {
        e.getPlayer().dropItem(e.getItemInOldSlot());
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getHandheldCamera();
    }
}
