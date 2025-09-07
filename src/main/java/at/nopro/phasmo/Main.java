package at.nopro.phasmo;

import at.nopro.phasmo.content.map.Maps;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getCommandManager().register(new Test());

        Listeners.init();

        GameManager.createGame("default", Maps.TANGLEWOOD_DRIVE);

        minecraftServer.start("0.0.0.0", 25565);
    }

    private static class Test extends Command {

        public Test() {
            super("target");

            addSyntax((sender, ctx) -> {
                if(sender instanceof Player player) {
                    GameContext gameContext = GameManager.getGame(player.getInstance());
                    if(gameContext != null) {
                        gameContext.entity.getNavigator().setPathTo(
                                player.getPosition(),
                                1,
                                () -> sender.sendMessage("Completed")
                        );
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
                    }
                }
            });
        }
    }
}
