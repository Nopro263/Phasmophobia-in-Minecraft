package at.nopro.phasmo.content;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.Map;

public class ItemModelProvider {
    private static final Map<String, ItemModel> itemModelBoxes = Map.of(
            "phasmo:emf_reader", new ItemModel(
                    new BoundingBox(0.5, 0.1875, 0.5),
                    new Pos(-0.350, 0.250, -0.120),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "phasmo:book_closed", new ItemModel(
                    new BoundingBox(0.85, 0.250, 0.85),
                    new Pos(-0.15, 0.250, 0),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "phasmo:book_open", new ItemModel(
                    new BoundingBox(1.25, 0.5, 1.25),
                    new Pos(-0.55, 0.5, 0),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "phasmo:cam", new ItemModel(
                    new BoundingBox(0.3, 0.3, 0.3),
                    new Pos(-0.15, 0.22, 0.15),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "minecraft:lantern", new ItemModel(
                    new BoundingBox(0.5, 0.1875, 0.5),
                    new Pos(-0.350, 0.250, -0.120),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "minecraft:heavy_core", new ItemModel(
                    new BoundingBox(0.5, 0.1875, 0.5),
                    new Pos(-0.350, 0.250, -0.120),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "minecraft:iron_pickaxe", new ItemModel(
                    new BoundingBox(0.5, 0.1875, 0.5),
                    new Pos(-0.350, 0.250, -0.120),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "minecraft:netherite_pickaxe", new ItemModel(
                    new BoundingBox(0, 0, 0),
                    new Pos(-0.350, 0.250, -0.120),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            ),
            "minecraft:oxidized_copper_lantern", new ItemModel(
                    new BoundingBox(0.5, 0.1875, 0.5),
                    new Pos(-0.350, 0.250, -0.120),
                    new float[]{ 0, 0, 0, 1 },
                    Vec.ONE,
                    new float[]{ 0, 0, 0, 1 }
            )
    );

    public static ItemModel getItemModel(String model) {
        return itemModelBoxes.get(model);
    }

    public record ItemModel(
            BoundingBox boundingBox,
            Point translation,
            float[] leftRotation,
            Vec scale,
            float[] rightRotation
    ) {
    }
}
