package at.nopro.phasmo.light;

import net.minestom.server.instance.block.Block;

@FunctionalInterface
public interface GlobalBlockLookup {
    Block getBlock(int x, int y, int z);
}
