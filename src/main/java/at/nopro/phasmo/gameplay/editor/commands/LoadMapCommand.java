package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldLoader;
import at.nopro.phasmo.core.world.WorldMeta;
import at.nopro.phasmo.gameplay.editor.EditorInstance;
import at.nopro.phasmo.gameplay.ingame.GameInstance;
import at.nopro.phasmo.gameplay.lobby.LobbyInstance;
import at.nopro.phasmo.utils.Utils;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class LoadMapCommand extends Command {
    public LoadMapCommand() {
        super("loadMap");


        var nameArg = ArgumentType.Word("name");
        nameArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            String name = ctx.get(nameArg);
            List<String> existingNames = WorldLoader.getWorldNames();
            suggestion.getEntries().clear();

            for (String existingName : existingNames) {
                if ("\0".equals(name) || existingName.startsWith(name)) {
                    suggestion.addEntry(new SuggestionEntry(existingName));
                }
            }
        });

        addSyntax(( (sender, context) -> {
            if (!( sender instanceof Player player )) {
                sender.sendMessage("only for players");
                return;
            }

            String name = context.get(nameArg);

            try {
                EditorInstance editorInstance = (EditorInstance) MinecraftServer.getInstanceManager().getInstance(Utils.uuidFromObject(name));

                if (editorInstance == null) {
                    DimensionType dimensionType;
                    WorldMeta worldMeta;

                    if (name.equals("lobby")) {
                        dimensionType = DimensionTypes.LOBBY;
                        worldMeta = new LobbyInstance.Meta();
                    } else {
                        var path = Path.of(WorldLoader.PREFIX, name + ".polar");
                        PolarWorld world = PolarReader.read(Files.readAllBytes(path));
                        dimensionType = DimensionTypes.getDimensionTypeFor(world.minSection() * 16, ( world.maxSection() + 1 ) * 16);
                        worldMeta = new GameInstance.Meta();
                    }

                    editorInstance = new EditorInstance(dimensionType, worldMeta, name);
                    WorldLoader.loadWorld(name, editorInstance);
                }

                Instance oldInstance = player.getInstance();
                player.setInstance(editorInstance).join();
                if (oldInstance instanceof EditorInstance oldEditor) {
                    oldEditor.closeIfEmpty();
                }

            } catch (IOException e) {
                if (e instanceof NoSuchFileException f) {
                    sender.sendMessage(Component.text("Map not found").color(TextColor.color(255, 21, 21)));
                    return;
                }
                throw new RuntimeException(e);
            }
        } ), nameArg);
    }
}
