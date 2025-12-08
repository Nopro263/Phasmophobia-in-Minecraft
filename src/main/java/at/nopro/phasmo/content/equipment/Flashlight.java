package at.nopro.phasmo.content.equipment;

import at.nopro.phasmo.content.ItemProvider;
import at.nopro.phasmo.event.AfterDropEvent;
import at.nopro.phasmo.event.AfterPickupEvent;
import at.nopro.phasmo.game.GameContext;
import at.nopro.phasmo.game.GameManager;
import at.nopro.phasmo.game.ItemReference;
import at.nopro.phasmo.lighting.PhasmoChunk;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;

public class Flashlight implements Equipment {
    @Override
    public void handle(Event event, Entity en, ItemReference r) {
        switch (event) {
            case PlayerMoveEvent e -> handle(e, en, r);
            case PlayerChangeHeldSlotEvent e -> handle(e, en, r);
            case AfterDropEvent e -> handle(e, en, r);
            case AfterPickupEvent e -> handle(e, en, r);
            case InstanceTickEvent e -> handle(e, en, r);
            default -> {
            }
        }
    }

    @Override
    public ItemStack getDefault() {
        return ItemProvider.getFlashlight();
    }

    private void handle(PlayerMoveEvent moveEvent, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(moveEvent.getInstance());

        if (entity.getPreviousPosition().chunkX() == entity.getPosition().chunkX() && entity.getPreviousPosition().chunkZ() == entity.getPosition().chunkZ()) {
            if (gameContext.getInstance().getChunkAt(entity.getPosition()) instanceof PhasmoChunk phasmoChunk) {
                /*LightSource lightSource = phasmoChunk.getLightSourceForId(r.hashCode());
                if (lightSource == null) {
                    phasmoChunk.addLightSource(new FlashlightLightSource(r));
                }
                if (!( lightSource instanceof FlashlightLightSource flashlightLightSource )) {
                    throw new RuntimeException("invalid light source");
                }

                flashlightLightSource.update();*/
                phasmoChunk.resendLight();
            }
        } else {
            if (gameContext.getInstance().getChunkAt(entity.getPreviousPosition()) instanceof PhasmoChunk previousPhasmoChunk) {
                if (gameContext.getInstance().getChunkAt(entity.getPreviousPosition()) instanceof PhasmoChunk newPhasmoChunk) {

                    /*LightSource lightSource = previousPhasmoChunk.getLightSourceForId(r.hashCode());
                    if (lightSource == null) {
                        lightSource = new FlashlightLightSource(r);
                    }
                    if (!( lightSource instanceof FlashlightLightSource flashlightLightSource )) {
                        throw new RuntimeException("invalid light source");
                    }

                    previousPhasmoChunk.removeLightSource(lightSource.getId());
                    newPhasmoChunk.addLightSource(lightSource);

                    flashlightLightSource.update();*/
                    newPhasmoChunk.resendLight();
                }
            }
        }
    }

    private void handle(PlayerChangeHeldSlotEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(e.getInstance());

        if (gameContext.getInstance().getChunkAt(entity.getPosition()) instanceof PhasmoChunk phasmoChunk) {
            /*LightSource lightSource = phasmoChunk.getLightSourceForId(r.hashCode());
            if (lightSource == null) return;
            if (!( lightSource instanceof FlashlightLightSource flashlightLightSource )) {
                throw new RuntimeException("invalid light source");
            }

            flashlightLightSource.update();*/
            phasmoChunk.resendLight();
        }
    }

    private void handle(AfterDropEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = e.getGameContext();

        if (gameContext.getInstance().getChunkAt(entity.getPosition()) instanceof PhasmoChunk phasmoChunk) {
            /*LightSource lightSource = phasmoChunk.getLightSourceForId(r.hashCode());
            if (lightSource == null) return;
            if (!( lightSource instanceof FlashlightLightSource flashlightLightSource )) {
                throw new RuntimeException("invalid light source");
            }

            flashlightLightSource.update();*/
            phasmoChunk.resendLight();
        }
    }

    private void handle(AfterPickupEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = e.getGameContext();

        if (gameContext.getInstance().getChunkAt(entity.getPosition()) instanceof PhasmoChunk phasmoChunk) {
            /*LightSource lightSource = phasmoChunk.getLightSourceForId(r.hashCode());
            if (lightSource == null) return;
            if (!( lightSource instanceof FlashlightLightSource flashlightLightSource )) {
                throw new RuntimeException("invalid light source");
            }

            flashlightLightSource.update();*/
            phasmoChunk.resendLight();
        }
    }

    private void handle(InstanceTickEvent e, Entity entity, ItemReference r) {
        GameContext gameContext = GameManager.getGame(e.getInstance());

        if (gameContext.getInstance().getChunkAt(entity.getPosition()) instanceof PhasmoChunk phasmoChunk) {
            /*LightSource lightSource = phasmoChunk.getLightSourceForId(r.hashCode());
            if (lightSource == null) return;
            if (!( lightSource instanceof FlashlightLightSource flashlightLightSource )) {
                throw new RuntimeException("invalid light source");
            }

            flashlightLightSource.update();*/
            //phasmoChunk.resendLight();
        }
    }
}
