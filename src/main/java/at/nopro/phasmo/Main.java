package at.nopro.phasmo;

import at.nopro.phasmo.core.auth.AuthHandler;
import at.nopro.phasmo.core.config.Configuration;
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

        server.start(config.mcServer.host, config.mcServer.port);
    }
}
