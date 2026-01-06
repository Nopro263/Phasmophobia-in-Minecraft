package at.nopro.phasmo.gameplay.editor.commands;

import at.nopro.phasmo.gameplay.editor.EditorInstance;
import at.nopro.phasmo.gameplay.lobby.LobbyInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class ExitCommand extends Command {
    public ExitCommand() {
        super("exit");

        var confirm = ArgumentType.Literal("confirm");

        addSyntax(( (sender, context) -> {
            if (!( sender instanceof Player player )) {
                sender.sendMessage("only for players");
                return;
            }

            if (player.getInstance() instanceof EditorInstance editorInstance) {
                long diff = System.currentTimeMillis() - editorInstance.getLastSaveTime();

                double seconds = diff / 1e3;
                if (seconds > 10) {
                    player.sendMessage(Component.text("Last saved " + seconds + "s ago").color(TextColor.color(255, 21, 21)));
                    player.sendMessage(Component.text("do you wish to continue? ").color(TextColor.color(155, 155, 155)));
                    player.sendMessage(
                            Component.join(JoinConfiguration.builder().separator(Component.text(" ")).build(),
                                    Component.text("[Yes, discard]").color(TextColor.color(255, 21, 21)).clickEvent(ClickEvent.runCommand("exit confirm")),
                                    Component.text(" [No, keep working]").color(TextColor.color(81, 255, 60)).clickEvent(null)
                            )
                    );

                    return;
                }
                player.sendMessage("Exiting to lobby");
                player.setInstance(LobbyInstance.INSTANCE).join();
                editorInstance.closeIfEmpty();
            } else {
                player.sendMessage("you must be in editor-mode to use this");
            }
        } ));

        addSyntax((sender, context) -> {
            if (!( sender instanceof Player player )) {
                sender.sendMessage("only for players");
                return;
            }

            if (player.getInstance() instanceof EditorInstance editorInstance) {
                player.sendMessage("Exiting to lobby");
                player.setInstance(LobbyInstance.INSTANCE).join();
                editorInstance.closeIfEmpty();
            } else {
                player.sendMessage("you must be in editor-mode to use this");
            }
        }, confirm);
    }
}
