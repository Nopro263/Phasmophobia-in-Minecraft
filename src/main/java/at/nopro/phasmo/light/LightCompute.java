package at.nopro.phasmo.light;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.minestom.server.collision.Shape;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public final class LightCompute {
    public static final byte[] EMPTY_CONTENT = new byte[2048];
    static final Direction[] DIRECTIONS = Direction.values();
    static final int SECTION_SIZE = 16;


    private LightCompute() {
    }

    /**
     * combines all light-byte-arrays into a new 2048 byte big one, using the biggest value for each nibble.
     * if a passed array is null, we treat it like an empty array
     *
     * @param content the arrays to combine
     * @return a new combined byte array
     */
    public static byte[] bake(byte[]... content) {
        byte[] out = new byte[2048];

        for (byte[] c : content) {
            if (c == null) {
                c = EMPTY_CONTENT;
            }

            internalBake(c, out);
        }

        return out;
    }

    /**
     * combine two byte arrays, using the bigger nibble for each
     *
     * @param content the first array to combine
     * @param out     the second array to combine and also the destination
     */
    private static void internalBake(byte[] content, byte[] out) {
        for (int i = 0; i < content.length; ++i) {
            byte c1 = content[i];
            byte c2 = out[i];
            byte l1 = (byte) ( c1 & 15 );
            byte l2 = (byte) ( c2 & 15 );
            byte u1 = (byte) ( c1 >> 4 & 15 );
            byte u2 = (byte) ( c2 >> 4 & 15 );
            byte lower = (byte) Math.max(l1, l2);
            byte upper = (byte) Math.max(u1, u2);
            out[i] = (byte) ( lower | upper << 4 );
        }
    }

    /**
     * generates the light values for skylights
     * if the block is above the heightmap, its value is levelAbove
     * else it is zero
     *
     * @param light         output light array
     * @param heightmap     heightmap of all (motion-blocking) blocks
     * @param levelAbove    the light-level above the heightmap
     * @param sectionStartY the global Y-coordinate of the current section
     */
    public static void computeSectionSkyLight(byte[] light, Heightmap heightmap, int levelAbove, int sectionStartY) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int currentHeight = heightmap.getHeight(x, z);

                for (int y = 0; y < 16; y++) {
                    if (y + sectionStartY >= currentHeight) {
                        setLight(light, x, y, z, levelAbove);
                    } else {
                        setLight(light, x, y, z, 0);
                    }
                }
            }
        }
    }

    private static void setLight(byte[] light, @Range(from = 0, to = 15) int x, @Range(from = 0, to = 15) int y, @Range(from = 0, to = 15) int z, @Range(from = 0, to = 15) int level) {
        int index = x | z << 4 | y << 8;

        setLight(light, index, level);
    }

    private static void setLight(byte[] light, int index, @Range(from = 0, to = 15) int level) {
        byte value = light[index >> 1];
        int shift = ( index & 1 ) << 2;
        int inverseShift = ( ( ~index & 1 ) ) << 2;

        value &= (byte) ( 15 << inverseShift );
        value |= (byte) ( level << shift );

        light[index >> 1] = value;
    }

    /**
     * calculate the light level for the van
     * sets the level between the two points in vanLightSource to vanLevel
     * propagates light outwards
     *
     * @param light          the destination array
     * @param vanLevel       the light-level inside the van
     * @param sectionStartY  global Y-coordinate of the current section
     * @param sectionStartX  global X-coordinate of the current section
     * @param sectionStartZ  global Z-coordinate of the current section
     * @param blockPalette   blockPalette of the current section
     * @param vanLightSource the VanLightSource to determine the 2 points where we want light to be
     */
    public static void computeSectionVanLight(byte[] light, int vanLevel, int sectionStartY, int sectionStartX, int sectionStartZ, Palette blockPalette, VanLightSource vanLightSource) {
        IntArrayFIFOQueue shortArrayFIFOQueue = new IntArrayFIFOQueue();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    if (
                            vanLightSource.point1().blockX() <= x + sectionStartX && vanLightSource.point2().blockX() >= x + sectionStartX &&
                                    vanLightSource.point1().blockY() <= y + sectionStartY && vanLightSource.point2().blockY() >= y + sectionStartY &&
                                    vanLightSource.point1().blockZ() <= z + sectionStartZ && vanLightSource.point2().blockZ() >= z + sectionStartZ
                    ) {
                        enqueueLight(shortArrayFIFOQueue, x, y, z, vanLevel);
                    }
                }
            }
        }

        compute(blockPalette, shortArrayFIFOQueue, light);
    }

    private static void enqueueLight(IntArrayFIFOQueue queue, int x, int y, int z, int level) {
        int allowedDirections = 0b111111;
        queue.enqueue(x | ( z << 4 ) | ( y << 8 ) | ( level << 12 ) | ( allowedDirections << 16 ));
    }

    /**
     * places all lights from lightPre and then spreads all lights using a bfs-search
     * but:
     * the allowed-light-directions specify in what direction this "ray" can propagate
     * they are updated for each subsequent light after hitting a non-air block -> this removes the going around corners completely
     *
     * @param blockPalette the blockPalette for the current section
     * @param lightPre     int queue in format: [10bit unused][6bit allowed light directions][4bit light level][4bit y][4bit z][4bit x]
     * @param lightArray   the output destination
     */
    private static void compute(Palette blockPalette, IntArrayFIFOQueue lightPre, byte[] lightArray) {

        final IntArrayFIFOQueue lightSources = new IntArrayFIFOQueue();

        while (!lightPre.isEmpty()) {
            final int index = lightPre.dequeueInt();

            final int newLightLevel = ( index >> 12 ) & 15;
            final int newIndex = index & 0xFFF;

            final int oldLightLevel = getLight(lightArray, newIndex);

            if (oldLightLevel < newLightLevel) {
                setLight(lightArray, newIndex, newLightLevel);
                lightSources.enqueue((short) index);
            }
        }

        while (!lightSources.isEmpty()) {
            final int index = lightSources.dequeueInt();
            final int x = index & 15;
            final int z = ( index >> 4 ) & 15;
            final int y = ( index >> 8 ) & 15;
            final int lightLevel = ( index >> 12 ) & 15;
            final int allowedDirections = ( index >> 16 ) & 0b111111;
            final byte newLightLevel = (byte) ( lightLevel - 1 );

            int newAllowedDirections = 0;

            for (int i = 0; i < DIRECTIONS.length; i++) {
                Direction direction = DIRECTIONS[i];

                final int xO = x + direction.normalX();
                final int yO = y + direction.normalY();
                final int zO = z + direction.normalZ();

                // Handler border
                if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                    continue;
                }

                final Block currentBlock = Objects.requireNonNullElse(getBlock(blockPalette, x, y, z), Block.AIR);
                final Block propagatedBlock = Objects.requireNonNullElse(getBlock(blockPalette, xO, yO, zO), Block.AIR);

                final Shape currentShape = currentBlock.registry().occlusionShape();
                final Shape propagatedShape = propagatedBlock.registry().occlusionShape();

                final boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                if (!airAir && currentShape.isOccluded(propagatedShape, BlockFace.fromDirection(direction)))
                    continue;

                newAllowedDirections |= 1 << i;
            }

            newAllowedDirections &= allowedDirections;

            for (int i = 0; i < DIRECTIONS.length; i++) {
                if (( ( newAllowedDirections >> i ) & 1 ) == 0) continue;

                Direction direction = DIRECTIONS[i];
                final int xO = x + direction.normalX();
                final int yO = y + direction.normalY();
                final int zO = z + direction.normalZ();

                // Handler border
                if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                    continue;
                }

                // Section
                final int newIndex = xO | ( zO << 4 ) | ( yO << 8 );

                if (getLight(lightArray, newIndex) < newLightLevel) {
                    final Block currentBlock = Objects.requireNonNullElse(getBlock(blockPalette, x, y, z), Block.AIR);
                    final Block propagatedBlock = Objects.requireNonNullElse(getBlock(blockPalette, xO, yO, zO), Block.AIR);

                    final Shape currentShape = currentBlock.registry().occlusionShape();
                    final Shape propagatedShape = propagatedBlock.registry().occlusionShape();

                    final boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                    if (!airAir && currentShape.isOccluded(propagatedShape, BlockFace.fromDirection(direction)))
                        continue;

                    setLight(lightArray, newIndex, newLightLevel);
                    lightSources.enqueue(newIndex | ( newLightLevel << 12 ) | ( newAllowedDirections << 16 ));
                }
            }
        }
    }

    private static @Range(from = 0, to = 15) int getLight(byte[] light, int index) {
        int value = light[index >>> 1];
        return value >>> ( ( index & 1 ) << 2 ) & 15;
    }

    public static Block getBlock(Palette palette, int x, int y, int z) {
        return Block.fromStateId(palette.get(x, y, z));
    }

    private static @Range(from = 0, to = 15) int getLight(byte[] light, @Range(from = 0, to = 15) int x, @Range(from = 0, to = 15) int y, @Range(from = 0, to = 15) int z) {
        int index = x | z << 4 | y << 8;
        return getLight(light, index);
    }
}
