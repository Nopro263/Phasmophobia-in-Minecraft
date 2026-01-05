package at.nopro.phasmo.core;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;

public final class DimensionTypes {
    public static final DimensionType SHORT_MAP = DimensionType.builder()
            .minY(0)
            .logicalHeight(0)
            .height(64)
            .skylight(false)
            .skybox(DimensionType.Skybox.NONE)
            .build();

    public static final DimensionType MEDIUM_TALL_MAP = DimensionType.builder()
            .minY(0)
            .logicalHeight(0)
            .height(128)
            .skylight(false)
            .skybox(DimensionType.Skybox.NONE)
            .build();

    public static final DimensionType TALL_MAP = DimensionType.builder()
            .minY(0)
            .logicalHeight(0)
            .height(256)
            .skylight(false)
            .skybox(DimensionType.Skybox.NONE)
            .build();

    public static final DimensionType LOBBY = DimensionType.builder()
            .minY(0)
            .logicalHeight(0)
            .height(64)
            .skylight(true)
            .skybox(DimensionType.Skybox.OVERWORLD)
            .cardinalLight(DimensionType.CardinalLight.NETHER)
            .build();

    public static final DimensionType[] VALUES = new DimensionType[]{ SHORT_MAP, MEDIUM_TALL_MAP, TALL_MAP };

    private DimensionTypes() {
    }

    public static void init() {
        MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("phasmophobia", "short"),
                SHORT_MAP
        );

        MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("phasmophobia", "medium_tall"),
                MEDIUM_TALL_MAP
        );

        MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("phasmophobia", "tall"),
                TALL_MAP
        );

        MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("phasmophobia", "lobby"),
                LOBBY
        );
    }

    public static RegistryKey<DimensionType> getDimensionTypeKey(int minY, int maxY) {
        for (DimensionType dimensionType : VALUES) {
            if (dimensionType.minY() == minY && dimensionType.maxY() == maxY) {
                return getKeyFor(dimensionType);
            }
        }
        return null;
    }

    public static RegistryKey<DimensionType> getKeyFor(DimensionType dimensionType) {
        return MinecraftServer.getDimensionTypeRegistry().getKey(dimensionType);
    }
}
