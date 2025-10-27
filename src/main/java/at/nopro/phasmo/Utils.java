package at.nopro.phasmo;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Pos;

import java.awt.*;

public class Utils {
    public static Pos addInDirection(Pos pos, double toAdd) {
        return pos.add(pos.direction().mul(toAdd));
    }

    public static Pos normalize(Pos pos) {
        return pos.apply((x, y, z, yaw, pitch) -> {
            double length = Math.sqrt(x * x + y * y + z * z);
            x = x / length;
            y = y / length;
            z = z / length;
            return new Pos(x, y, z, yaw, pitch);
        });
    }

    public static Color fromTextColor(TextColor textColor) {
        return new Color(
                textColor.red(),
                textColor.green(),
                textColor.blue()
        );
    }

    public static TextColor fromAWTColor(Color color) {
        return TextColor.color(
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }
}
