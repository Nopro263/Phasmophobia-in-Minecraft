package at.nopro.phasmo;

import at.nopro.phasmo.content.equipment.EMF_Reader;
import at.nopro.phasmo.content.equipment.Equipment;
import at.nopro.phasmo.content.equipment.EquipmentManager;
import at.nopro.phasmo.content.equipment.Ghost_Book;
import at.nopro.phasmo.content.map.Maps;
import at.nopro.phasmo.entity.PhasmoEntity;
import at.nopro.phasmo.entity.ai.InvalidPositionException;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import dev.lu15.voicechat.VoiceChat;
import dev.lu15.voicechat.api.SoundSelector;
import dev.lu15.voicechat.event.PlayerMicrophoneEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getCommandManager().register(new Test());
        MinecraftServer.getCommandManager().register(new Test2());
        MinecraftServer.getCommandManager().register(new Test3());

        Listeners.init();

        EquipmentManager.register(new EMF_Reader());
        EquipmentManager.register(new Ghost_Book());

        GameManager.createGame("default", Maps.TANGLEWOOD_DRIVE);
        VoiceChat voiceChat = VoiceChat.builder("0.0.0.0",25565).enable();

        minecraftServer.start("0.0.0.0", 25565);
    }

    private static class Test3 extends Command {

        public Test3() {
            super("give");

            var t = ArgumentType.String("eq");

            addSyntax((sender, ctx) -> {
                if(sender instanceof Player player) {
                    Equipment e = EquipmentManager.getInternal(ctx.get(t));
                    player.setItemInMainHand(e.getDefault());
                }
            }, t);
        }
    }

    private static class Test2 extends Command {

        public Test2() {
            super("showValidArea");

            addSyntax((sender, ctx) -> {
                if(sender instanceof Player player) {
                    GameContext context = GameManager.getGame(player.getInstance());

                    for (int i = context.getMapContext().lowerEnd().blockX(); i <= context.getMapContext().upperEnd().blockX(); i++) {
                        for (int j = context.getMapContext().lowerEnd().blockY(); j <= context.getMapContext().upperEnd().blockY(); j++) {
                            for (int k = context.getMapContext().lowerEnd().blockZ(); k <= context.getMapContext().upperEnd().blockZ(); k++) {
                                if(!context.getPathCache().isInvalid(
                                        (short) i,
                                        (short) j,
                                        (short) k
                                )) {
                                    player.sendPacket(new BlockChangePacket(new Pos(i,j,k), Block.LIME_STAINED_GLASS));
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private static class Test extends Command {

        public Test() {
            super("target");

            addSyntax((sender, ctx) -> {
                if(sender instanceof Player player) {
                    GameContext gameContext = GameManager.getGame(player.getInstance());
                    if(gameContext != null) {
                        CompletableFuture<PhasmoEntity> job;
                        try {
                            job = gameContext.entity.goTo(player.getPosition());
                        } catch (InvalidPositionException e) {
                            throw new RuntimeException(e);
                        }
                        sender.sendMessage("Path " + gameContext.entity.getNavigator().getState());

                        MinecraftServer.getSchedulerManager().submitTask(() -> {
                            if(gameContext.entity.getNavigator().getNodes() == null) {
                                return TaskSchedule.stop();
                            }
                            for(PNode pNode : gameContext.entity.getNavigator().getNodes()) {
                                if(pNode.parent() == null) {
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
