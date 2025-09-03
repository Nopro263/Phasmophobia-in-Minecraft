package at.nopro.phasmo.game;

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
}
