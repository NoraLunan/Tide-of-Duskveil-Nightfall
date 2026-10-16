package net.noralunan.duskveil.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.noralunan.duskveil.Duskveil;
import org.jetbrains.annotations.NotNull;

public class DuskveilData extends SavedData
{
    boolean tidenight;
    int count;
    boolean inspected;
    int nextfix;
    int last;
    int wave;
    int nextwave;
    TriggerType trigger;

    private DuskveilData()
    {
        this.tidenight = false;
        this.count = 0;
        this.inspected = false;
        this.nextfix = -1;
        this.last = -1;
        this.wave = 0;
        this.nextwave = -1;
        this.trigger = TriggerType.NONE;
    }

    static DuskveilData get(MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(
                DuskveilData::load,
                DuskveilData::new,
                Duskveil.MOD_ID + "-data"
        );
    }

    void sync()
    {
        this.setDirty();
    }

    private static DuskveilData load(CompoundTag tag)
    {
        return new DuskveilData().read(tag);
    }

    private static final String TIDENIGHT = "TideNight";
    private static final String COUNT = "EventCount";
    private static final String INSPECTED = "Checked";
    private static final String NEXTFIX = "NextFixDay";
    private static final String LAST = "LastEvent";
    private static final String WAVE = "Wave";
    private static final String NEXTWAVE = "NextWave";
    private static final String TRIGGER = "Trigger";

    private DuskveilData read(CompoundTag tag)
    {
        this.tidenight = tag.getBoolean(TIDENIGHT);
        this.count = tag.getInt(COUNT);
        this.inspected = tag.getBoolean(INSPECTED);
        this.nextfix = tag.getInt(NEXTFIX);
        this.last = tag.getInt(LAST);
        this.wave = tag.getInt(WAVE);
        this.nextwave = tag.getInt(NEXTWAVE);
        this.trigger = TriggerType.from(tag.getByte(TRIGGER));
        return this;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag)
    {
        tag.putBoolean(TIDENIGHT, this.tidenight);
        tag.putInt(COUNT, this.count);
        tag.putBoolean(INSPECTED, this.inspected);
        tag.putInt(NEXTFIX, this.nextfix);
        tag.putInt(LAST, this.last);
        tag.putInt(WAVE, this.wave);
        tag.putInt(NEXTWAVE, this.nextwave);
        tag.putByte(TRIGGER, this.trigger.aByte);
        return tag;
    }
}
