package at.nopro.phasmo.core.auth;

import net.minestom.server.Auth;

import java.util.HashSet;
import java.util.List;

import static at.nopro.phasmo.core.config.Configuration.config;

public final class AuthHandler {
    private AuthHandler() {
    }

    public static Auth getAuth() {
        Auth auth;
        switch (config.mcServer.auth) {
            case "offline" -> auth = new Auth.Offline();
            case "mojang" -> auth = new Auth.Online();
            case "velocity" ->
                    auth = new Auth.Velocity(config.mcServer.secret); //TODO find a way to whitelist the cam player
            case "bungee" ->
                    auth = new Auth.Bungee(new HashSet<>(List.of(config.mcServer.secret.split(" ")))); //TODO find a way to whitelist the cam player

            case null, default -> {
                System.err.println("invalid auth value. check your config!");
                System.exit(1);
                throw new AssertionError("this must never be reached");
            }
        }

        return auth;
    }

    public static void postInit() {
        if ("mojang".equals(config.mcServer.auth)) {
            MojangAuthWithExceptions.init();
        }
    }
}
