package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldLoader;
import at.nopro.phasmo.core.world.WorldMeta;
import at.nopro.phasmo.gameplay.editor.EditorInstance;
import at.nopro.phasmo.gameplay.lobby.LobbyInstance;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoadMapCommand extends Command {
    public LoadMapCommand() {
        super("loadMap");


        var nameArg = ArgumentType.Word("name");

        addSyntax(( (sender, context) -> {
            if (!( sender instanceof Player player )) {
                sender.sendMessage("only for players");
                return;
            }

            String name = context.get(nameArg);

            try {

                DimensionType dimensionType;
                WorldMeta worldMeta;

                if (name.equals("lobby")) {
                    dimensionType = DimensionTypes.LOBBY;
                    worldMeta = new LobbyInstance.Meta();
                } else {
                    var path = Path.of(WorldLoader.PREFIX, name + ".polar");
                    PolarWorld world = PolarReader.read(Files.readAllBytes(path));
                    dimensionType = DimensionTypes.getDimensionTypeFor(world.minSection() * 16, ( world.maxSection() + 1 ) * 16);
                    worldMeta = null; //TODO replace once there is a GameplayInstance
                }

                EditorInstance editorInstance = new EditorInstance(dimensionType, worldMeta);
                WorldLoader.loadWorld(name, editorInstance);
                Instance oldInstance = player.getInstance();
                player.setInstance(editorInstance).join();
                if (oldInstance instanceof EditorInstance oldEditor) {
                    oldEditor.closeIfEmpty();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } ), nameArg);
    }
}
