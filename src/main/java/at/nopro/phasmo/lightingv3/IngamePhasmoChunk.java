package at.nopro.phasmo.lightingv3;

import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.data.LightData;

public class IngamePhasmoChunk extends DynamicChunk {
    public IngamePhasmoChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
        return super.createLightData(requiredFullChunk);
    }
}
