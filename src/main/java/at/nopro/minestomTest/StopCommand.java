package at.nopro.minestomTest;

import at.nopro.minestomTest.ext.TeleportToLobby;
import at.nopro.minestomTest.ext.TeleportToPersonal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StopCommand extends Command {
    public StopCommand() {
        super("special", "s");

        setDefaultExecutor((sender,ctx) -> {
            sender.sendMessage("nope");
        });

        var t = ArgumentType.Enum("a", T.class);

        addSyntax((sender, ctx) -> {
            if(sender instanceof Player player) {
                player.setItemInMainHand(getItem(ctx.get(t)));
                BlockHandler handler = getHandler(ctx.get(t));
                player.eventNode().addListener(PlayerUseItemOnBlockEvent.class, (event) -> {

                    Block block = event.getInstance().getBlock(event.getPosition()).withHandler(handler);
                    event.getInstance().setBlock(event.getPosition(), block);

                    player.sendPacket(new BlockChangePacket(event.getPosition(), Block.COMMAND_BLOCK));
                    AtomicBoolean a = new AtomicBoolean(false);
                    MinecraftServer.getSchedulerManager().submitTask(() -> {
                        if(a.get()) {
                            player.sendPacket(new BlockChangePacket(event.getPosition(), event.getInstance().getBlock(event.getPosition())));
                            return TaskSchedule.stop();
                        }
                        a.set(true);
                        return TaskSchedule.seconds(2);
                    }, ExecutionType.TICK_END);
                });
            }
        },t);
    }

    private ItemStack getItem(T t) {
        return ItemStack.builder(Material.STICK).set(DataComponents.ITEM_NAME, Component.text(t + "")).build();
    }

    private BlockHandler getHandler(T t) {
        switch (t) {
            case PERSONAL -> {
                return new TeleportToPersonal();
            }
            case LOBBY -> {
                return new TeleportToLobby();
            }
        }
        return null;
    }

    private enum T {
        LOBBY,
        NONE,
        PERSONAL;
    }
}
