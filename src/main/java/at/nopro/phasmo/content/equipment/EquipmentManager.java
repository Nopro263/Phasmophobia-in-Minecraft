package at.nopro.phasmo.content.equipment;

import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EquipmentManager {
    private static final Map<String, Equipment> equipmentMap = new HashMap<>();
    private static final Map<Equipment, String> inverseEquipmentMap = new HashMap<>();

    public static final Tag<Equipment> EQUIPMENT_TAG = Tag.String("phasmo:equipment").map(equipmentMap::get, inverseEquipmentMap::get);

    public static void register(Equipment equipment) {
        String name = equipment.getClass().getSimpleName();
        equipmentMap.put(name, equipment);
        inverseEquipmentMap.put(equipment, name);
    }

    public static Equipment get(Class<? extends Equipment> clazz) {
        return getInternal(clazz.getSimpleName());
    }

    public static Equipment getInternal(String name) {
        if (!equipmentMap.containsKey(name)) {
            throw new RuntimeException("Unknown Equipment " + name);
        }
        return equipmentMap.get(name);
    }

    public static @Nullable Equipment getEquipment(ItemStack itemStack) {
        return itemStack.getTag(EQUIPMENT_TAG);
    }
}
