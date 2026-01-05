package at.nopro.phasmo.gameplay.lobby;

import at.nopro.phasmo.core.world.BaseInstance;
import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldLoader;

import java.io.IOException;

public class LobbyInstance extends BaseInstance {
    public static LobbyInstance INSTANCE;

    private LobbyInstance() {
        super(DimensionTypes.LOBBY);
    }

    public static void init() throws IOException {
        INSTANCE = new LobbyInstance();
        WorldLoader.loadLobby();
    }
}
