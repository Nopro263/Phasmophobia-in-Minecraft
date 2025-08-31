package at.nopro.minestomTest.phasmo;

import at.nopro.entityLoader.EntityLoader;
import at.nopro.entityLoader.MetadataMapper;
import at.nopro.minestomTest.ext.ReadonlyInstanceView;
import at.nopro.minestomTest.phasmo.maps.Tanglewood_Drive;
import at.nopro.minestomTest.phasmo.utils.DoorUtils;
import at.nopro.minestomTest.phasmo.utils.UnsafeHackyThings;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.GlowItemFrameMeta;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Main {
    public static ReadonlyInstanceView PHASMO;
    public static GlobalEventHandler EVENT;


    public static void main(String[] args) throws NoSuchFieldException {
        MinecraftServer minecraftServer = MinecraftServer.init();
        EVENT = MinecraftServer.getGlobalEventHandler();

        MetadataMapper.init();
        MinecraftServer.getCommandManager().register(new TestCommand());

        setupInstance();
        setSpawn();

        setupDoorEvent();
        //new EntityLoader("/home/noah/.local/share/multimc/instances/1.21.8_2/.minecraft/saves/6_tanglewood").loadChunk(PHASMO, 0, 0);
        System.out.println(Block.IRON_TRAPDOOR.registry().occludes());

        byte packedFlags = UnsafeHackyThings.get(Block.IRON_TRAPDOOR.registry(), "packedFlags", byte.class);
        packedFlags |= 8;

        UnsafeHackyThings.set(Block.IRON_TRAPDOOR.registry(), "packedFlags", packedFlags);

        System.out.println(Block.IRON_TRAPDOOR.registry().occludes());

        minecraftServer.start("127.0.0.1", 25565);
    }

    private static void setupInstance() {
        InstanceContainer instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        instanceContainer.setChunkLoader(new EntityLoader("/home/noah/.local/share/multimc/instances/1.21.8_2/.minecraft/saves/6_tanglewood"));
        instanceContainer.setGenerator(unit -> {unit.modifier().fillHeight(-50,-49, Block.STONE);});
        instanceContainer.setChunkSupplier(LightingChunk::new);

        PHASMO = new ReadonlyInstanceView(UUID.randomUUID(), instanceContainer);
        MinecraftServer.getInstanceManager().registerSharedInstance(PHASMO);

        MapMeta mapMeta = new Tanglewood_Drive();

        PHASMO.loadAll(mapMeta);
        GhostCreature ghostCreature = new GhostCreature(mapMeta);
        ghostCreature.setInstance(PHASMO, new Pos(-5,-42,-6));

        setupMaps();

        Random r = new Random();

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            GhostActivityManager.addActivity(r.nextInt(0,11));

            LargeGraphics2DFramebuffer activity = new LargeGraphics2DFramebuffer(128 * 2, 128);
            activity.getRenderer().setPaint(Color.RED);
            activity.getRenderer().setStroke(new BasicStroke(2f));
            GhostActivityManager.drawActivityLine(activity.getRenderer(), 0,255, 128, 0);

            for(Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    p.sendPacket(activity.preparePacket(1, 0, 0));
                    p.sendPacket(activity.preparePacket(2, 128, 0));
            }

            LargeGraphics2DFramebuffer sanity = new LargeGraphics2DFramebuffer(128 * 2, 128);
            SanityManager.drawSanity(sanity.getRenderer(), 128 * 2, 128, 10);

            for(Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                p.sendPacket(sanity.preparePacket(3, 0, 0));
                p.sendPacket(sanity.preparePacket(4, 128, 0));
            }

            return TaskSchedule.seconds(1);
        });
    }

    private static void setSpawn() {
        EVENT.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(PHASMO);
            player.setRespawnPoint(new Pos(18,-42,6));
        });
    }

    private static void setupDoorEvent() {
        Collection<String> DOORS = List.of(
                "dark_oak_door",
                "pale_oak_door"
        );
        EVENT.addListener(PlayerBlockInteractEvent.class, event -> {
            if(DOORS.contains(event.getBlock().key().value())) {
                DoorUtils.toggleDoubleDoor(event.getInstance(), event.getBlockPosition());
            }
        });
    }

    public static void setupMaps() {
        setupMap(23,-40,5,Direction.WEST,1);
        setupMap(23,-40,6,Direction.WEST,2);
        setupMap(23,-41,5,Direction.WEST,3);
        setupMap(23,-41,6,Direction.WEST,4);
    }

    private static void setupMap(int x, int y, int z, Direction d, int mapId) {
        Pos p = new Pos(x, y, z);
        Entity e = new Entity(EntityType.GLOW_ITEM_FRAME);
        if(e.getEntityMeta() instanceof GlowItemFrameMeta itemFrameMeta) {
            itemFrameMeta.setHasNoGravity(true);
            itemFrameMeta.setDirection(d);
            itemFrameMeta.setRotation(Rotation.NONE);
            itemFrameMeta.setItem(ItemStack.of(Material.FILLED_MAP).with(DataComponents.MAP_ID, mapId));
        }
        e.setInstance(PHASMO, p);
    }
}
