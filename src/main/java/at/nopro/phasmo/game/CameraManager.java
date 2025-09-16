package at.nopro.phasmo.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public class CameraManager {
    private final GameContext gameContext;

    public CameraManager(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void teleport(Pos pos) {
        this.gameContext.getCamPlayer().teleport(pos);
    }

    public void setNightVision(boolean enable) {
        if(enable) {
            this.gameContext.getCamPlayer().addEffect(new Potion(PotionEffect.NIGHT_VISION, 1, -1));
        } else {
            this.gameContext.getCamPlayer().removeEffect(PotionEffect.NIGHT_VISION);
        }
    }

    public boolean hasNightVisionEnabled() {
        return this.gameContext.getCamPlayer().hasEffect(PotionEffect.NIGHT_VISION);
    }
}
