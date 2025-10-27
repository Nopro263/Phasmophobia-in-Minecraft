package at.nopro.phasmo.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.UUID;

public class CameraManager {
    private static String camPlayerName;
    private static UUID camPlayerUUID;

    private final GameContext gameContext;

    public CameraManager(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public static String getCamPlayerName() {
        return camPlayerName;
    }

    public static void setCamPlayerName(String camPlayerName) {
        CameraManager.camPlayerName = camPlayerName;
    }

    public static UUID getCamPlayerUUID() {
        return camPlayerUUID;
    }

    public static void setCamPlayerUUID(UUID camPlayerUUID) {
        CameraManager.camPlayerUUID = camPlayerUUID;
    }

    public void teleport(Pos pos) {
        this.gameContext.getCamPlayer().teleport(pos);
    }

    public void setNightVision(boolean enable) {
        if (enable) {
            this.gameContext.getCamPlayer().addEffect(new Potion(PotionEffect.NIGHT_VISION, 1, -1));
        } else {
            this.gameContext.getCamPlayer().removeEffect(PotionEffect.NIGHT_VISION);
        }
    }

    public boolean hasNightVisionEnabled() {
        return this.gameContext.getCamPlayer().hasEffect(PotionEffect.NIGHT_VISION);
    }
}
