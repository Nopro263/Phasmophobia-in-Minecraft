package at.nopro.phasmo.content;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.Map;

public class ItemModelProvider {
    private static final Map<String, ItemModel> itemModelBoxes = Map.of(
            "phasmo:emf_reader", new ItemModel(
                    new BoundingBox(0.5,0.1875,0.5),
                    new Pos(-0.350,0.250,-0.120),
                    new float[] {0,0,0,1},
                    Vec.ONE,
                    new float[] {0,0,0,1}
            ),
            "phasmo:book_closed", new ItemModel(//TODO
                    new BoundingBox(1,0.5,1),
                    new Pos(-0.350,0.250,-0.120),
                    new float[] {0,0,0,1},
                    Vec.ONE,
                    new float[] {0,0,0,1}
            ),
            "phasmo:book_open", new ItemModel(//TODO
                    new BoundingBox(1.5,0.5,1.5),
                    new Pos(-0.350,0.250,-0.120),
                    new float[] {0,0,0,1},
                    Vec.ONE,
                    new float[] {0,0,0,1}
            )
    );

    public record ItemModel(
            BoundingBox boundingBox,
            Point translation,
            float[] leftRotation,
            Vec scale,
            float[] rightRotation
    ) {}

    public static ItemModel getItemModel(String model) {
        return itemModelBoxes.get(model);
    }
}
