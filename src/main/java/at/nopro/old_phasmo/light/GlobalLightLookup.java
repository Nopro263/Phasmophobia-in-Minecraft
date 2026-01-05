package at.nopro.old_phasmo.light;

@FunctionalInterface
public interface GlobalLightLookup {
    default byte[] getExternalLightForSectionAt(int x, int y, int z) {
        Light section = getLightForSectionAt(x, y, z);
        if (section == null) return null;
        return section.getPropagatingLightData();
    }

    Light getLightForSectionAt(int x, int y, int z);

    default int getLightAtPoint(int x, int y, int z) {
        Light section = getLightForSectionAt(x, y, z);
        if (section == null) return -1;
        return Math.max(
                LightCompute.getLight(section.getLightData(), x & 15, y & 15, z & 15),
                LightCompute.getLight(section.getPropagatingLightData(), x & 15, y & 15, z & 15)
        );
    }
}
