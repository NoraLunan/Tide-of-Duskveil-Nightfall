package net.noralunan.duskveil.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.noralunan.duskveil.Duskveil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

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

    private static volatile DuskveilData client = new DuskveilData();

    static DuskveilData get(Level level)
    {
        if (level.isClientSide) return client;
        else return Objects.requireNonNull(level.getServer()).overworld().getDataStorage().computeIfAbsent(
                DuskveilData::load,
                DuskveilData::new,
                Duskveil.MOD_ID + "-data"
        );
    }

    void sync(Level level)
    {
        if (level.isClientSide) return;
        this.setDirty();
        NetWork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new SyncPacket(this)
        );
    }

    public static boolean isTrigger(Level level)
    {
        return getTrigger(level) != TriggerType.NONE;
    }

    public static TriggerType getTrigger(Level level)
    {
        return get(level).trigger;
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

    private static void updateClientSnapshot(DuskveilData state)
    {
        client = state;
    }

    @SubscribeEvent
    public static void push(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity().level().isClientSide()) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        DuskveilData data = DuskveilData.get(player.level());
        NetWork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncPacket(data)
        );
    }

    private record SyncPacket(DuskveilData data)
    {
        private static void write(SyncPacket packet, FriendlyByteBuf buf)
        {
            if (packet.data != null)
            {
                buf.writeNbt(packet.data.save(new CompoundTag()));
            }
        }

        private static void apply(SyncPacket packet, Supplier<NetworkEvent.Context> context)
        {
            NetworkEvent.Context netContext = context.get();
            netContext.enqueueWork(() -> {
                if (!netContext.getDirection().getReceptionSide().isServer() && packet.data != null)
                {
                    updateClientSnapshot(packet.data);
                }
            });
            netContext.setPacketHandled(true);
        }
    }

    public static class NetWork
    {
        private static final String PROTOCOL = "1.1";
        private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Duskveil.MOD_ID, "channel"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

        private static int sequence = 1;

        private static SyncPacket packet(FriendlyByteBuf buf)
        {
            CompoundTag tag = buf.readNbt();
            if (tag != null)
            {
                DuskveilData state = new DuskveilData();
                state.read(tag);
                return new SyncPacket(state);
            }
            else return new SyncPacket(null);
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event)
        {
            CHANNEL.registerMessage(sequence, SyncPacket.class, SyncPacket::write, NetWork::packet, SyncPacket::apply);
            sequence++;
        }
    }
}
