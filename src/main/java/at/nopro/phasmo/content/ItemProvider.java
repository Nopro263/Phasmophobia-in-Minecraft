package at.nopro.phasmo.content;

import at.nopro.phasmo.content.equipment.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class ItemProvider {
    public static ItemStack getCrucifix(boolean isBurned) {
        return ItemStack.builder(Material.WOODEN_PICKAXE)
                .itemModel(isBurned ? "minecraft:netherite_pickaxe" : "minecraft:iron_pickaxe")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Crucifix.class))
                .customName(Component.text("Crucifix"))
                .build();
    }

    public static ItemStack getSpiritBox(boolean hasResponse) {
        return ItemStack.builder(Material.HEAVY_CORE)
                .itemModel("minecraft:heavy_core")
                .set(DataComponents.DAMAGE, hasResponse ? 1 : 100)
                .set(DataComponents.MAX_DAMAGE, 100)
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(SpiritBox.class))
                .customName(Component.text("SpiritBox"))
                .build();
    }

    public static ItemStack getEMFReader(int level) {
        if (level < 0 || level > 5) throw new RuntimeException("unknown EMF level");
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:emf_reader")
                .set(DataComponents.DAMAGE, 5 - level)
                .set(DataComponents.MAX_DAMAGE, 5)
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(EMF_Reader.class))
                .customName(Component.text("EMF Reader [" + level + "]"))
                .build();
    }

    public static ItemStack getTestThermometer(int level) {
        if (level < 0 || level > 5) throw new RuntimeException("unknown EMF level");
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:emf_reader")
                .set(DataComponents.DAMAGE, 5 - level)
                .set(DataComponents.MAX_DAMAGE, 5)
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Thermometer.class))
                .customName(Component.text("Thermometer [" + level + "]"))
                .build();
    }

    public static ItemStack getFlashlight() {
        return ItemStack.builder(Material.LANTERN)
                .itemModel("minecraft:lantern")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Flashlight.class))
                .customName(Component.text("Flashlight"))
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
                .customName(Component.text("open ghost book"))
                .build();
    }

    public static ItemStack getWrittenBook() {
        return ItemStack.builder(Material.STICK)
                .itemModel("phasmo:book_written")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Ghost_Book.class))
                .customName(Component.text("written ghost book"))
                .build();
    }

    public static ItemStack getHandheldCamera() {
        return ItemStack.builder(Material.COW_SPAWN_EGG)
                .itemModel("phasmo:cam")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(Handheld_Camera.class))
                .customName(Component.text("Video Camera"))
                .build();
    }

    public static ItemStack getDOTSProjector() {
        return ItemStack.builder(Material.OXIDIZED_COPPER_LANTERN)
                .itemModel("minecraft:oxidized_copper_lantern")
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(EquipmentManager.EQUIPMENT_TAG, EquipmentManager.get(DOTS_Projector.class))
                .customName(Component.text("DOTS Projector"))
                .build();
    }
}
