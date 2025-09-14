package at.nopro.phasmo.game;

import at.nopro.entityLoader.EntityLoader;
import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.ghost.TestGhost;
import at.nopro.phasmo.entity.ai.PathCache;
import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.event.PhasmoEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventBinding;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class GameContext {
    private final MapContext mapContext;
    private InstanceContainer instance;
    private PathCache pathCache;
    private EventNode<@NotNull Event> eventNode;
    private EventNode<@NotNull Event> monitoringEventNode;
    public PhasmoEntity entity;

    public GameContext(MapContext mapContext) {
        this.mapContext = mapContext;
        this.load();
    }

    private void load() {
        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(new EntityLoader(mapContext.worldPath()));
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

        this.eventNode = MinecraftServer.getGlobalEventHandler().addChild(EventNode.type("phasmo", EventFilter.ALL));
        this.monitoringEventNode = EventNode.all("phasmo-monitor");
        this.monitoringEventNode.setPriority(99);
        this.eventNode.addChild(this.monitoringEventNode);

        listenTo(GhostEvent.class);

        this.entity = new TestGhost(this);
        this.entity.setInstance(instance, new Pos(-8, -42, 3));

        this.monitoringEventNode.addListener(PlayerSwapItemEvent.class, (event) -> {

        });
    }

    private void listenTo(Class<? extends Event> clazz) {
        this.monitoringEventNode.addListener(clazz, (event) -> {
            for(Player player : instance.getPlayers()) {
                Equipment equipment = EquipmentManager.getEquipment(player.getItemInMainHand());
                if(equipment == null) {
                    continue;
                }

                equipment.handle(event, player, (itemStack) -> {
                    if(itemStack != null) {
                        player.setItemInMainHand(itemStack);
                    }
                });
            }
        });
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
}
