package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.config.forge.NoraConfig;

import java.io.File;

public class DuskveilConfig
{
    private static final NoraLogger LOGGER;

    public static final String SERVER_PATH;
    public static final String CLIENT_PATH;
    public static final String COMMON_PATH;
    public static final String MOBLIST_PATH;

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static
    {
        LOGGER = Duskveil.logger();

        String sep = File.separator;
        String modid = Duskveil.MOD_ID;
        String dir = FMLPaths.CONFIGDIR.get().toString() + sep + modid + sep;
        SERVER_PATH = dir + modid + "-server.toml";
        CLIENT_PATH = dir + modid + "-client.toml";
        COMMON_PATH = dir + modid + "-common.toml";
        MOBLIST_PATH = dir + modid + "-moblist.json";

        var server = NoraConfig.create(ServerConfig::new);
        SERVER = server.getLeft();
        SERVER_SPEC = server.getRight();

        var client = NoraConfig.create(ClientConfig::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
        // TODO: CommonConfig
    }

    public static class ServerConfig
    {
        public final EventConfig event;
        public final GazeConfig gaze;
        public final EntityConfig entity;
        public final SpawnerConfig spawner;

        private ServerConfig(NoraConfig config)
        {
            config.depict("Settings for Tide Night Event").header(Duskveil.MOD_NAME, Duskveil.VERSION, ModConfig.Type.SERVER);
            this.event = config.layer("Event").define(EventConfig::new);

            config.depict("""
                    Settings for the Gaze mechanic. If there is a player near the entity, there is a chance for the player to be gazed at
                    The entity will then force-lock the gazed player as its attack target, ignoring distance and solid blocks""");
            this.gaze = config.layer("Gaze").define(GazeConfig::new);

            config.depict("Settings for entity attributes and behaviors");
            this.entity = config.layer("Entity").define(EntityConfig::new);


            config.depict("This category controls how the mod spawns entities");
            this.spawner = config.layer("Spawner").define(SpawnerConfig::new);
        }
    }

    public static class ClientConfig
    {
        public final OverlayConfig overlay;
        public final OtherConfig other;

        private ClientConfig(NoraConfig config)
        {
            config.depict("Settings for Info Overlay").header(Duskveil.MOD_NAME, Duskveil.VERSION, ModConfig.Type.CLIENT);
            this.overlay = config.layer("Overlay").define(OverlayConfig::new);

            config.depict("Other Client Settings");
            this.other = config.layer("Other").define(OtherConfig::new);
        }
    }
}
