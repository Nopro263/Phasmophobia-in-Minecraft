package at.nopro.phasmo.lighting;

import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.equipment.Flashlight;
import at.nopro.phasmo.entity.ItemEntity;
import at.nopro.phasmo.game.ItemReference;
import at.nopro.phasmo.game.ItemTracker;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhasmoChunk extends DynamicChunk {
    private final Set<NewLightingCompute.ExternalLight> externalLights;
    private final List<LightSource> lightSources;

    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);

        externalLights = new HashSet<>();
        lightSources = new ArrayList<>();

        if (chunkX == 1 && chunkZ == -1) {
            lightSources.add(new FloodedLightSource(new BlockVec(20, -42, -2), new BlockVec(3, 2, 9), 14));
        }

        if (chunkX == 1 && chunkZ == 1) {
            lightSources.add(new ConeLightSource(new Pos(23, -42, 24, -90, 0), 15, 1));
        }
    }

    public void resendLight() {
        this.sendPacketToViewers(new UpdateLightPacket(chunkX, chunkZ, createLightData(false)));
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        return LightingCompute.generateLightForChunk(this);
    }

    public List<LightSource> getLightSources() {
        List<LightSource> lightSources = new ArrayList<>(this.lightSources.stream().filter(LightSource::isActive).toList());
        Set<Entity> entities = getInstance().getChunkEntities(this);
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity itemEntity) {
                ItemReference itemReference = ItemTracker.track(itemEntity);
                Equipment equipment = EquipmentManager.getEquipment(itemReference.get());
                if (equipment instanceof Flashlight) {
                    lightSources.add(new FlashlightLightSource(itemReference));
                }
            } else if (entity instanceof Player player) {
                ItemReference itemReference = ItemTracker.track(player, player.getHeldSlot());
                Equipment equipment = EquipmentManager.getEquipment(itemReference.get());
                if (equipment instanceof Flashlight) {
                    lightSources.add(new FlashlightLightSource(itemReference));
                }
            }
        }

        return lightSources;
    }

    public LightSource getLightSourceForId(long id) {
        return lightSources.stream().filter(lightSource -> lightSource.getId() == id).findFirst().orElse(null);
    }

    public void removeLightSource(long id) {
        lightSources.removeIf(lightSource -> lightSource.getId() == id);
    }

    public void addLightSource(LightSource lightSource) {
        lightSources.add(lightSource);
    }

    public List<PhasmoChunk> getNeighbours() {
        List<PhasmoChunk> chunks = new ArrayList<>(8);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk chunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (chunk == null || chunk == this) {
                    continue;
                }

                if (!( chunk instanceof PhasmoChunk pc )) {
                    throw new RuntimeException("chunk not the expected phasmo-chunk");
                }
                chunks.add(pc);
            }
        }
        return chunks;
    }

    @ApiStatus.Internal
    void addExternalLight(NewLightingCompute.ExternalLight externalLight) {
        externalLights.add(externalLight);
    }
}
