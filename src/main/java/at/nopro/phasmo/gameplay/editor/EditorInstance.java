package at.nopro.phasmo.gameplay.editor;

import at.nopro.phasmo.core.world.BaseInstance;
import at.nopro.phasmo.core.world.WorldMeta;
import at.nopro.phasmo.utils.Reflection;
import at.nopro.phasmo.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPickBlockEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.world.DimensionType;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static at.nopro.phasmo.utils.Reflection.set;

public class EditorInstance extends BaseInstance {
    private long lastSave;
    private final Map<String, Editor.MetaEntry> metaEntryMap = new HashMap<>();
    private Entity currentModifyingEntity = null;
    private Editor.MetaEntry currentModifyingEntry = null;

    public EditorInstance(DimensionType dimensionType, WorldMeta meta, String mapName) {
        super(Utils.uuidFromObject(mapName), dimensionType);
        setReadonly(false);
        setWorldMeta(meta);

        setChunkSupplier(LightingChunk::new);

        eventNode().addListener(PlayerSpawnEvent.class, this::onPlayerSpawn);
        eventNode().addListener(PlayerDisconnectEvent.class, this::onDisconnect);
        eventNode().addListener(PlayerPickBlockEvent.class, this::onBlockPick);
        eventNode().addListener(PlayerBlockInteractEvent.class, this::onBlockInteract);

        for (Field f : Reflection.getAllDeclared(meta)) {
            metaEntryMap.put(f.getName(), new Editor.MetaEntry(
                    (v) -> set(f, meta, v),
                    () -> Reflection.get(f, meta),
                    f.getType()
            ));
        }
    }

    private void onDisconnect(PlayerDisconnectEvent disconnectEvent) {
        scheduleNextTick(_ -> closeIfEmpty());
    }

    private void onBlockInteract(PlayerBlockInteractEvent blockInteractEvent) {
        ItemStack itemStack = blockInteractEvent.getPlayer().getItemInHand(blockInteractEvent.getHand());
        if (itemStack.isAir() && currentModifyingEntity != null) {
            Pos pos = blockInteractEvent.getBlockPosition().asPos().add(0.5, 0, 0.5).add(blockInteractEvent.getBlockFace().toDirection().vec());
            currentModifyingEntry.setter().accept(pos);
            showAllMetaValues();
        }
    }

    private void onPlayerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        playerSpawnEvent.getPlayer().setGameMode(GameMode.CREATIVE);
        playerSpawnEvent.getPlayer().teleport(new Pos(0, 17, 0));
    }

    private void onBlockPick(PlayerPickBlockEvent pickBlockEvent) {
        pickBlockEvent.getPlayer().getInventory().addItemStack(ItemStack.of(pickBlockEvent.getBlock().registry().material()));
    }

    public void showAllMetaValues() {
        for (var e : metaEntryMap.entrySet()) {
            if (e.getValue().type().equals(Pos.class)) {
                showPos(e.getKey(), e.getValue());
            }
        }
    }

    public void closeIfEmpty() {
        if (!getPlayers().isEmpty()) return;

        MinecraftServer.getInstanceManager().unregisterInstance(this);
    }

    private void showPos(String name, Editor.MetaEntry value) {
        Pos pos = (Pos) value.getter().get();
        UUID uuid = Utils.uuidFromObject(name);

        if (getEntityByUuid(uuid) instanceof Entity entity) {
            entity.teleport(pos);
        } else {
            Entity entity = new Entity(EntityType.ARMOR_STAND, uuid);
            entity.setNoGravity(true);
            ArmorStandMeta meta = (ArmorStandMeta) entity.getEntityMeta();
            entity.set(DataComponents.CUSTOM_NAME, Component.text(name).color(TextColor.color(255, 255, 255)));
            meta.setHasNoBasePlate(true);
            meta.setCustomNameVisible(true);

            entity.setInstance(this, pos);

            eventNode().addListener(EntityAttackEvent.class, entityAttackEvent -> {
                if (!entityAttackEvent.getTarget().equals(entity)) return;
                if (!( entityAttackEvent.getEntity() instanceof Player player )) return;

                if (entity.equals(currentModifyingEntity)) {
                    currentModifyingEntry = null;
                    currentModifyingEntity = null;
                    entity.setGlowing(false);
                    return;
                }

                if (currentModifyingEntity != null) {
                    currentModifyingEntity.setGlowing(false);
                }
                currentModifyingEntity = entity;
                currentModifyingEntry = value;
                entity.setGlowing(true);
            });
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        showAllMetaValues();
    }

    @Override
    public @NonNull CompletableFuture<Void> saveChunksToStorage() {
        lastSave = System.currentTimeMillis();
        return super.saveChunksToStorage();
    }

    public long getLastSaveTime() {
        return lastSave;
    }
}
