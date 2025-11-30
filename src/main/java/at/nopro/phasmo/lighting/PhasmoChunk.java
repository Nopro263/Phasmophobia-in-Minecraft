package at.nopro.phasmo.lighting;

import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PhasmoChunk extends DynamicChunk {
    private final int miny;

    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
        this.miny = instance.getCachedDimensionType().minY();
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();
        emptyBlockMask.set(1, sections.size());
        emptySkyMask.set(0, sections.size());
        /*
         * Sky light should be 0 inside house and ~7 outside
         * BlockLight should be 0 everywhere except light sources (lamps, etc)
         * */

        // TODO cleanup
        for (int i = 0; i < sections.size(); i++) {
            byte[] content = new byte[2048];

            int a = 0;
            byte shift = 0;
            byte c = 0;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {

                        int roof = motionBlockingHeightmap().getHeight(x, z);

                        byte level = 0;

                        /*
                         * i : index of section (y) ! there is one section above and below the world
                         * x,y,z : coordinates inside this chunk
                         * roof : y level of highest block on x,z
                         * miny : smallest possible y value -64
                         * */
                        if (( i - 1 ) * 16 + y + miny >= roof) {
                            level = 7;
                        }


                        c |= (byte) ( level << shift );

                        if (shift == 0) {
                            shift = 4;
                        } else {
                            shift = 0;
                            content[a] = c;
                            c = 0;
                            a++;
                        }
                    }
                }
            }

            skyMask.set(i);
            skyLights.add(content);
        }
        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }
}
