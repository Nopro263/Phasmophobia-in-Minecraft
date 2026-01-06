package at.nopro.phasmo.gameplay.editor;

import at.nopro.phasmo.gameplay.editor.commands.CreateMapCommand;
import at.nopro.phasmo.gameplay.editor.commands.ExitCommand;
import at.nopro.phasmo.gameplay.editor.commands.LoadMapCommand;
import at.nopro.phasmo.gameplay.editor.commands.SaveMapCommand;
import net.minestom.server.MinecraftServer;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static at.nopro.phasmo.core.config.Configuration.config;

public final class Editor {
    private Editor() {
    }

    public static void init() {
        if (!config.devMode) return;

        MinecraftServer.getCommandManager().register(new CreateMapCommand());
        MinecraftServer.getCommandManager().register(new SaveMapCommand());
        MinecraftServer.getCommandManager().register(new LoadMapCommand());
        MinecraftServer.getCommandManager().register(new ExitCommand());
    }

    public record MetaEntry(Consumer<Object> setter, Supplier<Object> getter, Class<?> type) {
    }
}
