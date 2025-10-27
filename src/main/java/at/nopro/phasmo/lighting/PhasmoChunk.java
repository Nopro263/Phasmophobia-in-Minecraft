package at.nopro.phasmo.lighting;

import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PhasmoChunk extends DynamicChunk {
    public PhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) { // TODO allow some light sources (van lights, ceiling lights, etc)
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();
        emptyBlockMask.set(0, sections.size());
        emptySkyMask.set(0, sections.size());


        return new LightData(skyMask, blockMask, emptySkyMask, emptyBlockMask, skyLights, blockLights);
    }
}
