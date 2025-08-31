package at.nopro.minestomTest.ext;

import at.nopro.minestomTest.skyblock.personal_island.PersonalManager;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class TeleportToPersonal implements BlockHandler {
    @Override
    public void onTouch(@NotNull Touch touch) {
        System.out.println(touch);
        if (touch.getTouching() instanceof Player player) {
            PersonalManager.getOrCreate(player).teleport(player);
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("test:teleport_to_personal");
    }
}
