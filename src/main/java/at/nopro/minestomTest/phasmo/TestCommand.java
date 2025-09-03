package at.nopro.minestomTest.phasmo;

import at.nopro.entityLoader.EntityLoader;
import at.nopro.minestomTest.phasmo.equipment.EquipmentManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;


import java.awt.*;

public class TestCommand extends Command {
    public TestCommand() {
        super("test");

        var equipment = ArgumentType.Enum("eq", EquipmentManager.EquipmentType.class);

        addSyntax((sender, ctx) -> {
            if(sender instanceof Player player) {
                EquipmentManager.EquipmentType e = ctx.get(equipment);
                player.setItemInMainHand(EquipmentManager.getDefaultItemStack(e));
            }
        }, equipment);

        addSyntax((sender, ctx) -> {
            if(sender instanceof Player player) {
                // Power off
                player.addEffect(new Potion(PotionEffect.BLINDNESS, 0, Potion.INFINITE_DURATION, 0));

                // Hunt???
                player.addEffect(new Potion(PotionEffect.DARKNESS, 255, Potion.INFINITE_DURATION, Potion.BLEND_FLAG));
            }
        });
    }
}
