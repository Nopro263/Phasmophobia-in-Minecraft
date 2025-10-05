package at.nopro.phasmo.game;

import at.nopro.entityLoader.EntityLoader;
import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.ghost.TestGhost;
import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.entity.ai.PathCache;
import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.event.PhasmoEvent;
import at.nopro.phasmo.event.TemperatureEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventBinding;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class GameContext {
    private final MapContext mapContext;
    private InstanceContainer instance;
    private PathCache pathCache;
    private DisplayManager displayManager;
    private CameraManager cameraManager;
    private ActivityManager activityManager;
    private ScopedScheduler scheduler;
    private RoomManager roomManager;

    private EventNode<@NotNull Event> eventNode;
    private EventNode<@NotNull Event> monitoringEventNode;
    public PhasmoEntity entity;
    private Player camPlayer;

    public GameContext(MapContext mapContext) {
        this.mapContext = mapContext;
        this.load();
    }

    private void load() {
        this.scheduler = new ScopedScheduler();
        this.displayManager = new DisplayManager(this);
        this.roomManager = new RoomManager(this);

        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(new EntityLoader(mapContext.worldPath(), (e) -> {
            e = this.displayManager.modifyEntity(e);
            e = this.roomManager.parseEntity(e);
            return e;
        }));
        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);

        instance.setTime(mapContext.time());

        int cx1 = CoordConversion.globalToChunk(mapContext.lowerEnd().x() - 1);
        int cz1 = CoordConversion.globalToChunk(mapContext.lowerEnd().z() - 1);

        int cx2 = CoordConversion.globalToChunk(mapContext.upperEnd().x() + 1);
        int cz2 = CoordConversion.globalToChunk(mapContext.upperEnd().z() + 1);

        long start = System.currentTimeMillis();

        for (int i = cx1; i <= cx2; i++) {
            for (int j = cz1; j <= cz2; j++) {
                instance.loadChunk(i,j).join();
            }
        }

        LightingChunk.relight(instance, instance.getChunks());

        System.out.println("Loaded chunks in " + (System.currentTimeMillis() - start) + "ms");

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

        System.out.println("Generated pathfinding map in " + (System.currentTimeMillis() - start) + "ms");

        this.activityManager = new ActivityManager(this);
        displayManager.init();

        this.eventNode = MinecraftServer.getGlobalEventHandler().addChild(EventNode.type("phasmo", EventFilter.ALL));
        this.monitoringEventNode = EventNode.all("phasmo-monitor");
        this.monitoringEventNode.setPriority(99);
        this.eventNode.addChild(this.monitoringEventNode);

        listenToGlobalEvent(GhostEvent.class);
        listenToGlobalEvent(TemperatureEvent.class);

        listenToEntityAttackEvent(EntityAttackEvent.class);
        listenToEntityEvent(EntityTeleportEvent.class);
        listenToEntityEvent(PlayerMoveEvent.class);
        listenToEntityEvent(PlayerBlockInteractEvent.class);
        listenToEntityEvent(PlayerChangeHeldSlotEvent.class);

        this.entity = new TestGhost(this);
        this.entity.setInstance(instance, new Pos(-8, -42, 3));

        this.cameraManager = new CameraManager(this);

        this.monitoringEventNode.addListener(GhostEvent.class, event -> {
            activityManager.onGhostEvent(event);
        });
    }

    private void listenToEntityEvent(Class<? extends EntityEvent> clazz) {
        this.monitoringEventNode.addListener(clazz, (event) -> {
            ItemReference ref;
            if(event.getEntity() instanceof Player player) {
                ref = ItemTracker.track(player, player.getHeldSlot());
            } else if (ItemEntity.get(event.getEntity()) instanceof ItemEntity itemEntity) {
                ref = ItemTracker.track(itemEntity);
            } else {
                return;
            }

            Equipment equipment = EquipmentManager.getEquipment(ref.get());
            if(equipment == null) {
                return;
            }
            equipment.handle(event, event.getEntity(), ref);
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
            if(equipment == null) {
                return;
            }
            equipment.handle(event, event.getTarget(), ref);
        });
    }

    private void listenToGlobalEvent(Class<? extends Event> clazz) {
        this.monitoringEventNode.addListener(clazz, (event) -> {
            for(Player player : instance.getPlayers()) {
                ItemReference ref = ItemTracker.track(player, player.getHeldSlot());

                Equipment equipment = EquipmentManager.getEquipment(ref.get());
                if(equipment == null) {
                    continue;
                }
                equipment.handle(event, player, ref);
            }
            for(Entity entity : instance.getEntities()) {
                if(!(entity instanceof ItemEntity itemEntity)) {
                    continue;
                }

                ItemReference ref = ItemTracker.track(itemEntity);

                Equipment equipment = EquipmentManager.getEquipment(ref.get());
                if(equipment == null) {
                    continue;
                }
                equipment.handle(event, itemEntity, ref);
            }
        });
    }

    public ScopedScheduler getScheduler() {
        return scheduler;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public Player getCamPlayer() {
        return camPlayer;
    }

    public void setCamPlayer(Player camPlayer) {
        this.camPlayer = camPlayer;
    }

    public MapContext getMapContext() {
        return mapContext;
    }

    public Instance getInstance() {
        return instance;
    }

    public PathCache getPathCache() {
        return pathCache;
    }

    public EventNode<@NotNull Event> getEventNode() {
        return eventNode;
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }
}
