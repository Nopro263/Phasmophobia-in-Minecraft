package at.nopro.phasmo.light;

import net.minestom.server.instance.palette.Palette;

@FunctionalInterface
public interface GlobalPaletteLookup {
    Palette getBlockPalette(int sectionX, int sectionY, int sectionZ);
}
