package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.GhostEvent;
import at.nopro.phasmo.event.PhasmoEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.item.ItemStack;

import java.util.function.Consumer;

public class EMF_Reader implements Equipment {
    @Override
    public void handle(Event event, Player p, Consumer<ItemStack> u) {
        switch (event) {
            case GhostEvent e -> handle(e,p,u);
            case PlayerSwapItemEvent e -> handle(e,p,u);
            default -> {}
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getEMFReader(0);
    }

    private void handle(GhostEvent ghostEvent, Player player, Consumer<ItemStack> updateFunction) {
        if(player.getPosition().distanceSquared(ghostEvent.getOrigin()) <= 25) {
            updateFunction.accept(ItemProvider.getEMFReader(ghostEvent.getEmfLevel()));
        }
    }

    private void handle(PlayerSwapItemEvent swapItemEvent, Player player, Consumer<ItemStack> updateFunction) {
        swapItemEvent.setCancelled(true);
    }
}
