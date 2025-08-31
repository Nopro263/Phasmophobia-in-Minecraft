package at.nopro.minestomTest.skyblock.personal_island;

import net.minestom.server.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PersonalManager {
    private static ConcurrentHashMap<String, Personal> map = new ConcurrentHashMap<>();

    public static Personal getOrCreate(Player player) {
        if(map.containsKey(player.getUsername())) {
            return map.get(player.getUsername());
        }
        map.put(player.getUsername(), new Personal(player));
        return map.get(player.getUsername());
    }
}
