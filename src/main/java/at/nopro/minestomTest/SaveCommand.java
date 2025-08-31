package at.nopro.minestomTest;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SaveCommand extends Command {
    public SaveCommand() {
        super("save");

        setDefaultExecutor((sender, ctx) -> {
            for(var instance : MinecraftServer.getInstanceManager().getInstances()) {
                CompletableFuture<Void> instanceSave = instance.saveInstance().thenCompose(v -> instance.saveChunksToStorage());
                try {
                    instanceSave.get();
                } catch (InterruptedException | ExecutionException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
            sender.sendMessage("Saving done!");
        });
    }
}
