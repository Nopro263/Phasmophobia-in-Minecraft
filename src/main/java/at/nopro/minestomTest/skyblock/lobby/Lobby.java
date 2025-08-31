package at.nopro.minestomTest.skyblock.lobby;

import at.nopro.minestomTest.ext.ReadonlyInstanceView;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;

import java.util.UUID;

public class Lobby {
    private ReadonlyInstanceView instance;
    private InstanceContainer instanceContainer;
    private Pos pos;

    public Lobby() {
        this.instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.instanceContainer.setChunkLoader(new AnvilLoader("skyblock/lobby"));
        //this.instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
        this.instanceContainer.setChunkSupplier(LightingChunk::new);
        JNoise noise = JNoise.newBuilder()
                .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
                .scale(0.005) // Low frequency for smooth terrain
                .build();

// Set the Generator
        instanceContainer.setGenerator(unit -> {
            Point start = unit.absoluteStart();
            for (int x = 0; x < unit.size().x(); x++) {
                for (int z = 0; z < unit.size().z(); z++) {
                    Point bottom = start.add(x, 0, z);

                    synchronized (noise) { // Synchronization is necessary for JNoise
                        double height = noise.evaluateNoise(bottom.x(), bottom.z()) * 16;
                        // * 16 means the height will be between -16 and +16
                        unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
                    }
                }
            }
        });

        this.instance = new ReadonlyInstanceView(UUID.randomUUID(), this.instanceContainer);

        MinecraftServer.getInstanceManager().registerSharedInstance(this.instance);

        this.pos = new Pos(0.5,41,0.5);
    }

    public void teleport(Player player) {
        player.setInstance(instance, pos);
    }

    public void teleportAdmin(Player player) {
        player.setInstance(instanceContainer, pos);
    }

    public Instance getInstance() {
        return instance;
    }

    public Pos getPos() {
        return pos;
    }
}
