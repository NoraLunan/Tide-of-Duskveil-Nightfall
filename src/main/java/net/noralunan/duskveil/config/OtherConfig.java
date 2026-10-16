package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.library.config.forge.NoraConfig;

public class OtherConfig
{
    public final ConfigValue<Boolean> modify_sky;
    public final ConfigValue<String> fog_color;
    public final ConfigValue<Boolean> lock_gamma;
    public final ConfigValue<Boolean> check_update;
    public final ConfigValue<Integer> timeout;

    protected OtherConfig(NoraConfig config)
    {
        this.modify_sky = config
                .depict("Should the sky color be overridden during a Tide Night?")
                .entry("Override Sky", true);
        this.fog_color = config
                .depict("The color value for the sky during a Tide Night.")
                .entry("Sky Color", "#992626");
        this.lock_gamma = config
                .depict("Should player screen brightness be locked to a dim setting?")
                .entry("Lock Gamma Value", true);
        this.check_update = config
                .depict("Whether to check for new mod version when a player joins the game?")
                .depict("If enabled and network connection is poor, this may cause lag depending on the timeout setting.")
                .entry("Check Update", true);
        this.timeout = config
                .depict("Maximum time in seconds to wait for the update check before timing out.")
                .min(0)
                .entry("Check Update Timeout", 3);
    }
}
