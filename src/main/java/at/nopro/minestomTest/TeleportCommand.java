package at.nopro.minestomTest;

import at.nopro.minestomTest.skyblock.personal_island.Personal;
import at.nopro.minestomTest.skyblock.personal_island.PersonalManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("tp");

        setDefaultExecutor(((commandSender, commandContext) -> commandSender.sendMessage("usage: /tp [desert/overworld]")));

        var target = ArgumentType.Enum("target", TeleportTarget.class);
        target.setCallback((sender, exception) -> {
            sender.sendMessage("target must be desert or overworld");
        });

        addSyntax((CommandSender sender, CommandContext commandContext) -> {
            TeleportTarget teleportTarget = commandContext.get("target");
            if(sender instanceof Player player) {
                switch (teleportTarget) {
                    case LOBBY -> {
                        Main.LOBBY.teleport(player);
                    }
                    case ADMIN_LOBBY -> {
                        Main.LOBBY.teleportAdmin(player);
                    }
                    case PERSONAL -> {
                        PersonalManager.getOrCreate(player).teleport(player);
                    }
                }
            }
        }, target);
    }

    private enum TeleportTarget {
        LOBBY,
        PERSONAL,
        ADMIN_LOBBY;
    }
}
