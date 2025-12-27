package at.nopro.phasmo.lightingv3;

import net.minestom.server.instance.heightmap.Heightmap;
import org.jetbrains.annotations.Range;

public final class LightCompute {
    public static final byte[] EMPTY_CONTENT = new byte[2048];


    private LightCompute() {
    }

    public static byte[] bake(byte[]... content) {
        byte[] out = new byte[2048];

        for (byte[] c : content) {
            if (c == null) {
                //throw new NullPointerException("content contains null values");
                c = EMPTY_CONTENT;
            }

            internalBake(c, out);
        }

        return out;
    }

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

    public static void setLight(byte[] light, @Range(from = 0, to = 15) int x, @Range(from = 0, to = 15) int y, @Range(from = 0, to = 15) int z, @Range(from = 0, to = 15) int level) {
        int index = x | z << 4 | y << 8;
        byte value = light[index >> 1];
        int shift = ( index & 1 ) << 2;
        int inverseShift = ( ( ~index & 1 ) ) << 2;

        value &= (byte) ( 15 << inverseShift );
        value |= (byte) ( level << shift );

        light[index >> 1] = value;
    }

    public static void computeSectionVanLight(byte[] light, int vanLevel, int sectionStartY, int sectionStartX, int sectionStartZ, VanLightSource vanLightSource) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    if (
                            vanLightSource.point1().blockX() <= x + sectionStartX && vanLightSource.point2().blockX() >= x + sectionStartX &&
                                    vanLightSource.point1().blockY() <= y + sectionStartY && vanLightSource.point2().blockY() >= y + sectionStartY &&
                                    vanLightSource.point1().blockZ() <= z + sectionStartZ && vanLightSource.point2().blockZ() >= z + sectionStartZ
                    ) {
                        setLight(light, x, y, z, vanLevel);
                    }
                }
            }
        }
    }

    public static @Range(from = 0, to = 15) int getLight(byte[] light, @Range(from = 0, to = 15) int x, @Range(from = 0, to = 15) int y, @Range(from = 0, to = 15) int z) {
        int index = x | z << 4 | y << 8;
        int value = light[index >>> 1];
        return value >>> ( ( index & 1 ) << 2 ) & 15;
    }
}
