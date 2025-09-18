package at.nopro.phasmo.game;

import at.nopro.phasmo.event.GhostEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.util.Random;

public class ActivityManager {
    private final GameContext gameContext;
    private final int[] dataPoints = new int[60];
    private int pointer = 0;

    private int currentActivity = 0;
    private final Random random = new Random();

    public ActivityManager(GameContext gameContext) {
        this.gameContext = gameContext;

        gameContext.getScheduler().run(this.hashCode() + "activity-manager", (first) -> {
            if(first) {
                return TaskSchedule.seconds(1);
            }
            synchronized (ActivityManager.this) {
                dataPoints[pointer++] = Math.min(currentActivity,10);
                pointer %= 60;
                currentActivity = Math.max(0, Math.min(currentActivity, 10)-random.nextInt(11));
            }

            gameContext.getDisplayManager().drawActivity();
            return TaskSchedule.seconds(1);
        });
    }

    @ApiStatus.Internal
    public void onGhostEvent(GhostEvent ghostEvent) {
        synchronized (ActivityManager.this) {
            currentActivity += ghostEvent.getEmfLevel();
        }
    }

    @ApiStatus.Internal
    public void drawActivityLine(Graphics2D graphics2D, int minX, int maxX, int minY, int maxY) {
        int writePointer = this.pointer;
        int pointer = (writePointer + 1) % 60;
        int previousValue = -1;

        double dx = (maxX - minX) / 60d;
        double dy = (maxY - minY) / 10d;

        int i = 0;

        while (pointer != writePointer) {

            if(previousValue != -1) {
                graphics2D.drawLine((int) ((i-1)*dx + minX), (int) (previousValue*dy + minY), (int) (i*dx + minX), (int) (dataPoints[pointer]*dy + minY));
            }

            previousValue = dataPoints[pointer];
            pointer = (pointer + 1) % 60;
            i++;
        }
    }
}
