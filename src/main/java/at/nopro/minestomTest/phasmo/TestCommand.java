package at.nopro.minestomTest.phasmo;

import at.nopro.entityLoader.EntityLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class TestCommand extends Command {
    public TestCommand() {
        super("test");

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
