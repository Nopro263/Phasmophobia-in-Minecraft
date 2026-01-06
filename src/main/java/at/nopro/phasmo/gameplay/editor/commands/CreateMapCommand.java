package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.core.world.WorldLoader;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.registry.RegistryKey;

import java.io.IOException;

public class CreateMapCommand extends Command {
    public CreateMapCommand() {
        super("createMap");

        String[] dimensionNames = MinecraftServer.getDimensionTypeRegistry().keys().stream().map(RegistryKey::name).toArray(String[]::new);

        var name = ArgumentType.Word("name");
        var type = ArgumentType.Word("type").from(dimensionNames).map(s -> {
            Key key = Key.key(s);
            return MinecraftServer.getDimensionTypeRegistry().get(key);
        });

        addSyntax(( (sender, context) -> {
            try {
                WorldLoader.createWorld(context.get(name), context.get(type));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } ), name, type);
    }
}
