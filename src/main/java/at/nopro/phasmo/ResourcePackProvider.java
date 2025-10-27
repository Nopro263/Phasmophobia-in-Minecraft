package at.nopro.phasmo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerLoadedEvent;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
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

import static at.nopro.phasmo.Configuration.config;


public class ResourcePackProvider implements HttpHandler {
    private final String bindIp;
    private final int bindPort;
    private final String externalIp;
    private final int externalPort;
    private final boolean externalHttps;
    private final String path;

    private final ByteArrayOutputStream rawPackData;
    private HttpServer httpServer;
    private ResourcePackRequest resourcePack;
    private ResourcePackInfo packInfo;

    private ResourcePackProvider(String bindIp, int bindPort, String externalIp, int externalPort, boolean externalHttps, String path) {
        this.bindIp = bindIp;
        this.bindPort = bindPort;
        this.externalIp = externalIp;
        this.externalPort = externalPort;
        this.externalHttps = externalHttps;
        this.path = path;
        this.rawPackData = new ByteArrayOutputStream();
    }

    public static ResourcePackProvider init() throws IOException {
        ResourcePackProvider provider = new ResourcePackProvider(
                config.resourcepackServer.bind.host,
                config.resourcepackServer.bind.port,
                config.resourcepackServer.access.host,
                config.resourcepackServer.access.port,
                config.resourcepackServer.access.https,
                "/pack.zip"
        );

        if (config.resourcepackServer.bind.path.endsWith(".zip")) {
            provider.fromFile(Path.of(config.resourcepackServer.bind.path));
        } else {
            Path path = Path.of(config.resourcepackServer.bind.path);
            if (!path.resolve("pack.mcmeta").toFile().exists()) {
                System.err.println("The specified resource pack directory does not contain a pack.mcmeta");
                System.exit(1);
                return null;
            }
            provider.fromDirectory(path);
        }
        provider.startServer();
        provider.initResourcePack();
        return provider;
    }

    private void fromDirectory(Path path) throws IOException {
        try (

                ZipOutputStream zos = new ZipOutputStream(rawPackData)
        ) {
            Files.walkFileTree(path, Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(path.relativize(dir) + "/"));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(path.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void startServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(bindIp, bindPort), 0);
        httpServer.createContext(path, this);
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();
    }

    private void initResourcePack() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoadedEvent.class, (event) -> {
            String host;
            if ("0.0.0.0".equals(externalIp)) {
                host = event.getPlayer().getPlayerConnection().getServerAddress();
            } else {
                host = externalIp;
            }

            URI uri;
            try {
                uri = new URI(
                        externalHttps ? "https" : "http",
                        null,
                        host,
                        externalPort,
                        path,
                        null,
                        null
                );
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            packInfo = ResourcePackInfo.resourcePackInfo()
                    .id(UUID.fromString("f40db609-a06b-4238-b06f-387672243b6e"))
                    .uri(uri)
                    .computeHashAndBuild().join();

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

    private void fromFile(Path path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(path.toFile());
        fileInputStream.transferTo(rawPackData);
        fileInputStream.close();
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
}
