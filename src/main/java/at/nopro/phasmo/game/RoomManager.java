package at.nopro.phasmo.game;

import at.nopro.entityLoader.MetadataMapper;
import at.nopro.phasmo.event.TemperatureEvent;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.MarkerMeta;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.nopro.entityLoader.MetadataMapper.DATA_TAG;

public class RoomManager {
    private final GameContext gameContext;
    private final List<Room> rooms = new ArrayList<>();

    public RoomManager(GameContext gameContext) {
        this.gameContext = gameContext;

        gameContext.getScheduler().run("room-manager", () -> {
            tick();
            return TaskSchedule.seconds(1);
        });
    }

    private void tick() {
        for (Room room : rooms) {
            room.calculateTemperature();
            gameContext.getEventNode().call(new TemperatureEvent(
                    gameContext,
                    room
            ));
        }
    }

    public @Nullable Room getRoom(Point point) {
        for (Room r : rooms) {
            if (r.contains(point)) {
                return r;
            }
        }
        return null;
    }

    @ApiStatus.Internal
    public Entity parseEntity(Entity entity) {
        if (entity.getEntityMeta() instanceof MarkerMeta markerMeta) {
            BinaryTag b = entity.getTag(DATA_TAG);
            if (b instanceof CompoundBinaryTag cbt) {
                String name = cbt.getString("name", "");
                Vec min = MetadataMapper.listToVec(cbt.getList("min"), entity.getPosition());
                Vec max = MetadataMapper.listToVec(cbt.getList("max"), entity.getPosition());

                Room room = null;

                for (Room r : rooms) {
                    if (r.name.equals(name)) {
                        room = r;
                        break;
                    }
                }

                if (room == null) {
                    room = new Room(name);
                    rooms.add(room);
                }

                room.boundingBoxes.add(new Room.RoomPart(min, max));
            }
            return null;
        }
        return entity;
    }


    public class Room {
        private final List<RoomPart> boundingBoxes = new ArrayList<>();
        private final String name;
        private double temperature;

        public Room(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public void calculateTemperature() {
            temperature = 3;
        }

        public List<Player> getPlayers() {
            return gameContext.getInstance().getPlayers().stream().filter((p) -> {
                if (p == gameContext.getCamPlayer()) {
                    return false;
                }
                return contains(p.getPosition());
            }).toList();
        }

        public boolean contains(Point point) {
            for (RoomPart bb : boundingBoxes) {
                if (bb.contains(point)) {
                    return true;
                }
            }
            return false;
        }

        private record RoomPart(Point a, Point b) {
            public boolean contains(Point point) {
                if (a.x() <= point.x() && point.x() <= b.x()) {
                    if (a.y() <= point.y() && point.y() <= b.y()) {
                        return a.z() <= point.z() && point.z() <= b.z();
                    }
                }

                return false;
            }
        }
    }
}
