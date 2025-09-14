package at.nopro.phasmo.content;

import net.minestom.server.collision.BoundingBox;

import java.util.Map;

public class ItemModelProvider {
    private static final Map<String, BoundingBox> itemModelBoxes = Map.of(
            "phasmo:emf_reader", new BoundingBox(0.75,0.5,0.75),
            "phasmo:book_closed", new BoundingBox(1,0.25,1),
            "phasmo:book_open", new BoundingBox(1.5,0.25,1.5)
    );

    public static BoundingBox getItemBoundingBox(String model) {
        return itemModelBoxes.get(model);
    }
}
