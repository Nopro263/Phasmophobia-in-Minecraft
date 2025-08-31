package at.nopro.minestomTest.phasmo;

import java.awt.*;

public class GhostActivityManager {
    private static final int[] pastActivity = new int[60];
    private static int activityWriterPointer = 0;

    public static void addActivity(int intensity) {
        pastActivity[activityWriterPointer++] = intensity;
        activityWriterPointer %= 60;
    }

    public static void drawActivityLine(Graphics2D graphics2D, int minX, int maxX, int minY, int maxY) {
        int writePointer = activityWriterPointer;
        int pointer = (writePointer + 1) % 60;
        int previousValue = -1;

        double dx = (maxX - minX) / 60d;
        double dy = (maxY - minY) / 10d;

        int i = 0;

        while (pointer != writePointer) {

            if(previousValue != -1) {
                graphics2D.drawLine((int) ((i-1)*dx + minX), (int) (previousValue*dy + minY), (int) (i*dx + minX), (int) (pastActivity[pointer]*dy + minY));
            }

            previousValue = pastActivity[pointer];
            pointer = (pointer + 1) % 60;
            i++;
        }
    }
}
