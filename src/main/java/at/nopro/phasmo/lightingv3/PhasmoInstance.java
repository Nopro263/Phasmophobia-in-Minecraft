package at.nopro.phasmo.lightingv3;

import at.nopro.phasmo.game.GameContext;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;

import java.util.UUID;

public class PhasmoInstance extends InstanceContainer {
    private final GameContext gameContext;

    public static final DimensionType DIMENSION_TYPE = DimensionType.builder()
            .logicalHeight(0)
            .build();

    public static RegistryKey<DimensionType> DIMENSION_TYPE_REGISTRY;

    public PhasmoInstance(GameContext gameContext) {
        super(UUID.randomUUID(), DIMENSION_TYPE_REGISTRY);
        MinecraftServer.getInstanceManager().registerInstance(this);

        this.gameContext = gameContext;
    }

    public static void registerDimensionType() {
        DIMENSION_TYPE_REGISTRY = MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("phasmo", "ingame"),
                DIMENSION_TYPE
        );
    }

    public GameContext getGameContext() {
        return gameContext;
    }
}
