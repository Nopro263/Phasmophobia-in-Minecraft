package at.nopro.minestomTest.skyblock.personal_island;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Personal {
    private InstanceContainer instanceContainer;
    private Path path;

    private static final Path TEMPLATE = Path.of("/home/noah/.local/share/multimc/instances/1.21.8_2/.minecraft/saves/New World/region");

    public Personal(Player player) {
        this.path = Path.of("skyblock", "personal", player.getUsername(), "region");

        if(!Files.exists(this.path)) {
            try {
                Files.createDirectories(path);
                Files.walkFileTree(TEMPLATE, new FileVisitor<>() {
                    @Override
                    public @NotNull FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, path.resolve(TEMPLATE.relativize(file)));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult visitFileFailed(Path file, @NotNull IOException exc) throws IOException {
                        return FileVisitResult.TERMINATE;
                    }

                    @Override
                    public @NotNull FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.instanceContainer.setChunkLoader(new AnvilLoader(this.path.getParent()));
        this.instanceContainer.setGenerator(unit -> {});
        this.instanceContainer.setChunkSupplier(LightingChunk::new);

        this.instanceContainer.eventNode().addListener(InstanceBlockUpdateEvent.class, (event) -> {
            System.out.println(event.getBlockPosition() + " " + event.getBlockPosition());
        });
    }

    public void teleport(Player player) {
        player.setInstance(this.instanceContainer, new Pos(8.5,-45,6.5,90,0));
    }
}
