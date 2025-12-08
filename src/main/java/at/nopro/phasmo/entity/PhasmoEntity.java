package at.nopro.phasmo.entity;

import at.nopro.phasmo.entity.ai.InvalidPositionException;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.RoomManager;
import at.nopro.phasmo.lighting.LightSource;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.pathfinding.followers.GroundNodeFollower;
import net.minestom.server.entity.pathfinding.followers.NodeFollower;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.entity.pathfinding.generators.PreciseGroundNodeGenerator;

import java.util.concurrent.CompletableFuture;

public class PhasmoEntity extends EntityCreature {
    protected final GameContext gameContext;
    private final NodeGenerator nodeGenerator;
    private final NodeFollower nodeFollower;

    public PhasmoEntity(EntityType entityType, GameContext gameContext) {
        super(entityType);

        this.gameContext = gameContext;

        /*this.nodeGenerator = new PhasmoNodeGenerator(gameContext.getPathCache());
        this.nodeFollower = new PhasmoNodeFollower(this);*/
        this.nodeGenerator = new PreciseGroundNodeGenerator();
        this.nodeFollower = new GroundNodeFollower(this);

        this.getNavigator().setNodeGenerator(() -> nodeGenerator);
        this.getNavigator().setNodeFollower(() -> nodeFollower);
    }

    @Override
    public void update(long time) {

        super.update(time);
    }

    public RoomManager.Room getRoom() {
        return gameContext.getRoomManager().getRoom(position);
    }

    public CompletableFuture<PhasmoEntity> goTo(Point point) throws InvalidPositionException { // FixMe if point not reachable, server crashes
        /*if (this.nodeGenerator.pointInvalid(
                this.instance,
                point,
                this.boundingBox
        )) {
            throw new InvalidPositionException("target position is invalid", point);
        }*/
        if (gameContext.getRoomManager().getRoom(point) == null) {
            throw new InvalidPositionException("target position is not in room", point);
        }

        CompletableFuture<PhasmoEntity> result = new CompletableFuture<>();

        BoundingBox bb = this.getBoundingBox();
        double centerToCorner = Math.sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / (double) 2.0F;

        this.getNavigator().setPathTo(point, centerToCorner, () -> result.complete(this));

        return result;
    }

    public GameContext getGameContext() {
        return gameContext;
    }

    public LightSource getLightSource() {
        return null;
    }
}
