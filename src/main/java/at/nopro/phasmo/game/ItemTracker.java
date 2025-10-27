package at.nopro.phasmo.game;

import at.nopro.phasmo.Pair;
import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.event.AfterDropEvent;
import at.nopro.phasmo.event.AfterPickupEvent;
import at.nopro.phasmo.event.PlaceEquipmentEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerPickEntityEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ItemTracker {
    private static Map<Pair<Player, Integer>, ItemReference> playerSlotMap = new HashMap<>();
    private static Map<Entity, ItemReference> itemMap = new HashMap<>();

    public static void init() {
        EventNode<@NotNull Event> node = EventNode.all("phasmo-monitor");
        node.setPriority(90);
        MinecraftServer.getGlobalEventHandler().addChild(node);

        node.addListener(ItemDropEvent.class, ItemTracker::handleDrop);
        node.addListener(PlaceEquipmentEvent.class, ItemTracker::handlePlace);
        node.addListener(PlayerSwapItemEvent.class, ItemTracker::handleSwap);
        node.addListener(PlayerPickEntityEvent.class, ItemTracker::handlePickUp);
        node.addListener(InventoryPreClickEvent.class, ItemTracker::handleInventoryClick);
        node.addListener(InventoryClickEvent.class, ItemTracker::handleInventoryClick);
        node.addListener(InventoryItemChangeEvent.class, ItemTracker::handleInventoryItemChange);
    }

    private static void handlePickUp(PlayerPickEntityEvent event) {
        Entity entity = event.getTarget();
        if(entity == null) return;

        Entity vehicle = entity.getVehicle();
        if(vehicle != null) {
            if (vehicle instanceof ItemEntity itemEntity) {
                ItemStack itemStack = itemEntity.getItem();
                final Player player = event.getPlayer();
                int slot = player.getHeldSlot();

                if(!player.getInventory().getItemStack(slot).isAir()) return;

                if(itemMap.containsKey(itemEntity)) {
                    ItemReference entityTracker = itemMap.remove(itemEntity);
                    entityTracker.setInPlayerInventory(player, slot);

                    Pair<Player, Integer> mainHandPair = new Pair<>(player, slot);
                    playerSlotMap.put(mainHandPair, entityTracker);
                }

                player.getInventory().setItemStack(slot, itemStack);

                GameContext gameContext = GameManager.getGame(event.getInstance());
                gameContext.getEventNode().call(new AfterPickupEvent(itemEntity, gameContext, player));

                vehicle.remove();
                entity.remove();
            }
        }
    }

    private static void handlePlace(PlaceEquipmentEvent event) {
        final Player player = (Player) event.getItemReference().getContainingEntity(); // if this errors, how does a non-player place equipment?
        int mainHandSlot = player.getHeldSlot();
        Pair<Player, Integer> mainHandPair = new Pair<>(player, mainHandSlot);
        ItemReference mainHandTracker = playerSlotMap.get(mainHandPair);

        ItemEntity itemEntity = new ItemEntity(event.getItemReference().get());

        if(mainHandTracker != null) {
            player.setItemInMainHand(ItemStack.AIR);
            mainHandTracker.setAsEntity(itemEntity);
            playerSlotMap.remove(mainHandPair);
            itemMap.put(itemEntity, mainHandTracker);
        }

        itemEntity.setInstance(event.getGameContext().getInstance(), event.getPos());
    }

    private static void handleDrop(ItemDropEvent event) {
        final Player player = event.getPlayer();
        int mainHandSlot = player.getHeldSlot();
        Pair<Player, Integer> mainHandPair = new Pair<>(player, mainHandSlot);
        ItemReference mainHandTracker = playerSlotMap.get(mainHandPair);

        ItemEntity itemEntity = new ItemEntity(event.getItemStack());

        if(mainHandTracker != null) {
            player.setItemInMainHand(ItemStack.AIR);
            mainHandTracker.setAsEntity(itemEntity);
            playerSlotMap.remove(mainHandPair);
            itemMap.put(itemEntity, mainHandTracker);
        }

        itemEntity.setInstance(event.getInstance(), player.getPosition().withPitch(0));
        GameContext gameContext = GameManager.getGame(event.getInstance());
        gameContext.getEventNode().call(new AfterDropEvent(itemEntity, gameContext, player));
    }

    private static void handleSwap(PlayerSwapItemEvent event) {
        if(event.isCancelled()) return;

        final Player player = event.getPlayer();

        int mainHandSlot = player.getHeldSlot();
        int offHandSlot = 45;
        swapInInventory(player,mainHandSlot, offHandSlot);
    }

    private static void handleInventoryClick(InventoryPreClickEvent event) {
        if(event.getClick() instanceof Click.Left || event.getClick() instanceof Click.Right) {

        } else {
            event.setCancelled(true);
            //TODO support other clicks as well
        }
    }

    private static void handleInventoryClick(InventoryClickEvent event) {
        int slotFrom = 99;
        int slotTo = 99;

        if(event.getCursorItem().isAir() && !event.getClickedItem().isAir()) {
            slotTo = -1;
            slotFrom = event.getSlot();
        }

        if(!event.getCursorItem().isAir() && event.getClickedItem().isAir()) {
            slotTo = event.getSlot();
            slotFrom = -1;
        }

        swapInInventory(event.getPlayer(), slotFrom, slotTo);
    }

    private static void handleInventoryItemChange(InventoryItemChangeEvent event) {

    }

    private static void swapInInventory(Player player, int slot1, int slot2) {
        Pair<Player, Integer> mainHandPair = new Pair<>(player, slot1);
        Pair<Player, Integer> offHandPair = new Pair<>(player, slot2);

        ItemReference mainHandTracker = playerSlotMap.get(mainHandPair);
        ItemReference offHandTracker = playerSlotMap.get(offHandPair);

        if(mainHandTracker != null){
            mainHandTracker.setInPlayerInventory(player, slot2);
            if(offHandTracker == null) {
                playerSlotMap.remove(mainHandPair);
            }
            playerSlotMap.put(offHandPair, mainHandTracker);
        }

        if(offHandTracker != null){
            offHandTracker.setInPlayerInventory(player, slot1);
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

    public static ItemReference track(ItemEntity itemEntity) {
        if(itemMap.containsKey(itemEntity)) {
            return itemMap.get(itemEntity);
        }
        ItemReference ref = new ItemReference();
        ref.setAsEntity(itemEntity);
        itemMap.put(itemEntity, ref);
        return ref;
    }

    public static void stopTracking(ItemReference itemReference) {
        itemReference.deactivate();
        //TODO implement
    }
}

