package at.nopro.phasmo.core.config;

import at.nopro.phasmo.Main;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Configuration {
    private static final Path path = Path.of("config.yml");
    private static final File file = path.toFile();

    public static Config config;

    public static void parseOrCreate() throws IOException {
        InputStream templateConfig = Main.class.getResourceAsStream("config.yml");
        Objects.requireNonNull(templateConfig);


        if (!file.exists()) {
            Files.copy(templateConfig, path);
            System.out.println("Created the configuration file, please change anything you need. Exiting...");
            System.exit(1);
            return;
        }


        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true);

        Yaml yaml = new Yaml(new Constructor(Config.class, loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions);

        config = yaml.load(new FileInputStream(file));
    }

    public static class Config {
        public mcServer mcServer;
        public resourcepackServer resourcepackServer;
        public camera camera;
        public voicechat voicechat;
        public boolean devMode;

        public static class mcServer {
            public String host;
            public int port;
            public String auth;
            public String secret;
        }

        public static class voicechat {
            public String host;
            public int port;
            public boolean enabled;
        }

        public static class resourcepackServer {
            public bind bind;
            public access access;

            public static class bind {
                public String host;
                public int port;
                public String path;
            }

            public static class access {
                public String host;
                public int port;
                public boolean https;
            }
        }

        public static class camera {
            public boolean enabled;
            public String playerName;
            public String playerUuid;
            public String headlessmcPath;
            public String minecraftPath;
        }
    }
}
