package at.nopro.phasmo.gameplay.lobby;

import at.nopro.phasmo.core.world.BaseInstance;
import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldLoader;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerSpawnEvent;

import java.io.IOException;

public class LobbyInstance extends BaseInstance {
    public static LobbyInstance INSTANCE;

    private LobbyInstance() {
        super(DimensionTypes.LOBBY);
        setReadonly(true);

        eventNode().addListener(PlayerSpawnEvent.class, this::onPlayerSpawn);
    }

    private void onPlayerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        playerSpawnEvent.getPlayer().teleport(new Pos(0, 17, 0));
    }


    public static void init() throws IOException {
        INSTANCE = new LobbyInstance();
        WorldLoader.loadWorld("lobby", INSTANCE);
    }
}
