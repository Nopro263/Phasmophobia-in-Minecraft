package at.nopro.old_phasmo.game;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private static final Map<String, GameContext> game = new HashMap<>();

    public static GameContext getGame(String id) {
        return game.get(id);
    }

    public static GameContext createGame(String id, MapContext mapContext) {
        return game.put(id, new GameContext(mapContext));
    }

    public static @Nullable GameContext getGame(Instance instance) {
        if (instance == null) {
            return null;
        }
        for (GameContext ctx : game.values()) {
            if (ctx.getInstance().getUuid() == instance.getUuid()) {
                return ctx;
            }
        }

        return null;
    }
}
