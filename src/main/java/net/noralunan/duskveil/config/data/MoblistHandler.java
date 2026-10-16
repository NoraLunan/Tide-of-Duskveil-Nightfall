package net.noralunan.duskveil.config.data;

import com.google.gson.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.JsonConfig;
import net.noralunan.library.common.NoraLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MoblistHandler
{
    private static final NoraLogger LOGGER = Duskveil.logger();
    private static final Gson GSON = new Gson();
    protected static final File JSON = new File(DuskveilConfig.MOBLIST_PATH);

    public static final String MOB_ID = "Mob ID";
    public static final String START_HIS = "Start History";
    public static final String END_HIS = "Stop History";
    public static final String MIN_COUNT = "Min Count";
    public static final String MAX_COUNT = "Max Count";

    private static volatile JsonObject cache = new JsonObject();
    private static final Object LOCK = new Object();

    @SubscribeEvent
    public static void reload(MoblistConfig.ReloadEvent event)
    {
        sync();
    }

    protected static void sync()
    {
        synchronized (LOCK)
        {
            cache = perform();
        }
    }

    protected static JsonObject read()
    {
        synchronized (LOCK)
        {
            return perform();
        }
    }

    private static JsonObject get()
    {
        return cache;
    }

    private static JsonObject perform()
    {
        JsonObject empty = new JsonObject();
        if (!JSON.exists())
        {
            return empty;
        }
        try
        {
            String content = Files.readString(JSON.toPath());
            if (content.trim().isEmpty())
            {
                return empty;
            }
            JsonObject result = GSON.fromJson(content, JsonObject.class);
            return result == null ? empty : result;
        }
        catch (IOException exception)
        {
            LOGGER.error("Failed to read or parse config file: {}", JSON, exception);
        }
        catch (JsonSyntaxException exception)
        {
            LOGGER.error("Failed to parse JSON in config file: {}", JSON, exception);
        }
        return empty;
    }

    protected static class Inspect
    {
        private static boolean whole(String key)
        {
            if (key == null || key.isEmpty()) return false;

            JsonObject full = get();
            if (!(full.has(key) && full.get(key).isJsonObject()))
                return false;

            JsonObject single = full.get(key).getAsJsonObject();
            if (single == null) return false;

            return string(single, MOB_ID)
                    && number(single, START_HIS)
                    && number(single, END_HIS)
                    && number(single, MIN_COUNT)
                    && number(single, MAX_COUNT);
        }

        private static boolean string(JsonObject single, String entry)
        {
            JsonPrimitive pri = getPrimitive(single, entry);
            if (pri == null) return false;
            else return pri.isString();
        }

        private static boolean number(JsonObject single, String entry)
        {
            JsonPrimitive pri = getPrimitive(single, entry);
            if (pri == null) return false;
            else return pri.isNumber();
        }
    }

    @Nullable
    private static JsonPrimitive getPrimitive(JsonObject single, String entry)
    {
        if (single == null) return null;
        JsonElement element = single.get(entry);

        if (element != null && element.isJsonPrimitive()) return element.getAsJsonPrimitive();
        else return null;
    }

    private static JsonObject getSingle(String key)
    {
        JsonObject content = get();
        if (!Inspect.whole(key) || content == null) return null;
        else return content.get(key).getAsJsonObject();
    }

    public static class Getter
    {
        public final String mob_id;
        public final int start_his;
        public final int end_his;
        public final int min_count;
        public final int max_count;

        protected Getter(String key)
        {
            JsonObject single = getSingle(key);
            if (single == null)
            {
                JsonConfig config = DuskveilConfig.COMMON.json;
                this.mob_id = config.mobId().replace("{key}", key.toLowerCase());
                this.start_his = config.startHistory();
                this.end_his = config.endHistory();
                this.min_count = config.minCount();
                this.max_count = config.maxCount();
                LOGGER.error("Entity {} configuration verification is invalid, use the default", key);
                return;
            }
            this.mob_id = single.get(MOB_ID).getAsString();
            this.start_his = single.get(START_HIS).getAsInt();
            this.end_his = single.get(END_HIS).getAsInt();
            this.min_count = single.get(MIN_COUNT).getAsInt();
            this.max_count = single.get(MAX_COUNT).getAsInt();
        }
    }
}
