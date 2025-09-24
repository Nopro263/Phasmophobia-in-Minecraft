package at.nopro.phasmo.content;

import at.nopro.phasmo.content.equipment.EMF_Reader;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.equipment.Ghost_Book;
import at.nopro.phasmo.content.equipment.Handheld_Camera;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class ItemProvider {
    public static ItemStack getEMFReader(int level) {
        if (level < 0 || level > 5) throw new RuntimeException("unknown EMF level");
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:emf_reader")
                .set(DataComponents.DAMAGE, 5-level)
                .set(DataComponents.MAX_DAMAGE, 5)
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(EMF_Reader.class))
                .customName(Component.text("EMF Reader [" + level + "]"))
                .build();
    }

    public static ItemStack getClosedBook() {
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:book_closed")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Ghost_Book.class))
                .customName(Component.text("ghost book"))
                .build();
    }

    public static ItemStack getOpenBook() {
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:book_open")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Ghost_Book.class))
                .customName(Component.text("written ghost book"))
                .build();
    }

    public static ItemStack getHandheldCamera() {
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:cam")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Handheld_Camera.class))
                .customName(Component.text("Video Camera"))
                .build();
    }
}
