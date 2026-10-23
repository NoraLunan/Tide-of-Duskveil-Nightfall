package net.noralunan.duskveil.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.noralunan.duskveil.config.data.MoblistHandler;
import net.noralunan.library.config.forge.NoraConfig;

import java.util.List;

public class JsonConfig {
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> key_list;
    private ForgeConfigSpec.ConfigValue<String> mob_id;
    private ForgeConfigSpec.ConfigValue<Integer> start_his;
    private ForgeConfigSpec.ConfigValue<Integer> end_his;
    private ForgeConfigSpec.ConfigValue<Integer> min_count;
    private ForgeConfigSpec.ConfigValue<Integer> max_count;
    // TODO: 添加 display_name 条目

    protected JsonConfig(NoraConfig config) {
        this.key_list = config.depict("""
                This list dynamically generates the "duskveil-moblist.json" config file.
                Entries are only used as JSON keys and can be freely named.
                Do not use non-Latin characters (Chinese, Korean, Japanese, etc.) that cannot be typed directly on a standard keyboard."""
        ).lineBreak().validator(String.class).entry("JSON Key List", Lists.newArrayList("Zombie"));

        config.depict("Set the default values for each entity data entry").layer("Default").nest(() -> {
            this.mob_id = config.depict("The full namespaced ID of the mob.").entry(MoblistHandler.MOB_ID, "minecraft:{key}");
            this.start_his = config.depict("The minimum event history count required for the mob to spawn. The first event counts as 1.")
                    .min(1).entry(MoblistHandler.START_HIS, 1);
            this.end_his = config.depict("The maximum event history count allowed for the mob to spawn. Set to 0 to allow spawning indefinitely.")
                    .min(0).entry(MoblistHandler.END_HIS, 0);
            this.min_count = config.depict("The minimum number of mobs spawned in a single spawn attempt.")
                    .min(0).entry(MoblistHandler.MIN_COUNT, 12);
            this.max_count = config.depict("The maximum number of mobs spawned in a single spawn attempt.")
                    .min(1).entry(MoblistHandler.MAX_COUNT, 24);
        });
    }

    public List<? extends String> keyList() {
        return this.key_list.get();
    }

    public String mobId() {
        return this.mob_id.get();
    }

    public int startHistory() {
        return this.start_his.get();
    }

    public int endHistory() {
        return this.end_his.get();
    }

    public int minCount() {
        return this.min_count.get();
    }

    public int maxCount() {
        return this.max_count.get();
    }
    
}
