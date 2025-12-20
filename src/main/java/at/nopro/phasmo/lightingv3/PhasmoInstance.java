package at.nopro.phasmo.lightingv3;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;

import java.util.UUID;

public class PhasmoInstance extends InstanceContainer {
    private final GameContext gameContext;

    public PhasmoInstance(GameContext gameContext) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(this);

        this.gameContext = gameContext;
    }
}
