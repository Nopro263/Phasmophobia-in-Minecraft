package at.nopro.phasmo;

import at.nopro.phasmo.core.CoreListeners;
import at.nopro.phasmo.core.DimensionTypes;
import at.nopro.phasmo.core.auth.AuthHandler;
import at.nopro.phasmo.core.config.Configuration;
import at.nopro.phasmo.gameplay.Gameplay;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;

import java.io.IOException;

import static at.nopro.phasmo.core.config.Configuration.config;

public class Main {
    static void main() throws IOException {
        Configuration.parseOrCreate();

        Auth auth = AuthHandler.getAuth();
        var server = MinecraftServer.init(auth);

        AuthHandler.postInit();
        DimensionTypes.init();
        CoreListeners.init();
        Gameplay.init();

        server.start(config.mcServer.host, config.mcServer.port);
    }
}
