package net.noralunan.duskveil.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.noralunan.duskveil.core.TideLevel;
import net.noralunan.duskveil.core.TriggerType;

public class TideNightLifecycle extends Event
{
    private final TideLevel tide;

    private TideNightLifecycle(TideLevel tide)
    {
        this.tide = tide;
    }

    public TideLevel tideLevel()
    {
        return this.tide;
    }

    @Cancelable
    public static class StatusChangeEvent extends TideNightLifecycle
    {
        private TriggerType type;
        protected StatusChangeEvent(TideLevel tide, TriggerType type)
        {
            super(tide);
            this.type = type;
        }

        public TriggerType getType()
        {
            return this.type;
        }

        public void setType(TriggerType type)
        {
            this.type = type;
        }
    }

    @Cancelable
    public static class StartingEvent extends TideNightLifecycle
    {
        protected StartingEvent(TideLevel tide)
        {
            super(tide);
        }
    }

    public static class StartedEvent extends TideNightLifecycle
    {
        private final String message;
        private final MessageType type;

        protected StartedEvent(TideLevel tide, String message, MessageType type)
        {
            super(tide);
            this.message = message;
            this.type = type;
        }

        public int getEventCount()
        {
            return super.tide.getEventCount();
        }

        public int getLastTide()
        {
            return super.tide.getLastTide();
        }

        public String getMessage()
        {
            return this.message;
        }

        public MessageType messageType()
        {
            return this.type;
        }
    }

    public static class StoppingEvent extends TideNightLifecycle
    {
        protected StoppingEvent(TideLevel tide)
        {
            super(tide);
        }
    }

    public static class StoppedEvent extends TideNightLifecycle
    {
        public final String message;
        private final MessageType type;

        protected StoppedEvent(TideLevel tide, String message, MessageType type)
        {
            super(tide);
            this.message = message;
            this.type = type;
        }

        public String getMessage()
        {
            return this.message;
        }

        public MessageType messageType()
        {
            return this.type;
        }
    }
}
