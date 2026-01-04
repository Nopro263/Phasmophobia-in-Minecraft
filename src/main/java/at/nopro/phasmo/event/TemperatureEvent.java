package at.nopro.phasmo.event;

import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.RoomManager;

public class TemperatureEvent implements PhasmoEvent {
    private final GameContext gameContext;
    private RoomManager.Room room;

    public TemperatureEvent(GameContext gameContext, RoomManager.Room room) {
        this.gameContext = gameContext;
        this.room = room;
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    public double getTemperature() {
        return room.getTemperature();
    }

    public void setTemperature(double temperature) {
        room.setTemperature(temperature);
    }

    public RoomManager.Room getRoom() {
        return room;
    }

    public void setRoom(RoomManager.Room room) {
        this.room = room;
    }
}
