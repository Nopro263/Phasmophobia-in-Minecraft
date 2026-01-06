package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.gameplay.editor.EditorInstance;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SaveMapCommand extends Command {
    public SaveMapCommand() {
        super("save");

        addSyntax(( (sender, context) -> {
            if (!( sender instanceof Player player )) {
                sender.sendMessage("only for players");
                return;
            }

            if (player.getInstance() instanceof EditorInstance editorInstance) {
                CompletableFuture<Void> task = editorInstance.save();
                player.sendMessage("saving instance");
                task.join();
                player.sendMessage("saved!");
            } else {
                player.sendMessage("you must be in editor-mode to use this");
            }
        } ));
    }
}
