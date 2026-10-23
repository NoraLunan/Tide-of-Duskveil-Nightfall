package net.noralunan.duskveil.config.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.JsonConfig;
import net.noralunan.library.common.NoraLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class MoblistConfig
{
    private final NoraLogger logger;
    private final File file;
    private JsonObject content;
    private boolean change;
    private MoblistConfig()
    {
        this.logger = Duskveil.logger();
        this.file = MoblistHandler.JSON;
        this.content = new JsonObject();
        this.change = false;
    }

    @SubscribeEvent
    public static void load(@Nullable ServerAboutToStartEvent event)
    {
        MoblistConfig moblist = new MoblistConfig();
        moblist.load();
        MinecraftForge.EVENT_BUS.post(new ReloadEvent(moblist.content));
    }

    public static class ReloadEvent extends Event
    {
        private final JsonObject content;
        private ReloadEvent(JsonObject content)
        {
            this.content = content;
        }

        public JsonObject getFileContent()
        {
            return this.content.deepCopy();
        }
    }

    private void load()
    {
        if (this.file.exists())
        {
            this.content = MoblistHandler.read();
            maintain();
        }
        else
        {
            try
            {
                this.logger.warn("{} is inexistent, creating", DuskveilConfig.MOBLIST_PATH);
                create();
            }
            catch (IOException exception)
            {
                this.logger.error("Failed to create config file: ", exception);
            }
        }
    }

    public static MoblistHandler.Getter get(String key)
    {
        return new MoblistHandler.Getter(key);
    }

    private void maintain()
    {
        Set<String> valid = new LinkedHashSet<>(DuskveilConfig.COMMON.json.keyList());
        boolean change = false;
        Runnable log = () -> this.logger.warn("{} is incorrect, correcting", DuskveilConfig.MOBLIST_PATH);
        // 移除冗余项
        Set<String> redundant = new HashSet<>();
        for (String jsonKey : this.content.keySet())
        {
            if (!valid.contains(jsonKey))
            {
                redundant.add(jsonKey);
                if (!change)
                {
                    change = true;
                    log.run();
                }
            }
        }
        if (!redundant.isEmpty())
        {
            redundant.forEach(this.content::remove);
            this.logger.warn("Removed {} invalid keys from config.", redundant.size());
        }
        // 添加缺失项
        for (String configKey : valid)
        {
            if (!this.content.has(configKey))
            {
                if (!change)
                {
                    change = true;
                    log.run();
                }
                addDefault(configKey);
            }
        }
        this.change = change;
        write();
    }

    private void create() throws IOException
    {
        File dir = this.file.getParentFile();
        if (dir != null && !dir.exists())
        {
            if (!dir.mkdirs())
            {
                this.logger.error("Failed to create parent directories for config file: {}", dir);
                return;
            }
        }

        if (this.file.createNewFile())
        {
            DuskveilConfig.COMMON.json.keyList().forEach(this::addDefault);
            this.change = true;
            write();
        }
    }

    private void addDefault(String key)
    {
        JsonObject entry = new JsonObject();
        JsonConfig config = DuskveilConfig.COMMON.json;
        String mob_id = config.mobId();
        entry.addProperty(MoblistHandler.MOB_ID, mob_id.replace("{key}", key.toLowerCase()));
        entry.addProperty(MoblistHandler.START_HIS, config.startHistory());
        entry.addProperty(MoblistHandler.END_HIS, config.endHistory());
        entry.addProperty(MoblistHandler.MIN_COUNT, config.minCount());
        entry.addProperty(MoblistHandler.MAX_COUNT, config.maxCount());
        this.content.add(key, entry);

        this.logger.warn("Missing key {}, added", key);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private void write()
    {
        if (!this.change) return;
        File temp = new File(this.file.getAbsolutePath() + ".tmp");
        // 写入临时文件
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8))
        {
            writer.write(GSON.toJson(this.content));
            writer.flush();
        }
        catch (IOException exception)
        {
            this.logger.error("Failed to write config file: ", exception);
            removeTemp(temp);
            return;
        }
        // 写入配置文件
        try
        {
            Files.move(temp.toPath(), this.file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            this.logger.info("Moblist config file has been successfully updated: {}", this.file.toPath());
            MoblistHandler.sync();
            this.change = false;
        }
        catch (IOException abc)
        {
            this.logger.warn("Atomic replacement failed, attempting direct replacement.");
            try
            {
                Files.move(temp.toPath(), this.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                MoblistHandler.sync();
                this.change = false;
            }
            catch (IOException exception)
            {
                this.logger.error("Failed to replace config file: ", exception);
            }
            removeTemp(temp);
        }
    }

    private void removeTemp(File temp)
    {
        if (temp.exists() && !temp.delete())
        {
            this.logger.error("Failed to delete temporary files: {}", temp);
        }
    }
}
