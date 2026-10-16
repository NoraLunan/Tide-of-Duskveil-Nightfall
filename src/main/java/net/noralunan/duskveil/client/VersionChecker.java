package net.noralunan.duskveil.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.library.common.NoraLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class VersionChecker
{
    private static final NoraLogger LOGGER = Duskveil.logger();
    private static final Gson GSON = new Gson();
    private static final String PROMOS = "promos";
    private static final String LATEST = "1.20-latest";

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (DuskveilConfig.CLIENT.other.check_update.get() == false) return;
        String latest = check();
        if (latest != null && !latest.equals(Duskveil.VERSION))
        {
            Component message = Component.translatable("duskveil.message.version_update", latest, Duskveil.VERSION);
            event.getEntity().displayClientMessage(message, false);
            Minecraft.getInstance().stop();
        }
    }

    public static String check()
    {
        return CompletableFuture.supplyAsync(() -> {
            String latest = null;
            try
            {
                URL url = new URL("https://www.github.com/NoraLunan/Tide-of-Duskveil-Nightfall/raw/master/version.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int timeout = DuskveilConfig.CLIENT.other.timeout.get() * 1000;
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);

                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String input;
                    while ((input = in.readLine()) != null)
                    {
                        content.append(input);
                    }
                    in.close();

                    JsonObject object = GSON.fromJson(content.toString(), JsonObject.class);
                    JsonObject obj = object.get(PROMOS).getAsJsonObject();
                    latest = obj.get(LATEST).getAsString();
                }
                else
                {
                    LOGGER.error("Version check failed, unexpected HTTP response code: {}", response);
                }
            }
            catch (Exception exception)
            {
                LOGGER.error("Version check failed: {}", exception);
            }
            return latest;
        }).join();
    }
}
