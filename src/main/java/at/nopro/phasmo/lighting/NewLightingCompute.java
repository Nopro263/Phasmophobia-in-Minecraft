package at.nopro.phasmo.lighting;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class NewLightingCompute {
    private static final int OUTSIDE_LIGHT = 7;
    private static final int INSIDE_LIGHT = 0;

    public static @NotNull LightData generateLightForChunk(PhasmoChunk chunk, Set<ExternalLight> externalLights, LightData oldLightData) {
        try {
            return generateLightForChunk(chunk, chunk.getInstance().getCachedDimensionType(), externalLights, oldLightData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull LightData generateLightForChunk(PhasmoChunk chunk, DimensionType dimensionType, Set<ExternalLight> externalLights, LightData oldLightData) {
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

        for (PhasmoChunk c : neighbours) {
            c.removeAllExternalLightsComingFromChunk(chunk);
        }


        ArrayDeque<Light> lights = new ArrayDeque<>();
        for (ExternalLight externalLight : externalLights) {
            lights.add(externalLight.getWrappedLight());
        }

        for (LightSource lightSource : lightSources) {
            placeLightAt(
                    lightSource.getPosition(),
                    lightSource,
                    chunk.getInstance(),
                    blockMask,
                    blockLights,
                    dimensionType,
                    lights,
                    lightSource.getSourceLevel()
            );
        }

        while (!lights.isEmpty()) {
            Light light = lights.remove();

            placeLightAt(light.point, light.lightSource, chunk.getInstance(), blockMask, blockLights, dimensionType, lights, light.level);
        }

        for (PhasmoChunk c : neighbours) {
            c.invalidateChunkIfLightsChanged();
        }


        emptySkyMask.set(0, sections.size());
        emptySkyMask.andNot(skyMask);

        emptyBlockMask.set(0, sections.size());
        emptyBlockMask.andNot(blockMask);
        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }

    private static void placeLightAt(
            Point point,
            @Nullable LightSource lightSource,
            Instance instance,
            BitSet mask,
            List<byte[]> lightData,
            DimensionType dimensionType,
            Queue<Light> lightsToCompute,
            int level
    ) {
        if (lightSource != null) {
            level = lightSource.modifyLevelAtPoint(point, level);
        }

        if (level == 0) return;

        int section = ( point.sectionY() + 1 ) - dimensionType.minY() / 16;

        byte[] data = getByteArrayForSection(section, mask, lightData);
        double indexAndShift = getIndexForPoint(point);

        int index = (int) indexAndShift;
        int shift = indexAndShift % 1 == 0 ? 0 : 4;

        byte o = data[index];
        o |= (byte) ( level << shift );
        data[index] = o;

        if (level > 1 && ( lightSource == null || lightSource.canPropagateFrom(point, level) )) {
            propagateLightTo(point, point.add(0, 0, 1), lightSource, instance, mask, lightData, section, lightsToCompute, level - 1);
            propagateLightTo(point, point.add(1, 0, 0), lightSource, instance, mask, lightData, section, lightsToCompute, level - 1);
            propagateLightTo(point, point.add(0, 0, -1), lightSource, instance, mask, lightData, section, lightsToCompute, level - 1);
            propagateLightTo(point, point.add(-1, 0, 0), lightSource, instance, mask, lightData, section, lightsToCompute, level - 1);
            //propagateLightTo(point, point.add(0, 1, 0), lightSource, instance, mask, lightData, section, lightsToCompute, level - 1);
            //propagateLightTo(point, point.add(0, -1, 0), lightSource, instance, mask, lightData, section, lightsToCompute, level - 1);

        }
    }

    static byte[] getByteArrayForSection(int sectionIndex, BitSet mask, List<byte[]> lights) {
        return getByteArrayForSection(sectionIndex, mask, lights, true);
    }

    static byte[] getByteArrayForSection(int sectionIndex, BitSet mask, List<byte[]> lights, boolean allowCreation) {
        int arrIndex = countPrecedingBits(sectionIndex, mask);

        if (mask.get(sectionIndex)) {
            return lights.get(arrIndex);
        } else {
            if (allowCreation) {
                byte[] arr = new byte[2048];
                lights.add(arrIndex, arr);
                mask.set(sectionIndex);
                return arr;
            } else {
                return null;
            }
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
            Point from,
            Point point,
            @Nullable LightSource lightSource,
            Instance instance,
            BitSet mask,
            List<byte[]> lightData,
            int section,
            Queue<Light> lightsToCompute,
            int newLevel
    ) {
        if (instance.getBlock(point).isSolid()) {
            return;
        }
        if (!from.sameChunk(point)) {
            if (!( instance.getChunkAt(from) instanceof PhasmoChunk owner )) {
                throw new RuntimeException("ahah");
            }

            if (instance.getChunkAt(point) instanceof PhasmoChunk pc) {
                pc.addExternalLight(new ExternalLight(owner, new Light(point, lightSource, newLevel)));
            }
            return;
        }

        byte[] data = getByteArrayForSection(section, mask, lightData);
        double indexAndShift = getIndexForPoint(point);

        int index = (int) indexAndShift;
        int shift = indexAndShift % 1 == 0 ? 0 : 4;

        int oldLevel = ( data[index] >> shift ) & 15;

        if (newLevel > 0 && newLevel > oldLevel) {
            lightsToCompute.add(new Light(point, lightSource, newLevel));
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

    public record Light(Point point, LightSource lightSource, int level) {
        @Override
        public boolean equals(Object obj) {
            if (!( obj instanceof Light light )) return false;
            return light.point.equals(point) && light.lightSource.equals(lightSource);
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, lightSource);
        }
    }

    public record ExternalLight(PhasmoChunk owner, Light light) {
        @Override
        public boolean equals(Object obj) {
            if (!( obj instanceof ExternalLight(PhasmoChunk owner1, Light light1) )) return false;
            return owner.equals(owner1) && this.light.lightSource.equals(light1.lightSource) && this.light.point.equals(light1.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, light.lightSource, light.point);
        }

        public Light getWrappedLight() {
            PhasmoChunk[] owners;
            if (light.lightSource instanceof ExternalWrappedLightSource e) {
                owners = new PhasmoChunk[e.getOwners().length + 1];
                owners[0] = owner;
                System.arraycopy(e.getOwners(), 0, owners, 1, e.getOwners().length);
            } else {
                owners = new PhasmoChunk[]{ owner };
            }

            return new Light(
                    light.point,
                    new ExternalWrappedLightSource(light.lightSource, owners),
                    light.level
            );
        }
    }
}
