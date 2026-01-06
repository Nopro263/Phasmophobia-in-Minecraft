package at.nopro.phasmo.core.world;

import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.world.DimensionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class WorldLoader {
    public static final String PREFIX = "worlds";
    //TODO anvil import

    public static void createWorld(String name, DimensionType dimensionType) throws IOException {
        var path = Path.of(PREFIX, name + ".polar");

        PolarWorld world = new PolarWorld(dimensionType);

        Files.write(path, PolarWriter.write(world));
    }

    public static <T extends BaseInstance> T loadWorld(String name, T instance) throws IOException {
        var path = Path.of(PREFIX, name + ".polar");

        PolarWorld world = PolarReader.read(Files.readAllBytes(path));

        PolarLoader loader = new PolarLoader(path, world);
        loader.setWorldAccess(instance.getWorldMeta());
        loader.loadInstance(instance);
        instance.setChunkLoader(loader);
        instance.onLoad();
        return instance;
    }

    public static List<String> getWorldNames() {
        File file = new File(PREFIX);
        return Arrays.stream(file.list()).filter(s -> s.endsWith(".polar")).map(s -> s.substring(0, s.length() - 6)).toList();
    }
}
