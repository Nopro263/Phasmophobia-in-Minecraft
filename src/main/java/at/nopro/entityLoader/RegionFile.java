package at.nopro.entityLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO.Compression;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

final class RegionFile implements AutoCloseable {
    private static final int MAX_ENTRY_COUNT = 1024;
    private static final int SECTOR_SIZE = 4096;
    private static final int SECTOR_1MB = 256;
    private static final int HEADER_LENGTH = 8192;
    private static final int CHUNK_HEADER_LENGTH = 5;
    private static final int COMPRESSION_ZLIB = 2;
    private static final BinaryTagIO.Reader TAG_READER = BinaryTagIO.unlimitedReader();
    private static final BinaryTagIO.Writer TAG_WRITER = BinaryTagIO.writer();
    private final ReentrantLock lock = new ReentrantLock();
    private final RandomAccessFile file;
    private final int[] locations = new int[1024];
    private final int[] timestamps = new int[1024];
    private final BitSet freeSectors = new BitSet(2);
    private final ByteBuffer headerBuffer = ByteBuffer.allocate(8192);
    private boolean headerDirty = false;

    public static String getFileName(int regionX, int regionZ) {
        return "r." + regionX + "." + regionZ + ".mca";
    }

    public RegionFile(Path path) throws IOException {
        this.file = new RandomAccessFile(path.toFile(), "rw");
        this.readHeader();
    }

    public boolean hasChunkData(int chunkX, int chunkZ) {
        this.lock.lock();

        boolean var3;
        try {
            var3 = this.locations[this.getChunkIndex(chunkX, chunkZ)] != 0;
        } finally {
            this.lock.unlock();
        }

        return var3;
    }

    public @Nullable CompoundBinaryTag readChunkData(int chunkX, int chunkZ) throws IOException {
        this.lock.lock();

        Object var3;
        try {
            if (this.hasChunkData(chunkX, chunkZ)) {
                int location = this.locations[this.getChunkIndex(chunkX, chunkZ)];
                this.file.seek((long)(location >> 8) * 4096L);
                int length = this.file.readInt();
                int compressionType = this.file.readByte();
                BinaryTagIO.Compression var10000;
                switch (compressionType) {
                    case 1 -> var10000 = Compression.GZIP;
                    case 2 -> var10000 = Compression.ZLIB;
                    case 3 -> var10000 = Compression.NONE;
                    default -> throw new IOException("Unsupported compression type: " + compressionType);
                }

                BinaryTagIO.Compression compression = var10000;
                byte[] data = new byte[length - 1];
                this.file.read(data);
                CompoundBinaryTag var8 = TAG_READER.read(new ByteArrayInputStream(data), compression);
                return var8;
            }

            var3 = null;
        } finally {
            this.lock.unlock();
        }

        return (CompoundBinaryTag)var3;
    }

    public void writeChunkData(int chunkX, int chunkZ, CompoundBinaryTag data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TAG_WRITER.writeNamed(Map.entry("", data), out, Compression.ZLIB);
        byte[] dataBytes = out.toByteArray();
        int chunkLength = 5 + dataBytes.length;
        int sectorCount = (int)Math.ceil((double)chunkLength / (double)4096.0F);
        Check.stateCondition(sectorCount >= 256, "Chunk data is too large to fit in a region file");
        this.lock.lock();

        try {
            int chunkIndex = this.getChunkIndex(chunkX, chunkZ);
            int oldLocation = this.locations[chunkIndex];
            int firstSector = this.findFreeSectors(sectorCount);
            if (firstSector == -1) {
                firstSector = this.allocSectors(sectorCount);
            }

            int newLocation = firstSector << 8 | sectorCount;
            this.markLocation(oldLocation, true);
            this.markLocation(newLocation, false);
            this.file.seek((long)firstSector * 4096L);
            this.file.writeInt(chunkLength);
            this.file.writeByte(2);
            this.file.write(dataBytes);
            this.locations[chunkIndex] = newLocation;
            this.timestamps[chunkIndex] = (int)(System.currentTimeMillis() / 1000L);
            this.writeHeader();
        } finally {
            this.lock.unlock();
        }

    }

    public void close() throws IOException {
        this.file.close();
    }

    private int getChunkIndex(int chunkX, int chunkZ) {
        return CoordConversion.chunkToRegionLocal(chunkZ) << 5 | CoordConversion.chunkToRegionLocal(chunkX);
    }

    private void readHeader() throws IOException {
        this.file.seek(0L);
        if (this.file.length() < 8192L) {
            this.file.write(new byte[8192]);
        }

        long totalSectors = (this.file.length() - 1L) / 4096L + 1L;
        this.freeSectors.set(0, (int)totalSectors);
        this.freeSectors.clear(0);
        this.freeSectors.clear(1);
        this.file.seek(0L);
        byte[] headerData = new byte[8192];
        this.file.readFully(headerData);
        this.headerBuffer.clear();
        this.headerBuffer.put(headerData);
        this.headerBuffer.flip();

        for(int i = 0; i < 1024; ++i) {
            int location = this.locations[i] = this.headerBuffer.getInt();
            if (location != 0) {
                this.markLocationInBitSet(location, false);
            }
        }

        for(int i = 0; i < 1024; ++i) {
            this.timestamps[i] = this.headerBuffer.getInt();
        }

        this.headerDirty = false;
    }

    private void writeHeader() throws IOException {
        if (this.headerDirty) {
            this.headerBuffer.clear();

            for(int location : this.locations) {
                this.headerBuffer.putInt(location);
            }

            for(int timestamp : this.timestamps) {
                this.headerBuffer.putInt(timestamp);
            }

            this.file.seek(0L);
            this.file.write(this.headerBuffer.array());
            this.headerDirty = false;
        }
    }

    private int findFreeSectors(int length) {
        int nextClear;
        for(int start = this.freeSectors.nextSetBit(0); start != -1 && start + length <= this.freeSectors.size(); start = this.freeSectors.nextSetBit(nextClear)) {
            nextClear = this.freeSectors.nextClearBit(start);
            if (nextClear >= start + length) {
                return start;
            }
        }

        return -1;
    }

    private int allocSectors(int count) throws IOException {
        long eof = this.file.length();
        this.file.seek(eof);
        byte[] emptySector = new byte[4096];
        int startSector = (int)(eof / 4096L);

        for(int i = 0; i < count; ++i) {
            this.freeSectors.set(startSector + i, true);
            this.file.write(emptySector);
        }

        return startSector;
    }

    private void markLocation(int location, boolean free) {
        this.markLocationInBitSet(location, free);
        this.headerDirty = true;
    }

    private void markLocationInBitSet(int location, boolean free) {
        int sectorCount = location & 255;
        int sectorStart = location >> 8;
        Check.stateCondition(sectorStart + sectorCount > this.freeSectors.size(), "Invalid sector count");
        this.freeSectors.set(sectorStart, sectorStart + sectorCount, free);
    }
}
