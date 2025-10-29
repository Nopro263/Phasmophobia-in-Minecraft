package at.nopro.phasmo.entity.ai;


import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Random;
import java.util.function.Predicate;

public class PathCache {
    private final int[][][] cache;
    private final int ox;
    private final int oy;
    private final int oz;
    private final int sx;
    private final int sy;
    private final int sz;
    private final Random random = new Random();

    public PathCache(int[][][] data, int ox, int oy, int oz, int sx, int sy, int sz) {
        cache = data;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
    }

    public static PathCache compute(short x1, short y1, short z1, short x2, short y2, short z2, Instance instance) {
        assert x1 <= x2;
        assert y1 <= y2;
        assert z1 <= z2;

        int ox = -x1;
        int oy = -y1;
        int oz = -z1;
        int sx = ox + x2 + 1;
        int sy = oy + y2 + 1;
        int sz = oz + z2 + 1;

        double size = ( sx * sy * sz * 4 ) / 1024.0;
        System.out.println("Generating new " + size + "KB cache");

        int[][][] data = new int[sx][sy][sz];

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                for (int k = z1; k <= z2; k++) {

                    Block currentBlock = instance.getBlock(i, j, k);
                    if (!currentBlock.isAir() && !currentBlock.key().value().contains("door")) {
                        data[i + ox][j + oy][k + oz] = 0;
                        continue;
                    }

                    Block below = instance.getBlock(i, j - 1, k);

                    if (below.isAir()) {
                        data[i + ox][j + oy][k + oz] = 0;
                        continue;
                    }

                    int d = 0;

                    for (byte l = -1; l <= 1; l++) {
                        for (byte m = -1; m <= 1; m++) {
                            for (byte n = -1; n <= 1; n++) {
                                Block b = instance.getBlock(i + l, j + m, k + n);
                                Block bl = instance.getBlock(i + l, j + m - 1, k + n);
                                if (( b.isAir() || b.key().value().contains("door") ) && !bl.isAir()) {
                                    d |= 1 << PathCache.calculateIndex(l, m, n);
                                }
                            }
                        }
                    }

                    data[i + ox][j + oy][k + oz] = d;
                }
            }
        }

        return new PathCache(data, ox, oy, oz, sx, sy, sz);
    }

    public BlockVec getRandomBlock(Predicate<BlockVec> filter) {
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(sx);
            int y = random.nextInt(sy);
            int z = random.nextInt(sz);
            if (cache[x][y][z] != 0) {
                BlockVec vec = new BlockVec(x - ox, y - oy, z - oz);
                if (filter.test(vec)) {
                    return vec;
                }
            }
        }
        throw new RuntimeException("Well Shit");
    }

    private static int calculateIndex(byte dx, byte dy, byte dz) {
        int index = 0;
        if (dx == 1) {
            index += 1;
        } else if (dx == -1) {
            index += 2;
        }
        if (dy == 1) {
            index += 3;
        } else if (dy == -1) {
            index += 6;
        }
        if (dz == 1) {
            index += 9;
        } else if (dz == -1) {
            index += 18;
        }
        return index;
    }

    public boolean canMoveTo(short x, short y, short z, byte dx, byte dy, byte dz) {
        try {
            //System.out.println(x + ":" + ox + " " + y + ":" + oy + " " + z + ":" + oz);
            int data = cache[x + ox][y + oy][z + oz];
            return ( data & ( 1 << calculateIndex(dx, dy, dz) ) ) != 0;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean isInvalid(short x, short y, short z) {
        try {
            return cache[x + ox][y + oy][z + oz] == 0;
        } catch (IndexOutOfBoundsException e) {
            return true;
        }
    }
}
