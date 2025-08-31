package at.nopro.entityLoader;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityLoader extends AnvilLoader {
    private final Path entityPath;
    public EntityLoader(Path path) {
        super(path);
        this.entityPath = path.resolve("entities");
    }

    public EntityLoader(String path) {
        this(Path.of(path));
    }

    @Override
    public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        try {
            loadMCA(instance, chunkX, chunkZ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return super.loadChunk(instance, chunkX, chunkZ);
    }

    private @Nullable RegionFile getMCAFile(int chunkX, int chunkZ) {
        int regionX = CoordConversion.chunkToRegion(chunkX);
        int regionZ = CoordConversion.chunkToRegion(chunkZ);
        String fileName = RegionFile.getFileName(regionX, regionZ);
        try {
            Path regionPath = this.entityPath.resolve(fileName);
            if (!Files.exists(regionPath, new LinkOption[0])) {
                return null;
            } else {
                try {
                    return new RegionFile(regionPath);
                } catch (IOException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                    return null;
                }
            }
        } finally {

        }
    }

    private boolean loadMCA(Instance instance, int chunkX, int chunkZ) throws IOException {
        RegionFile mcaFile = this.getMCAFile(chunkX, chunkZ);
        if (mcaFile == null) {
            return false;
        } else {
            CompoundBinaryTag chunkData = mcaFile.readChunkData(chunkX, chunkZ);
            if (chunkData == null) {
                return false;
            } else {
                for(BinaryTag entityTag : chunkData.getList("Entities", BinaryTagTypes.COMPOUND)) {
                    if(entityTag instanceof CompoundBinaryTag entity) {

                        Entity e;

                        String id = entity.getString("id");
                        int[] rawUUID = entity.getIntArray("UUID");
                        long msb = ((long)rawUUID[0] << 32) | rawUUID[1];
                        long lsb = ((long)rawUUID[2] << 32) | rawUUID[3];

                        e = new Entity(MetadataMapper.MAP.get(id), new UUID(msb, lsb));

                        EntityMeta meta = e.getEntityMeta();

                        meta.setNotifyAboutChanges(false);
                        MetadataMapper.META_CONSUMER.get(id).accept(entity, meta);
                        meta.setNotifyAboutChanges(true);

                        ListBinaryTag listBinaryTag = entity.getList("Pos");

                        Pos p = new Pos(listBinaryTag.getDouble(0), listBinaryTag.getDouble(1), listBinaryTag.getDouble(2));
                        if(instance instanceof InstanceContainer container) {
                            for(SharedInstance sharedInstance : container.getSharedInstances()) {
                                e.setInstance(sharedInstance, p);
                            }
                        }
                        e.setInstance(instance, p);
                        System.out.println(e);
                    }
                }

            }
        }
        return false;
    }
}
