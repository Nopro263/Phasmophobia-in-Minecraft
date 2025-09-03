package at.nopro.minestomTest.phasmo.item;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;

public interface Item {
    boolean canPlaceAt(Instance instance, Point point);
    void placeAt(Instance instance, Point point);
    ItemManager.ItemType type();
}
