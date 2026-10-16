package net.noralunan.duskveil.core;

import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.event.EventDetector;
import net.noralunan.duskveil.event.EventManager;
import net.noralunan.library.common.NoraLevel;
import net.noralunan.library.common.NoraLogger;

public interface TideLevel extends NoraLevel {
    default boolean isTideNight() {
        return getSavedData().tidenight;
    }

    default void setTideNight(boolean value) {
        getSavedData().tidenight = value;
    }

    default boolean isTrigger() {
        return getTrigger() != TriggerType.NONE;
    }

    default TriggerType getTrigger() {
        return getSavedData().trigger;
    }

    /**
     * Developers should not call this method to set TriggerType
     * If you want to change the TriggerType, please use the setStatus method from {@link EventDetector}
     */
    default void setTrigger(TriggerType type) {
        Class<?> clazz = NoraLogger.STACK_WALKER.getCallerClass();
        if (clazz != EventDetector.class && clazz != EventManager.class)
            throw new UnsupportedOperationException("Please use the setStatus method from "+ EventDetector.class.getName());
        getSavedData().trigger = type;
    }

    default int getEventCount() {
        return getSavedData().count;
    }

    default void setEventCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Target value < 0 :" + value);
        }
        getSavedData().count = value;
    }

    default void addEventCount() {
        addEventCount(1);
    }

    default void addEventCount(int value) {
        if (getEventCount() + value < 0) {
            throw new IllegalArgumentException("Target value < 0 :");
        }
        getSavedData().count += value;
    }

    default boolean isInspected() {
        return getSavedData().inspected;
    }

    default void setInspected(boolean value) {
        getSavedData().inspected = value;
    }

    default int getNextFixDay() {
        return getSavedData().nextfix;
    }

    default void setNextFixDay(int value) {
        getSavedData().nextfix = value;
    }

    default int getLastTide() {
        return getSavedData().last;
    }

    default void setLastTide(int value) {
        getSavedData().last = value;
    }

    default int getWaveCount() {
        return getSavedData().wave;
    }

    default void setWaveCount(int value) {
        getSavedData().wave = value;
    }

    default void addWaveCount(int value) {
        getSavedData().wave += value;
    }

    default int getNextWave() {
        return getSavedData().nextwave;
    }

    default void setNextWave(int value) {
        getSavedData().nextwave = value;
    }

    default void addNextWave(int value) {
        getSavedData().nextwave += value;
    }

    default void syncData() {
        getSavedData().sync();
    }

    private DuskveilData getSavedData() {
        return DuskveilData.get(server());
    }

    default int getTideNightRemain() {
        return DuskveilConfig.SERVER.event.end_time.get() - dayTime();
    }
}
