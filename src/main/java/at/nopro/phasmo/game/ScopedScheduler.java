package at.nopro.phasmo.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScopedScheduler {
    private Map<String, Task> map = new HashMap<>();

    public void run(String id, Supplier<TaskSchedule> supplier) {
        tryCancel(id);
        map.put(id, MinecraftServer.getSchedulerManager().submitTask(supplier));
    }

    public void run(String id, Function<Boolean, TaskSchedule> supplier) {
        tryCancel(id);
        AtomicBoolean isFirst = new AtomicBoolean(true);
        map.put(id, MinecraftServer.getSchedulerManager().submitTask(() -> supplier.apply(isFirst.getAndSet(false))));
    }

    public void tryCancel(String id) {
        if(!map.containsKey(id)) return;

        map.get(id).cancel();
    }

    @ApiStatus.Internal
    public void stop() {
        for(Map.Entry<String,Task> e : map.entrySet()) {
            e.getValue().cancel();
        }
    }
}
