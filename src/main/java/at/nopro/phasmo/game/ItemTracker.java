package at.nopro.phasmo.game;

import at.nopro.phasmo.Pair;
import at.nopro.phasmo.entity.ItemEntity;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemTracker {
    private static Map<Pair<Player, Integer>, ItemReference> playerSlotMap = new HashMap<>();
    private static Map<Entity, ItemReference> itemMap = new HashMap<>();

    public static void init() {
        EventNode<@NotNull Event> node = EventNode.all("phasmo-monitor");
        node.setPriority(90);
        MinecraftServer.getGlobalEventHandler().addChild(node);

        node.addListener(ItemDropEvent.class, ItemTracker::handleDrop);
        node.addListener(PlayerSwapItemEvent.class, ItemTracker::handleSwap);
    }

    private static void handleDrop(ItemDropEvent event) {
        final Player player = event.getPlayer();
        int mainHandSlot = player.getHeldSlot();
        Pair<Player, Integer> mainHandPair = new Pair<>(player, mainHandSlot);
        ItemReference mainHandTracker = playerSlotMap.get(mainHandPair);

        ItemEntity itemEntity = new ItemEntity(event.getItemStack());

        if(mainHandTracker != null) {
            mainHandTracker.setAsEntity(itemEntity);
            playerSlotMap.remove(mainHandPair);
        }

        itemEntity.setInstance(event.getInstance(), player.getPosition());
    }

    private static void handleSwap(PlayerSwapItemEvent event) {
        if(event.isCancelled()) return;

        final Player player = event.getPlayer();

        int mainHandSlot = player.getHeldSlot();
        int offHandSlot = 45;
        Pair<Player, Integer> mainHandPair = new Pair<>(player, mainHandSlot);
        Pair<Player, Integer> offHandPair = new Pair<>(player, offHandSlot);

        ItemReference mainHandTracker = playerSlotMap.get(mainHandPair);
        ItemReference offHandTracker = playerSlotMap.get(offHandPair);

        if(mainHandTracker != null){
            mainHandTracker.setInPlayerInventory(player, offHandSlot);
            if(offHandTracker == null) {
                playerSlotMap.remove(mainHandPair);
            }
            playerSlotMap.put(offHandPair, mainHandTracker);
        }

        if(offHandTracker != null){
            offHandTracker.setInPlayerInventory(player, mainHandSlot);
            if(mainHandTracker == null) {
                playerSlotMap.remove(offHandPair);
            }
            playerSlotMap.put(mainHandPair, offHandTracker);
        }
    }

    public static ItemReference track(Player player, int slot) {
        Pair<Player, Integer> pair = new Pair<>(player, slot);
        if(playerSlotMap.containsKey(pair)) {
            return playerSlotMap.get(pair);
        }
        ItemReference ref = new ItemReference();
        ref.setInPlayerInventory(player, slot);
        playerSlotMap.put(pair, ref);
        return ref;
    }

    public static void stopTracking(ItemReference itemReference) {
        itemReference.deactivate();

    }
}

