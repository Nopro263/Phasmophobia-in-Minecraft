package at.nopro.phasmo.game;

import at.nopro.entityLoader.EntityLoader;
import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.ghost.BaseGhost;
import at.nopro.phasmo.content.ghost.TestGhost;
import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.entity.ai.PathCache;
import at.nopro.phasmo.event.*;
import at.nopro.phasmo.lightingv2.PhasmoChunk;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import org.jetbrains.annotations.NotNull;

public class GameContext {
    private final MapContext mapContext;
    public BaseGhost entity;
    private InstanceContainer instance;
    private PathCache pathCache;
    private DisplayManager displayManager;
    private CameraManager cameraManager;
    private ActivityManager activityManager;
    private ScopedScheduler scheduler;
    private RoomManager roomManager;
    private PlayerManager playerManager;
    private EventNode<@NotNull Event> eventNode;
    private EventNode<@NotNull Event> monitoringEventNode;
    private Player camPlayer;

    public GameContext(MapContext mapContext) {
        this.mapContext = mapContext;
        this.load();
    }

    private void load() {
        this.scheduler = new ScopedScheduler();
        this.playerManager = new PlayerManager(this);
        this.displayManager = new DisplayManager(this);
        this.roomManager = new RoomManager(this);

        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(new EntityLoader(mapContext.worldPath(), (e) -> {
            e = this.displayManager.modifyEntity(e);
            e = this.roomManager.parseEntity(e);
            return e;
        }));
        instance.setChunkSupplier(PhasmoChunk::new);
        instance.setTimeRate(0);

        instance.setTime(mapContext.time());

        int cx1 = CoordConversion.globalToChunk(mapContext.lowerEnd().x() - 1);
        int cz1 = CoordConversion.globalToChunk(mapContext.lowerEnd().z() - 1);

        int cx2 = CoordConversion.globalToChunk(mapContext.upperEnd().x() + 1);
        int cz2 = CoordConversion.globalToChunk(mapContext.upperEnd().z() + 1);

        long start = System.currentTimeMillis();

        for (int i = cx1; i <= cx2; i++) {
            for (int j = cz1; j <= cz2; j++) {
                instance.loadChunk(i, j).join();
            }
        }

        LightingChunk.relight(instance, instance.getChunks());

        System.out.println("Loaded chunks in " + ( System.currentTimeMillis() - start ) + "ms");

        start = System.currentTimeMillis();

        pathCache = PathCache.compute(
                (short) mapContext.lowerEnd().blockX(),
                (short) mapContext.lowerEnd().blockY(),
                (short) mapContext.lowerEnd().blockZ(),
                (short) mapContext.upperEnd().blockX(),
                (short) mapContext.upperEnd().blockY(),
                (short) mapContext.upperEnd().blockZ(),
                instance
        );

        System.out.println("Generated pathfinding map in " + ( System.currentTimeMillis() - start ) + "ms");

        this.activityManager = new ActivityManager(this);
        displayManager.init();

        this.eventNode = MinecraftServer.getGlobalEventHandler().addChild(EventNode.type("phasmo", EventFilter.ALL));
        this.monitoringEventNode = EventNode.all("phasmo-monitor");
        this.monitoringEventNode.setPriority(99);
        this.eventNode.addChild(this.monitoringEventNode);

        listenToGlobalEvent(EmfEvent.class);
        listenToGlobalEvent(TemperatureEvent.class);
        listenToGlobalEvent(InstanceTickEvent.class);

        listenToEntityAttackEvent(EntityAttackEvent.class);
        listenToEntityEvent(EntityTeleportEvent.class);
        listenToEntityEvent(PlayerMoveEvent.class);
        listenToEntityEvent(PlayerBlockInteractEvent.class);
        listenToEntityEvent(PlayerChangeHeldSlotEvent.class);
        listenToEntityEvent(AfterDropEvent.class);
        listenToEntityEvent(AfterPickupEvent.class);

        this.entity = new TestGhost(this);
        this.entity.setInstance(instance, new Pos(-8, -42, 3));

        this.cameraManager = new CameraManager(this);

        this.monitoringEventNode.addListener(EmfEvent.class, event -> {
            activityManager.onGhostEvent(event);
            playerManager.onGhostEvent(event);
        });

        this.monitoringEventNode.addListener(SanityDrainEvent.class, event -> {
            playerManager.onSanityDrain(event);
        });

        this.monitoringEventNode.addListener(PlayerDieEvent.class, event -> {
            playerManager.onPlayerDie(event);
        });
    }

    private void listenToGlobalEvent(Class<? extends Event> clazz) {
        this.monitoringEventNode.addListener(clazz, (event) -> {
            for (Player player : playerManager.getPlayers()) {
                ItemReference ref = ItemTracker.track(player, player.getHeldSlot());

                Equipment equipment = EquipmentManager.getEquipment(ref.get());
                if (equipment != null) {
                    equipment.handle(event, player, ref);
                }

                ref = ItemTracker.track(player, -1);

                equipment = EquipmentManager.getEquipment(ref.get());
                if (equipment != null) {
                    equipment.handle(event, player, ref);
                }

            }
            for (Entity entity : instance.getEntities()) {
                if (!( entity instanceof ItemEntity itemEntity )) {
                    continue;
                }

                ItemReference ref = ItemTracker.track(itemEntity);

                Equipment equipment = EquipmentManager.getEquipment(ref.get());
                if (equipment == null) {
                    continue;
                }
                equipment.handle(event, itemEntity, ref);
            }
        });
    }

    private void listenToEntityAttackEvent(Class<? extends EntityAttackEvent> clazz) {
        this.monitoringEventNode.addListener(clazz, (event) -> {
            ItemReference ref;
            if (ItemEntity.get(event.getTarget()) instanceof ItemEntity itemEntity) {
                ref = ItemTracker.track(itemEntity);
            } else {
                return;
            }

            Equipment equipment = EquipmentManager.getEquipment(ref.get());
            if (equipment == null) {
                return;
            }
            equipment.handle(event, event.getTarget(), ref);
        });
    }

    private void listenToEntityEvent(Class<? extends EntityEvent> clazz) {
        this.monitoringEventNode.addListener(clazz, (event) -> {
            ItemReference ref;
            if (event.getEntity() instanceof Player player) {
                ref = ItemTracker.track(player, player.getHeldSlot());

                ItemReference ref2 = ItemTracker.track(player, 45);
                Equipment equipment2 = EquipmentManager.getEquipment(ref2.get());
                if (equipment2 != null) {
                    equipment2.handle(event, event.getEntity(), ref2);
                }
            } else if (ItemEntity.get(event.getEntity()) instanceof ItemEntity itemEntity) {
                ref = ItemTracker.track(itemEntity);
            } else {
                return;
            }

            Equipment equipment = EquipmentManager.getEquipment(ref.get());
            if (equipment == null) {
                return;
            }
            equipment.handle(event, event.getEntity(), ref);
        });
    }

    public @NotNull ScopedScheduler getScheduler() {
        return scheduler;
    }

    public @NotNull ActivityManager getActivityManager() {
        return activityManager;
    }

    public @NotNull Player getCamPlayer() {
        return camPlayer;
    }

    public void setCamPlayer(Player camPlayer) {
        this.camPlayer = camPlayer;
    }

    public @NotNull MapContext getMapContext() {
        return mapContext;
    }

    public @NotNull Instance getInstance() {
        return instance;
    }

    public @NotNull PathCache getPathCache() {
        return pathCache;
    }

    public @NotNull EventNode<@NotNull Event> getEventNode() {
        return eventNode;
    }

    public @NotNull DisplayManager getDisplayManager() {
        return displayManager;
    }

    public @NotNull CameraManager getCameraManager() {
        return cameraManager;
    }

    public @NotNull RoomManager getRoomManager() {
        return roomManager;
    }

    public @NotNull PlayerManager getPlayerManager() {
        return playerManager;
    }
}
