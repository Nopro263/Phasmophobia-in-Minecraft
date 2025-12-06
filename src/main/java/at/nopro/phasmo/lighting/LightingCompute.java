package at.nopro.phasmo.lighting;

import net.minestom.server.instance.Section;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public abstract class LightingCompute {
    private static final int OUTSIDE_LIGHT = 7;
    private static final int INSIDE_LIGHT = 0;

    public static @NotNull LightData generateLightForChunk(PhasmoChunk chunk) {
        return generateLightForChunk(chunk, chunk.getInstance().getCachedDimensionType());
    }

    public static @NotNull LightData generateLightForChunk(PhasmoChunk chunk, DimensionType dimensionType) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        List<Section> sections = chunk.getSections();

        for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++) {
            generateLightForSection(chunk, dimensionType, skyMask, blockMask, skyLights, blockLights, sectionIndex);
        }

        emptySkyMask.set(0, sections.size());
        emptySkyMask.andNot(skyMask);

        emptyBlockMask.set(0, sections.size());
        emptyBlockMask.andNot(blockMask);
        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }

    private static void generateLightForSection(
            PhasmoChunk chunk,
            DimensionType dimensionType,
            BitSet skyMask,
            BitSet blockMask,
            List<byte[]> skyLights,
            List<byte[]> blockLights,
            int sectionIndex) {

        int byteIndex = 0;
        int byteShift = 0; // 0 or 4

        byte[] skyLight = null;
        byte sl = 0;
        byte[] blockLight = null;
        byte bl = 0;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int blockLightValue = 0;
                    int skyLightValue = 0;


                    // START outside lighting
                    int roof = chunk.motionBlockingHeightmap().getHeight(x, z);

                    if (( sectionIndex - 1 ) * 16 + y + dimensionType.minY() >= roof) {
                        skyLightValue = OUTSIDE_LIGHT;
                    } else {
                        skyLightValue = INSIDE_LIGHT;
                    }
                    // END outside lighting

                    // START
                    int gX = chunk.getChunkX() * 16 + x;
                    int gY = ( sectionIndex - 1 ) * 16 + y + dimensionType.minY();
                    int gZ = chunk.getChunkZ() * 16 + z;

                    for (LightSource lightSource : chunk.getLightSources()) {
                        if (lightSource instanceof FloodedLightSource floodedLightSource) {
                            if (floodedLightSource.inRange(gX, gY, gZ)) {
                                blockLightValue = floodedLightSource.getLevel();
                                break;
                            }
                        } else if (lightSource instanceof RadialLightSource radialLightSource) {
                            int level = radialLightSource.getLevelAtPosition(gX, gY, gZ);
                            if (level > 0) {
                                blockLightValue = level;
                            }
                        }
                    }
                    // END

                    if (blockLightValue != 0) {
                        bl |= (byte) ( blockLightValue << byteShift );
                    }
                    if (skyLightValue != 0) {
                        sl |= (byte) ( skyLightValue << byteShift );
                    }

                    byteShift = byteShift == 0 ? 4 : 0;
                    if (byteShift == 0) {
                        if (sl != 0) {
                            if (skyLight == null) {
                                skyLight = new byte[2048];
                                skyLights.add(skyLight);
                                skyMask.set(sectionIndex);
                            }
                            skyLight[byteIndex] = sl;
                        }
                        if (bl != 0) {
                            if (blockLight == null) {
                                blockLight = new byte[2048];
                                blockLights.add(blockLight);
                                blockMask.set(sectionIndex);
                            }
                            blockLight[byteIndex] = bl;
                        }
                        byteIndex++;
                        bl = 0;
                        sl = 0;
                    }
                }
            }
        }
    }
}
