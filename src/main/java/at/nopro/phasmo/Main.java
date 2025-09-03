package at.nopro.phasmo;

import at.nopro.phasmo.content.map.Maps;
import at.nopro.phasmo.game.GameManager;
import net.minestom.server.MinecraftServer;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        Listeners.init();

        GameManager.createGame("default", Maps.TANGLEWOOD_DRIVE);

        minecraftServer.start("0.0.0.0", 25565);
    }
}
