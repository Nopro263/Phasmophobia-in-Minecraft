package at.nopro.old_phasmo.light;

import net.minestom.server.instance.palette.Palette;

@FunctionalInterface
public interface GlobalPaletteLookup {
    Palette getBlockPaletteAt(int x, int y, int z);
}
