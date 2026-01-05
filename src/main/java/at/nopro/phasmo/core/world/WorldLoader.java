package at.nopro.phasmo.core.world;

import at.nopro.phasmo.gameplay.lobby.LobbyInstance;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorldLoader {
    private static final String PREFIX = "worlds";

    public static LobbyInstance loadLobby() throws IOException {
        return loadReadonlyWorld("lobby", LobbyInstance.INSTANCE);
    }

    private static <T extends BaseInstance> T loadReadonlyWorld(String name, T instance) throws IOException {
        var path = Path.of(PREFIX, name + ".polar");

        PolarWorld world = PolarReader.read(Files.readAllBytes(path));

        PolarLoader loader = new PolarLoader(world);
        loader.loadInstance(instance);
        instance.setReadonly(true);
        return instance;
    }
}
