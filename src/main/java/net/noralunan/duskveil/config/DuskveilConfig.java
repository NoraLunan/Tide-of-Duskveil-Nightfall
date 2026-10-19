package net.noralunan.duskveil.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.config.forge.NoraConfig;

import javax.annotation.Nullable;
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

    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

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

        var common = NoraConfig.create(CommonConfig::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();
    }

    public static boolean reload(ModConfig config)
    {
        if (config.getConfigData() instanceof CommentedFileConfig cfg)
        {
            try
            {
                cfg.load();
                config.getSpec().acceptConfig(cfg);
                LOGGER.info("Successfully reloaded config: {}", config.getFullPath());

                IEventBus event = MinecraftForge.EVENT_BUS;
                if (config == Duskveil.getConfig(ModConfig.Type.SERVER))
                    event.post(new ServerConfigReloadEvent(null));
                else if (config == Duskveil.getConfig(ModConfig.Type.CLIENT))
                    event.post(new ClientConfigReloadEvent(null));

                return true;
            }
            catch (Exception exception)
            {
                LOGGER.error("Failed to reload config: ", exception);
                return false;
            }
        }
        else
        {
            LOGGER.error("Config data is not a {} for: {}", CommentedFileConfig.class.getName(), config.getFullPath());
            return false;
        }
    }

    public static class ServerConfigReloadEvent extends Event {
        private ServerConfigReloadEvent(@Nullable String ignored) {
        }
    }

    public static class ClientConfigReloadEvent extends Event {
        private ClientConfigReloadEvent(@Nullable String ignored) {
        }
    }

    static final String RELOAD = "This entry will only take effect after restarting the game.";

    public static class ServerConfig {
        public final EventConfig event;
        public final GazeConfig gaze;
        public final EntityConfig entity;
        public final SpawnerConfig spawner;

        private ServerConfig(NoraConfig config) {
            config.depict("Settings for Tide Night Event").header(Duskveil.MOD_NAME, Duskveil.VERSION, ModConfig.Type.SERVER);
            this.event = config.layer("Event").nest(EventConfig::new);

            config.depict("""
                    Settings for the Gaze mechanic. If there is a player near the entity, there is a chance for the player to be gazed at
                    The entity will then force-lock the gazed player as its attack target, ignoring distance and solid blocks""");
            this.gaze = config.layer("Gaze").nest(GazeConfig::new);

            config.depict("Settings for entity attributes and behaviors");
            this.entity = config.layer("Entity").nest(EntityConfig::new);


            config.depict("This category controls how the mod spawns entities");
            this.spawner = config.layer("Spawner").nest(SpawnerConfig::new);
        }
    }

    public static class ClientConfig {
        public final OverlayConfig overlay;
        public final OtherClientConfig other;

        private ClientConfig(NoraConfig config) {
            config.depict("Settings for Info Overlay").header(Duskveil.MOD_NAME, Duskveil.VERSION, ModConfig.Type.CLIENT);
            this.overlay = config.layer("Overlay").nest(OverlayConfig::new);

            config.depict("Other Client Settings");
            this.other = config.layer("Other").nest(OtherClientConfig::new);
        }
    }

    public static class CommonConfig {
        public final JsonConfig json;
        public final CommandConfig command;

        private CommonConfig(NoraConfig config) {
            config.depict("Define how the MobListConfig content should be generated").header(Duskveil.MOD_NAME, Duskveil.VERSION, ModConfig.Type.COMMON);
            this.json = config.layer("JSON").nest(JsonConfig::new);
            this.command = config.depict("Mod Command Settings").layer("Command").nest(CommandConfig::new);
        }
    }
}
