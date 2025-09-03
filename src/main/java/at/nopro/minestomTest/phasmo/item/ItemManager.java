package at.nopro.minestomTest.phasmo.item;

import at.nopro.minestomTest.phasmo.equipment.Equipment;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;

public class ItemManager {
    public static final Tag<ItemType> itemTypeTag = Tag.String("itemType").map(ItemType::valueOf, ItemType::toString);

    public static boolean canPlace(ItemType itemType, Instance instance, Point point) {
        return itemType.item.canPlaceAt(instance, point);
    }

    public static void place(ItemType itemType, Instance instance, Point point) {
        itemType.item.placeAt(instance, point);
    }

    public enum ItemType {
        BOOK_OPEN(new Book(true)),
        BOOK_CLOSED(new Book(false));

        public final Item item;
        ItemType(Item item) {
            this.item = item;
        }
    }
}
