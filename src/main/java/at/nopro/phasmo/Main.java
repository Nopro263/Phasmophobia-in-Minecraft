package at.nopro.phasmo;

import at.nopro.phasmo.content.equipment.*;
import at.nopro.phasmo.content.map.Maps;
import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.entity.ai.InvalidPositionException;
import at.nopro.phasmo.game.CameraManager;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemTracker;
import at.nopro.phasmo.lightingv3.IngamePhasmoChunk;
import at.nopro.phasmo.lightingv3.PhasmoInstance;
import dev.lu15.voicechat.VoiceChat;
import net.kyori.adventure.text.Component;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.listener.preplay.HandshakeListener;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static at.nopro.phasmo.Configuration.config;

public class Main {
    public static void main(String[] args) throws IOException {
        Configuration.parseOrCreate();

        VirtualClient virtualClient = new VirtualClient(new File(config.camera.headlessmcPath));
        if (config.camera.enabled) {
            virtualClient.start();
        }

        CameraManager.setCamPlayerName(config.camera.playerName);
        CameraManager.setCamPlayerUUID(UUID.fromString(config.camera.playerUuid));

        Auth auth;
        if ("offline".equals(config.mcServer.auth)) {
            auth = new Auth.Offline();
        } else if ("mojang".equals(config.mcServer.auth)) {
            auth = new Auth.Online();
        } else if ("velocity".equals(config.mcServer.auth)) {
            auth = new Auth.Velocity(config.mcServer.secret);
            //TODO find a way to whitelist the cam player
        } else if ("bungee".equals(config.mcServer.auth)) {
            auth = new Auth.Bungee(new HashSet<>(List.of(config.mcServer.secret.split(" "))));
            //TODO find a way to whitelist the cam player
        } else {
            System.err.println("invalid auth value. check your config!");
            System.exit(1);
            return;
        }

        MinecraftServer minecraftServer = MinecraftServer.init(auth);
        MinecraftServer.getPacketListenerManager().setListener(ConnectionState.HANDSHAKE, ClientHandshakePacket.class, Main::handshakeListener);

        if ("mojang".equals(config.mcServer.auth)) {
            new MojangAuthWithExceptions();
        }

        ResourcePackProvider.init();
        Listeners.init();
        ItemTracker.init();
        PhasmoInstance.registerDimensionType();

        EquipmentManager.register(new EMF_Reader());
        EquipmentManager.register(new Ghost_Book());
        EquipmentManager.register(new Handheld_Camera());
        EquipmentManager.register(new Thermometer());
        EquipmentManager.register(new Flashlight());
        EquipmentManager.register(new DOTS_Projector());

        if (config.devMode) {
            MinecraftServer.getCommandManager().register(new Test());
            MinecraftServer.getCommandManager().register(new Test2());
            MinecraftServer.getCommandManager().register(new Test3());
            MinecraftServer.getCommandManager().register(new Test4());
            MinecraftServer.getCommandManager().register(new Test5());
            MinecraftServer.getCommandManager().register(new Test6());
        }

        GameManager.createGame("default", Maps.TANGLEWOOD_DRIVE);
        if (config.voicechat.enabled) {
            VoiceChat voiceChat = VoiceChat.builder(config.voicechat.host, config.voicechat.port).enable(); //Re enable after new version releases
        }

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                GameContext context = GameManager.getGame(player.getInstance());
                if (context == null) {
                    continue;
                }
                GoalSelector goalSelector = context.entity.getAIGroups().stream().findFirst().get().getCurrentGoalSelector();
                String n;
                if (goalSelector == null) {
                    n = "--- " + context.entity.getPositionAsString();
                } else {
                    n = goalSelector.getClass().getSimpleName() + " " + context.entity.getPositionAsString();
                }

                player.sendActionBar(Component.text(n));
            }
            return TaskSchedule.nextTick();
        });

        minecraftServer.start(config.mcServer.host, config.mcServer.port);
    }

    private static void handshakeListener(ClientHandshakePacket packet, PlayerConnection playerConnection) {
        ClientHandshakePacket packet1 = new ClientHandshakePacket(
                packet.protocolVersion(),
                packet.serverAddress(),
                packet.serverPort(),
                packet.intent() == ClientHandshakePacket.Intent.TRANSFER ? ClientHandshakePacket.Intent.LOGIN : packet.intent()
        );
        HandshakeListener.listener(packet1, playerConnection);
    }

    private static class Test6 extends Command {

        public Test6() {
            super("toggleLight");

            addSyntax((sender, ctx) -> {
                if (sender instanceof Player player) {
                    IngamePhasmoChunk chunk = (IngamePhasmoChunk) player.getChunk();

                    chunk.toggle();
                    chunk.invalidate();
                    chunk.resendLight();

                    player.sendMessage("toggled");
                }
            });
        }
    }

    private static class Test5 extends Command {

        public Test5() {
            super("bake");

            addSyntax((sender, ctx) -> {
                if (sender instanceof Player player) {
                    IngamePhasmoChunk chunk = (IngamePhasmoChunk) player.getChunk();


                    player.sendMessage("baked");
                }
            });
        }
    }

    private static class Test4 extends Command {

        public Test4() {
            super("toggleGhost");

            addSyntax((sender, ctx) -> {
                if (sender instanceof Player player) {
                    GameContext g = GameManager.getGame(player.getInstance());
                    g.entity.setAutoViewable(!g.entity.isAutoViewable());
                }
            });
        }
    }

    private static class Test3 extends Command {

        public Test3() {
            super("give");

            var t = ArgumentType.Word("equipmentType").from(EquipmentManager.getAllRegistered());

            addSyntax((sender, ctx) -> {
                if (sender instanceof Player player) {
                    Equipment e;
                    try {
                        e = EquipmentManager.getInternal(ctx.get(t));
                    } catch (RuntimeException _) {
                        player.sendMessage("Error");
                        return;
                    }
                    player.setItemInMainHand(e.getDefault());
                }
            }, t);
        }
    }

    private static class Test2 extends Command {

        public Test2() {
            super("die");

            addSyntax((sender, ctx) -> {
                if (sender instanceof Player player) {
                    GameContext context = GameManager.getGame(player.getInstance());

                    if (context.getPlayerManager().isAlive(player)) {
                        context.getPlayerManager().kill(player);
                    } else {
                        context.getPlayerManager().revive(player);
                    }
                }
            });
        }
    }

    private static class Test extends Command {

        public Test() {
            super("target");

            addSyntax((sender, ctx) -> {
                if (sender instanceof Player player) {
                    GameContext gameContext = GameManager.getGame(player.getInstance());
                    if (gameContext != null) {
                        CompletableFuture<PhasmoEntity> job;
                        try {
                            job = gameContext.entity.goTo(player.getPosition());
                        } catch (InvalidPositionException e) {
                            throw new RuntimeException(e);
                        }
                        sender.sendMessage("Path " + gameContext.entity.getNavigator().getState());

                        MinecraftServer.getSchedulerManager().submitTask(() -> {
                            if (gameContext.entity.getNavigator().getNodes() == null) {
                                return TaskSchedule.stop();
                            }
                            for (PNode pNode : gameContext.entity.getNavigator().getNodes()) {
                                if (pNode.parent() == null) {
                                    continue;
                                }
                                Point current = new Pos(pNode.x(), pNode.y(), pNode.z());
                                Point parent = new Pos(pNode.parent().x(), pNode.parent().y(), pNode.parent().z());

                                Point delta = current.sub(parent).mul(0.1);
                                for (int i = 0; i < 5; i++) {
                                    player.sendPacket(new ParticlePacket(Particle.FALLING_LAVA, true, true, current, Pos.ZERO, 1, 2));
                                    current = current.add(delta);
                                }
                            }
                            return TaskSchedule.tick(2);
                        });

                        job.thenRun(() -> sender.sendMessage("Completed"));
                    }
                }
            });
        }
    }
}
