package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldLoader;
import at.nopro.phasmo.gameplay.editor.EditorInstance;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoadMapCommand extends Command {
    public LoadMapCommand() {
        super("loadMap");


        var name = ArgumentType.Word("name");

        addSyntax(( (sender, context) -> {
            if (!( sender instanceof Player player )) {
                sender.sendMessage("only for players");
                return;
            }

            try {

                DimensionType dimensionType;

                if (context.get(name).equals("lobby")) {
                    dimensionType = DimensionTypes.LOBBY;
                } else {
                    var path = Path.of(WorldLoader.PREFIX, name + ".polar");
                    PolarWorld world = PolarReader.read(Files.readAllBytes(path));
                    dimensionType = DimensionTypes.getDimensionTypeFor(world.minSection() * 16, ( world.maxSection() + 1 ) * 16);

                }

                EditorInstance editorInstance = new EditorInstance(dimensionType);
                WorldLoader.loadWorld(context.get(name), editorInstance);
                player.setInstance(editorInstance);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } ), name);
    }
}
