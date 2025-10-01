package at.nopro.phasmo.content.ghost;

import at.nopro.phasmo.game.GameContext;
import net.minestom.server.entity.EntityType;

public class TestGhost extends BaseGhost {
    public TestGhost(GameContext gameContext) {
        super(EntityType.WITHER_SKELETON, gameContext);

        activateEMF5();
        activateFreezing();
    }
}
