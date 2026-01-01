package at.nopro.phasmo.light;

import java.util.Arrays;

public class Light {
    private final byte[] lightData;
    private final byte[] propagatingLightData;

    public Light() {
        lightData = Arrays.copyOf(LightCompute.EMPTY_CONTENT, 2048);
        propagatingLightData = Arrays.copyOf(LightCompute.EMPTY_CONTENT, 2048);
    }

    public byte[] getLightData() {
        return lightData;
    }

    public byte[] getPropagatingLightData() {
        return propagatingLightData;
    }

    public void bake() {
        LightCompute.bakeInto(lightData, propagatingLightData);
        Arrays.fill(propagatingLightData, (byte) 0);
    }

    public void clear() {
        Arrays.fill(propagatingLightData, (byte) 0);
        Arrays.fill(lightData, (byte) 0);
    }
}
