package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Section;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class NewLightingCompute {
    private static final int OUTSIDE_LIGHT = 7;
    private static final int INSIDE_LIGHT = 0;

    public static @NotNull LightData generateLightForChunk(PhasmoChunk chunk) {
        try {
            return generateLightForChunk(chunk, chunk.getInstance().getCachedDimensionType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull LightData generateLightForChunk(PhasmoChunk chunk, DimensionType dimensionType) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        List<Section> sections = chunk.getSections();
        List<PhasmoChunk> neighbours = chunk.getNeighbours();

        List<LightSource> lightSources = new ArrayList<>();
        lightSources.addAll(chunk.getLightSources());

        /*for (PhasmoChunk c : neighbours) {
            lightSources.addAll(c.getLightSources());
        }*/

        ArrayDeque<Light> lights = new ArrayDeque<>();

        for (LightSource lightSource : lightSources) {
            if (lightSource instanceof PointLightSource pointLightSource) {
                placeLightAt(pointLightSource.getSource(), blockMask, blockLights, dimensionType, lights, 15);
            }
        }

        while (!lights.isEmpty()) {
            Light light = lights.remove();

            placeLightAt(light.point, blockMask, blockLights, dimensionType, lights, light.level);
        }


        emptySkyMask.set(0, sections.size());
        emptySkyMask.andNot(skyMask);

        emptyBlockMask.set(0, sections.size());
        emptyBlockMask.andNot(blockMask);
        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }

    private static void placeLightAt(
            Point point,
            BitSet mask,
            List<byte[]> lightData,
            DimensionType dimensionType,
            Queue<Light> lightsToCompute,
            int level
    ) {
        int section = ( point.sectionY() + 1 ) - dimensionType.minY() / 16;

        byte[] data = getByteArrayForSection(section, mask, lightData);
        double indexAndShift = getIndexForPoint(point);

        int index = (int) indexAndShift;
        int shift = indexAndShift % 1 == 0 ? 0 : 4;

        byte o = data[index];
        o |= (byte) ( level << shift );
        data[index] = o;

        if (level > 1) {
            if (point.blockZ() % 16 != 15) {
                propagateLightTo(point.add(0, 0, 1), mask, lightData, section, lightsToCompute, level - 1);
            }
            if (point.blockX() % 16 != 15) {
                propagateLightTo(point.add(1, 0, 0), mask, lightData, section, lightsToCompute, level - 1);
            }
            if (point.blockZ() % 16 != 0) {
                propagateLightTo(point.add(0, 0, -1), mask, lightData, section, lightsToCompute, level - 1);
            }
            if (point.blockX() % 16 != 0) {
                propagateLightTo(point.add(-1, 0, 0), mask, lightData, section, lightsToCompute, level - 1);
            }
        }
    }

    static byte[] getByteArrayForSection(int sectionIndex, BitSet mask, List<byte[]> lights) {
        int arrIndex = countPrecedingBits(sectionIndex, mask);

        if (mask.get(sectionIndex)) {
            return lights.get(arrIndex);
        } else {
            byte[] arr = new byte[2048];
            lights.add(arrIndex, arr);
            mask.set(sectionIndex);
            return arr;
        }
    }

    static double getIndexForPoint(Point point) {
        int x = point.blockX() % 16;
        int y = point.blockY() % 16;
        int z = point.blockZ() % 16;

        if (x < 0) x += 16;
        if (z < 0) z += 16;
        if (y < 0) y += 16;

        return ( x + z * 16 + y * 256 ) / 2d;
    }

    private static void propagateLightTo(
            Point point,
            BitSet mask,
            List<byte[]> lightData,
            int section,
            Queue<Light> lightsToCompute,
            int newLevel
    ) {
        byte[] data = getByteArrayForSection(section, mask, lightData);
        double indexAndShift = getIndexForPoint(point);

        int index = (int) indexAndShift;
        int shift = indexAndShift % 1 == 0 ? 0 : 4;

        int oldLevel = ( data[index] >> shift ) & 15;

        if (newLevel > 0 && newLevel > oldLevel) {
            lightsToCompute.add(new Light(point, newLevel));
        }
    }

    static int countPrecedingBits(int upTo, BitSet bs) {
        int count = 0;
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            if (i >= upTo) {
                return count;
            }
            count++;
        }
        return count;
    }

    private record Light(Point point, int level) {
    }
}
