package at.nopro.phasmo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerLoadedEvent;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackProvider implements HttpHandler {
    private final String ip;
    private final int port;
    private final String path;

    private HttpServer httpServer;
    private ByteArrayOutputStream rawPackData;

    private ResourcePackRequest resourcePack;
    private ResourcePackInfo packInfo;

    private ResourcePackProvider(String ip, int port, String path) {
        this.ip = ip;
        this.port = port;
        this.path = path;
        this.rawPackData = new ByteArrayOutputStream();
    }

    public static ResourcePackProvider initFromFile(String ip, int port, Path file) throws IOException, URISyntaxException {
        ResourcePackProvider provider = new ResourcePackProvider(ip, port, "/pack.zip");
        provider.fromFile(file);
        provider.startServer();
        provider.initResourcePack();
        return provider;
    }

    public static ResourcePackProvider initFromDirectory(String ip, int port, Path directory) throws IOException, URISyntaxException {
        ResourcePackProvider provider = new ResourcePackProvider(ip, port, "/pack.zip");
        provider.fromDirectory(directory);
        provider.startServer();
        provider.initResourcePack();
        return provider;
    }

    private void initResourcePack() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoadedEvent.class, (event) -> {
            try {
                packInfo = ResourcePackInfo.resourcePackInfo()
                        .id(UUID.fromString("f40db609-a06b-4238-b06f-387672243b6e"))
                        .uri(new URI("http://" + event.getPlayer().getPlayerConnection().getServerAddress() + ":" + port + path))
                        .computeHashAndBuild().join();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            resourcePack = ResourcePackRequest.resourcePackRequest()
                    .packs(packInfo)
                    .prompt(Component.text("use this resource pack"))
                    .required(true)
                    .replace(false)
                    .build();
            event.getPlayer().sendResourcePacks(resourcePack);
        });

        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> p.sendResourcePacks(resourcePack));
    }

    private void startServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        httpServer.createContext(path, this);
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/zip");
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(rawPackData.size()));
        if ("HEAD".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        exchange.sendResponseHeaders(200, rawPackData.size());
        try (var stream = exchange.getResponseBody()) {
            rawPackData.writeTo(stream);
        }
    }

    private void fromFile(Path path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(path.toFile());
        fileInputStream.transferTo(rawPackData);
        fileInputStream.close();
    }

    private void fromDirectory(Path path) throws IOException {
        try (

                ZipOutputStream zos = new ZipOutputStream(rawPackData)
        ) {
            Files.walkFileTree(path, Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,  new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(path.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(path.relativize(dir).toString() + "/"));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
