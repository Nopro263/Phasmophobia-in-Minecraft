package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.TemperatureEvent;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemReference;
import at.nopro.phasmo.game.RoomManager;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;

public class Thermometer implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case TemperatureEvent e -> handle(e, en, r);
            case PlayerMoveEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getTestThermometer(0);
    }

    private void handle(TemperatureEvent temperatureEvent, Entity entity, ItemReference r) {
        if (temperatureEvent.gameContext().getRoomManager().getRoom(entity.getPosition()) != temperatureEvent.getRoom()) {
            return;
        }

        if (temperatureEvent.getTemperature() < 0) {
            r.set(ItemProvider.getTestThermometer(1));
        }
        if (temperatureEvent.getTemperature() > 0) {
            r.set(ItemProvider.getTestThermometer(5));
        }
    }

    private void handle(PlayerMoveEvent moveEvent, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(moveEvent.getInstance());
        RoomManager.Room room = gameContext.getRoomManager().getRoom(moveEvent.getNewPosition());
        if (room == null) {
            r.set(ItemProvider.getTestThermometer(0));
            return;
        }
        if (room.getTemperature() < 0) {
            r.set(ItemProvider.getTestThermometer(1));
        }
        if (room.getTemperature() > 0) {
            r.set(ItemProvider.getTestThermometer(5));
        }
    }
}
