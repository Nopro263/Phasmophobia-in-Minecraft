package at.nopro.phasmo.gameplay.ingame;

import at.nopro.phasmo.core.world.BaseInstance;
import at.nopro.phasmo.core.world.DimensionTypes;
import at.nopro.phasmo.core.world.WorldMeta;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameInstance extends BaseInstance {
    public GameInstance() {
        super(DimensionTypes.SHORT_MAP);

        setWorldMeta(new Meta());
    }

    public static class Meta implements WorldMeta {
        public static final int VERSION = 0;
        public Pos spawnPos = new Pos(0, 18, 0);

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
