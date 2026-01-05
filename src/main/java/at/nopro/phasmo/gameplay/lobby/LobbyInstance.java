package at.nopro.phasmo.gameplay.lobby;

import at.nopro.phasmo.core.DimensionTypes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;

import java.util.UUID;

public class LobbyInstance extends InstanceContainer {
    public static LobbyInstance INSTANCE;

    private LobbyInstance() {
        super(UUID.randomUUID(), DimensionTypes.getKeyFor(DimensionTypes.LOBBY));
        MinecraftServer.getInstanceManager().registerInstance(this);
    }

    public static void init() {
        INSTANCE = new LobbyInstance();
    }
}
