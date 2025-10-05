package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.TemperatureEvent;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemReference;
import at.nopro.phasmo.game.RoomManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.block.BlockIterator;

public class Flashlight implements Equipment { // TODO rewrite lighting engine to allow directional light sources
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case PlayerMoveEvent e -> handle(e,en,r);
            case PlayerChangeHeldSlotEvent e -> handle(e,en,r);
            default -> {}
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getFlashlight();
    }

    private void handle(PlayerMoveEvent moveEvent, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(moveEvent.getInstance());
        x(gameContext, gameContext.getInstance(), entity.getPreviousPosition(), entity.getEyeHeight(), 15, true);
        x(gameContext, gameContext.getInstance(), entity.getPosition(), entity.getEyeHeight(), 15, false);
    }

    private void handle(PlayerChangeHeldSlotEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(e.getInstance());
        x(gameContext, gameContext.getInstance(), entity.getPosition(), entity.getEyeHeight(), 15, true);
    }

    private void x(GameContext gameContext, Instance instance, Pos pos, double offset, int dist, boolean revert) {

        BlockIterator bi = new BlockIterator(pos, offset, dist);
        Point prev = null;
        int distance = 0;
        while (bi.hasNext()) {
            Point b = bi.next();
            if(instance.getBlock(b).isAir()) {
                prev = b;
                distance++;
                if(distance > 5) {
                    Block block;
                    if(revert) {
                        block = Block.AIR;
                    } else {
                        block = Block.LIGHT.withProperty("level",Math.max(15-distance, 0) + "");
                    }
                    lightBlock(gameContext, instance, prev, block);
                }
            } else {
                break;
            }
        }

        if(distance <= 5 && prev != null) {
            Block block;
            if(revert) {
                block = Block.AIR;
            } else {
                block = Block.LIGHT.withProperty("level",Math.max(15-distance, 0) + "");
            }
            lightBlock(gameContext, instance, prev, block);
        }
    }

    private void lightBlock(GameContext gameContext, Instance instance, Point prev, Block block) {
        for(Player player : instance.getPlayers()) {
            player.sendPacket(new BlockChangePacket(prev, block));
        }
    }
}
