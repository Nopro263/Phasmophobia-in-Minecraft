package at.nopro.minestomTest;

import at.nopro.minestomTest.ext.TeleportToLobby;
import at.nopro.minestomTest.ext.TeleportToPersonal;
import at.nopro.minestomTest.skyblock.lobby.Lobby;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.ping.Status;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class Main {
    public static Lobby LOBBY;

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getCommandManager().register(new StopCommand(), new SaveCommand(), new TeleportCommand());
        MinecraftServer.getBlockManager().registerHandler("test:teleport_to_personal", TeleportToPersonal::new);
        MinecraftServer.getBlockManager().registerHandler("test:teleport_to_lobby", TeleportToLobby::new);

        LOBBY = new Lobby();

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(LOBBY.getInstance());
            player.setRespawnPoint(LOBBY.getPos());
        });

        globalEventHandler.addListener(ServerListPingEvent.class, serverListPingEvent -> {
            serverListPingEvent.setStatus(Status.builder().description(Component.text("A server")).playerInfo(Status.PlayerInfo.builder().maxPlayers(9999).onlinePlayers(1).sample("aRandomUser").build())
                    .versionInfo(new Status.VersionInfo("Some version", serverListPingEvent.getConnection().getProtocolVersion())).build());
        });

        MinecraftServer.getConnectionManager().setPlayerProvider((connection, gameProfile) -> {
            // This method will be called at players connection to change the player's provider
            return new Player(connection, new GameProfile(UUID.randomUUID(), gameProfile.name(), gameProfile.properties()));
        });


        globalEventHandler.addListener(PlayerSpawnEvent.class, playerSpawnEvent -> {
            playerSpawnEvent.getPlayer().setGameMode(GameMode.CREATIVE);
        });

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            for(Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                player.kick("Server stopped");
            }

            for(var instance : MinecraftServer.getInstanceManager().getInstances()) {
                CompletableFuture<Void> instanceSave = instance.saveInstance().thenCompose(v -> instance.saveChunksToStorage());
                try {
                    instanceSave.get();
                } catch (InterruptedException | ExecutionException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }

            System.out.println("saved all");
        });

        minecraftServer.start("127.0.0.1", 25565);
    }
}
