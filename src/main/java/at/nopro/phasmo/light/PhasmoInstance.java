package at.nopro.phasmo.light;

import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.equipment.Flashlight;
import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.ItemReference;
import at.nopro.phasmo.game.ItemTracker;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PhasmoInstance extends InstanceContainer {
    private final GameContext gameContext;

    public static final DimensionType DIMENSION_TYPE = DimensionType.builder()
            .logicalHeight(0)
            .build();

    public static RegistryKey<DimensionType> DIMENSION_TYPE_REGISTRY;

    public PhasmoInstance(GameContext gameContext) {
        super(UUID.randomUUID(), DIMENSION_TYPE_REGISTRY);
        MinecraftServer.getInstanceManager().registerInstance(this);

        this.gameContext = gameContext;

        gameContext.getScheduler().run("light" + this.hashCode(), () -> {
            this.updateDynamicLight();
            return TaskSchedule.nextTick();
        });
    }

    public static void registerDimensionType() {
        DIMENSION_TYPE_REGISTRY = MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("phasmo", "ingame"),
                DIMENSION_TYPE
        );
    }

    public GameContext getGameContext() {
        return gameContext;
    }

    public void updateDynamicLight() {
        for (Chunk c : getChunks()) {
            if (c instanceof IngamePhasmoChunk i) {
                i.clearDynamicLight();
            }
        }

        Set<IngamePhasmoChunk> chunksToUpdate = new HashSet<>();

        for (Entity entity : getEntities()) {
            if (getChunkAt(entity.getPreviousPosition()) instanceof IngamePhasmoChunk chunk) {
                chunksToUpdate.add(chunk);
            }

            Pos lightSource = getLightSourceFromEntity(entity);
            if (lightSource == null) {
                continue;
            }

            if (getChunkAt(lightSource) instanceof IngamePhasmoChunk chunk) {
                chunk.addDynamicLightSource(lightSource);
                chunksToUpdate.add(chunk);
            }
        }

        for (IngamePhasmoChunk chunk : chunksToUpdate) {
            chunk.calculateDynamicLight();
            chunk.invalidate();
            chunk.resendLight();
        }
    }

    private @Nullable Pos getLightSourceFromEntity(Entity entity) {
        if (entity instanceof Player player) {
            ItemReference ref = ItemTracker.track(player, player.getHeldSlot());
            Equipment mainhandEquipment = EquipmentManager.getEquipment(ref.get());
            if (mainhandEquipment instanceof Flashlight) {
                return player.getPosition();
            }
            ref = ItemTracker.track(player, -1);
            Equipment offhandEquipment = EquipmentManager.getEquipment(ref.get());
            if (offhandEquipment instanceof Flashlight) {
                return player.getPosition();
            }
        }

        if (entity instanceof ItemEntity itemEntity) {
            ItemReference ref = ItemTracker.track(itemEntity);
            Equipment equipment = EquipmentManager.getEquipment(ref.get());

            if (equipment instanceof Flashlight) {
                return itemEntity.getPosition();
            }
        }


        return null;
    }

    public void recalculateFullLight() {
        for (Chunk c : getChunks()) {
            if (c instanceof IngamePhasmoChunk i) {
                i.clearLight();
            }
        }

        for (Chunk c : getChunks()) {
            if (c instanceof IngamePhasmoChunk i) {
                i.calculateInitialLight();
            }
        }

        for (Chunk c : getChunks()) {
            if (c instanceof IngamePhasmoChunk i) {
                i.invalidate();
            }
        }
    }
}
