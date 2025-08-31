package at.nopro.minestomTest.phasmo;

import net.minestom.server.entity.Player;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SanityManager {
    private static Map<Player, Integer> sanity = new HashMap<>();

    public static void setSanity(Player player, int sanity) {
        SanityManager.sanity.put(player, sanity);
    }

    public static void drawSanity(Graphics2D graphics2D, int canvasWidth, int canvasHeight, int padding) {
        int[] x = new int[] {
                padding,
                canvasWidth/2 + padding,
                padding,
                canvasWidth/2 + padding
        };
        int[] y = new int[] {
                padding,
                padding,
                canvasHeight/2 + padding,
                canvasHeight/2 + padding
        };
        int width = canvasWidth / 2 - 1 - 2*padding;
        int height = canvasHeight / 2 - 1 - 2*padding;

        Color[] colors = new Color[] {
                Color.RED,
                Color.GREEN,
                Color.CYAN,
                Color.MAGENTA
        };

        int[] sanity = new int[] {
                90,0,50,33
        };

        boolean[] alive = new boolean[] {
                true,
                false,
                true,
                true
        };

        String[] names = new String[] {
                "Noah",
                "David",
                "Emilio",
                "Fly"
        };

        for (int i = 0; i < 4; i++) {
            if(alive[i]) {
                int sanityPixels = (int) ( width * ( sanity[i] / 100d ) );
                graphics2D.setPaint(colors[i]);
                graphics2D.fillRect(x[i], y[i] + 2 * height / 3, sanityPixels, height / 3);
                graphics2D.setPaint(colors[i].darker());
                graphics2D.fillRect(x[i] + sanityPixels, y[i] + 2 * height / 3, width - sanityPixels, height / 3);
            }

            graphics2D.setPaint(Color.WHITE);

            String s;
            if (alive[i]) {
                s = sanity[i] + "%";
            } else {
                s = "?";
            }
            graphics2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, height / 3));
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            graphics2D.drawString(s, x[i] + width - fontMetrics.stringWidth(s), y[i] + height - 2);
            graphics2D.drawString(names[i], x[i] + 2, y[i] + height / 2 - 2);

            graphics2D.drawRect(x[i], y[i], width, height);

            graphics2D.drawRect(x[i], y[i] + 2 * height / 3, width, height / 3);
        }
    }
}
