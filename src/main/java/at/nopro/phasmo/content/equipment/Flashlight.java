package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.AfterDropEvent;
import at.nopro.phasmo.event.AfterPickupEvent;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemReference;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.block.BlockIterator;

public class Flashlight implements Equipment { // TODO rewrite lighting engine to allow directional light sources
    private static final int DIST = 15;

    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case PlayerMoveEvent e -> handle(e, en, r);
            case PlayerChangeHeldSlotEvent e -> handle(e, en, r);
            case AfterDropEvent e -> handle(e, en, r);
            case AfterPickupEvent e -> handle(e, en, r);
            case InstanceTickEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getFlashlight();
    }

    private void handle(PlayerMoveEvent moveEvent, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(moveEvent.getInstance());
        x(gameContext, gameContext.getInstance(), entity.getPreviousPosition(), entity.getEyeHeight(), DIST, true);
        x(gameContext, gameContext.getInstance(), entity.getPosition(), entity.getEyeHeight(), DIST, false);
    }

    private void handle(PlayerChangeHeldSlotEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(e.getInstance());
        x(gameContext, gameContext.getInstance(), entity.getPosition(), entity.getEyeHeight(), DIST, true);
    }

    private void handle(AfterDropEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = e.getGameContext();
        x(gameContext, gameContext.getInstance(), e.getPlayer().getPosition(), e.getPlayer().getEyeHeight(), DIST + 2, true);
        x(gameContext, gameContext.getInstance(), e.getEntity().getPosition(), entity.getEyeHeight(), DIST, false);
    }

    private void handle(AfterPickupEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = e.getGameContext();
        x(gameContext, gameContext.getInstance(), e.getEntity().getPosition(), e.getEntity().getEyeHeight(), DIST + 2, true);
        x(gameContext, gameContext.getInstance(), e.getPlayer().getPosition(), e.getPlayer().getEyeHeight(), DIST, false);
    }

    private void handle(InstanceTickEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(e.getInstance());
        x(gameContext, gameContext.getInstance(), entity.getPosition(), entity.getEyeHeight(), DIST, false);
    }

    private void x(GameContext gameContext, Instance instance, Pos pos, double offset, int dist, boolean revert) {

        BlockIterator bi = new BlockIterator(pos, offset, dist);
        Point prev = null;
        int distance = 0;
        Point b = pos;
        while (bi.hasNext()) {
            if (instance.getBlock(b).isAir()) {
                prev = b;
                distance++;
                if (distance > 5) {
                    Block block;
                    if (revert) {
                        block = Block.AIR;
                    } else {
                        block = Block.LIGHT.withProperty("level", Math.max(dist - distance, 0) + "");
                    }
                    lightBlock(gameContext, instance, prev, block);
                }
            } else {
                if (prev != null) {
                    break;
                }
            }

            b = bi.next();
        }

        if (distance <= 5 && prev != null) {
            Block block;
            if (revert) {
                block = Block.AIR;
            } else {
                block = Block.LIGHT.withProperty("level", Math.max(dist - distance, 0) + "");
            }
            lightBlock(gameContext, instance, prev, block);
        }
    }

    private void lightBlock(GameContext gameContext, Instance instance, Point prev, Block block) {
        for (Player player : instance.getPlayers()) {
            player.sendPacket(new BlockChangePacket(prev, block));
        }
    }
}
