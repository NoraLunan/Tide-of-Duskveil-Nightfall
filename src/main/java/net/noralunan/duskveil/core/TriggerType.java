package net.noralunan.duskveil.core;

public enum TriggerType
{
    NONE(0),
    FULL_MOON(1),
    FIX_INTERVAL(2),
    CHANCE(3),
    COMMAND(4);

    final byte aByte;

    TriggerType(int aByte)
    {
        this.aByte = (byte) aByte;
    }

    public static TriggerType from(byte value)
    {
        for (TriggerType type : values())
        {
            if (type.aByte == value) return type;
        }
        return TriggerType.NONE;
    }
}
