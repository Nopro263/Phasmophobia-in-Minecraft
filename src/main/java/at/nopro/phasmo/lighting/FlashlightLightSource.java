package at.nopro.phasmo.lighting;

import at.nopro.phasmo.game.ItemReference;

public class FlashlightLightSource extends ConeLightSource {
    private final ItemReference itemReference;

    public FlashlightLightSource(ItemReference itemReference) {
        super(itemReference.getContainingEntity().getPosition(), 15, 1);
        this.itemReference = itemReference;
    }

    public void update() {
        this.source = itemReference.getContainingEntity().getPosition();
    }

    @Override
    public long getId() {
        return itemReference.hashCode();
    }
}
