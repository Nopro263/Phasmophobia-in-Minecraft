package at.nopro.minestomTest.phasmo.equipment;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class EquipmentManager {
    public static final EventNode<@NotNull PlayerEvent> playerNode = EventNode.type("equipment-player-listener", EventFilter.PLAYER);
    public static final Tag<EquipmentType> equipmentTypeTag = Tag.String("equipment").map(EquipmentType::valueOf, EquipmentType::toString);

    static {
        MinecraftServer.getGlobalEventHandler().addChild(playerNode);

        for(EquipmentType equipmentType : EquipmentType.values()) {
            addEquipment(equipmentType.equipment);
        }
    }

    private static void addEquipment(Equipment equipment) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("equipment-" + equipment.type() + "-listener", EventFilter.PLAYER,(event, _) -> event.getPlayer().getItemInMainHand().getTag(equipmentTypeTag) == equipment.type());

        equipment.registerEvents(node);

        playerNode.addChild(node);
    }

    public static ItemStack getDefaultItemStack(EquipmentType equipmentType) {
        return equipmentType.equipment.getDefaultItemStack().withTag(equipmentTypeTag, equipmentType);
    }

    public enum EquipmentType {
        Flashlight(new Flashlight()),
        Book(new Book());

        private final Equipment equipment;
        EquipmentType(Equipment equipment) {
            this.equipment = equipment;
        }
    }
}
