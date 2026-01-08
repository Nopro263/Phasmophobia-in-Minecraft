package at.nopro.phasmo.gameplay.lobby;

import at.nopro.phasmo.core.world.BaseInstance;
import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldLoader;
import at.nopro.phasmo.core.world.WorldMeta;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityStatuses;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class LobbyInstance extends BaseInstance {
    public static LobbyInstance INSTANCE;

    private LobbyInstance() {
        super(DimensionTypes.LOBBY);
        setReadonly(true);
        setWorldMeta(new Meta());

        eventNode().addListener(PlayerSpawnEvent.class, this::onPlayerSpawn);
    }

    private void onPlayerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        playerSpawnEvent.getPlayer().teleport(getWorldMeta().spawnPos);
        playerSpawnEvent.getPlayer().setGameMode(GameMode.SURVIVAL);
        playerSpawnEvent.getPlayer().sendPacket(new EntityStatusPacket(
                playerSpawnEvent.getPlayer().getEntityId(),
                (byte) EntityStatuses.Player.PERMISSION_LEVEL_0
        ));
    }

    @Override
    public Meta getWorldMeta() {
        return (Meta) super.getWorldMeta();
    }

    public static void init() throws IOException {
        INSTANCE = new LobbyInstance();
        WorldLoader.loadWorld("lobby", INSTANCE);
    }

    public static class Meta implements WorldMeta {
        public static final int VERSION = 0;
        public Pos spawnPos = new Pos(0, 107, 0);

        @Override
        public void loadWorldData(@NotNull Instance instance, @Nullable NetworkBuffer userData) {
            if (userData == null) return;

            int readVersion = userData.read(NetworkBuffer.VAR_INT);
            if (readVersion != VERSION) {
                System.err.println("version mismatch, migrating");
                //TODO migration
            }

            spawnPos = userData.read(NetworkBuffer.POS);
        }

        @Override
        public void saveWorldData(@NotNull Instance instance, @NotNull NetworkBuffer userData) {
            userData.write(NetworkBuffer.VAR_INT, VERSION);
            userData.write(NetworkBuffer.POS, spawnPos);

        }
    }
}
