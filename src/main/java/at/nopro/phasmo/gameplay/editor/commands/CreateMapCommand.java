package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.core.world.WorldLoader;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;

import java.io.IOException;

public class CreateMapCommand extends Command {
    public CreateMapCommand() {
        super("createMap");

        String[] dimensionNames = MinecraftServer.getDimensionTypeRegistry().keys().stream().map(RegistryKey::name).toArray(String[]::new);

        var name = ArgumentType.Word("name");
        var type = ArgumentType.Word("type").from(dimensionNames);

        addSyntax(( (sender, context) -> {
            if (!( sender instanceof Player player )) return;

            Key key = Key.key(context.get(type));
            DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(key);
            try {
                WorldLoader.createWorld(context.get(name), dimensionType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            MinecraftServer.getCommandManager().execute(player, "loadMap " + context.get(name));
        } ), name, type);
    }
}
