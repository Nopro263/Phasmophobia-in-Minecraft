package at.nopro.phasmo.lighting;

import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PhasmoChunk extends DynamicChunk {
    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList();
        List<byte[]> blockLights = new ArrayList();
        int index = 0;
        Heightmap heightmap = motionBlockingHeightmap();

        for(Section section : this.sections) {
            ++index;
            //byte[] skyLight = section.skyLight().array();
            //byte[] blockLight = section.blockLight().array();
            /*if (skyLight.length != 0) {
                //skyLights.add(skyLight);
                //skyMask.set(index);
                System.out.println("gfdsljkhgfdsgfd " + index);
            } else {
                emptySkyMask.set(index);
            }*/

            emptySkyMask.set(index);

            /*if (blockLight.length != 0) {
                blockLights.add(blockLight);
                blockMask.set(index);
            } else {*/
                emptyBlockMask.set(index);
            //}
        }

        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }
}
