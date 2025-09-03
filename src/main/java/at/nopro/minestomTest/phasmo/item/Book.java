package at.nopro.minestomTest.phasmo.item;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;

public class Book implements Item, Throwable {
    private final boolean open;
    public Book(boolean open) {
        this.open = open;
    }

    @Override
    public boolean canPlaceAt(Instance instance, Point point) {
        return instance.getBlock(point).isAir();
    }

    @Override
    public void placeAt(Instance instance, Point point) {
        Entity entity = new Entity(EntityType.ITEM_DISPLAY);
        ItemDisplayMeta meta = (ItemDisplayMeta) entity.getEntityMeta();
        meta.setHasNoGravity(true);
        meta.setDisplayContext(ItemDisplayMeta.DisplayContext.GROUND);
        meta.setItemStack(ItemStack.builder(Material.BOOK)
                .itemModel(open ? "phasmo:book_open" : "phasmo:book_closed")
                .set(ItemManager.itemTypeTag, open ? ItemManager.ItemType.BOOK_OPEN : ItemManager.ItemType.BOOK_CLOSED).build());
        entity.setBoundingBox(2.5,0.5,1);
        entity.setInstance(instance, point.add(0,0.5,0));
        boolean[] b = new boolean[] {false};
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if(b[0]) {
                ghostThrow(entity);
                return TaskSchedule.stop();
            }
            b[0] = true;
            return TaskSchedule.seconds(3);
        });
    }

    public void ghostThrow(Entity bookEntity) {
        if(!(bookEntity.getEntityMeta() instanceof ItemDisplayMeta meta)) {
            throw new RuntimeException("books must be itemdisplays");
        }
        bookEntity.setVelocity(new Vec(3,0,0));
    }

    @Override
    public ItemManager.ItemType type() {
        return open ? ItemManager.ItemType.BOOK_OPEN : ItemManager.ItemType.BOOK_CLOSED;
    }
}
