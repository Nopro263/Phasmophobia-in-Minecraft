package at.nopro.minestomTest.ext;

import at.nopro.minestomTest.Main;
import at.nopro.minestomTest.skyblock.personal_island.PersonalManager;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class TeleportToLobby implements BlockHandler {
    @Override
    public void onTouch(@NotNull Touch touch) {
        if (touch.getTouching() instanceof Player player) {
            Main.LOBBY.teleport(player);
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("test:teleport_to_lobby");
    }
}
