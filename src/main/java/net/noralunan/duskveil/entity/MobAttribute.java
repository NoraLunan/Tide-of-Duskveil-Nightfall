package net.noralunan.duskveil.entity;

import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraftforge.common.ForgeMod;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.EntityConfig;

import java.util.UUID;

public class MobAttribute
{
    private final Mob mob;
    private final EntityConfig config;

    MobAttribute(Mob mob)
    {
        this.mob = mob;
        this.config = DuskveilConfig.SERVER.entity;
    }

    void modify()
    {
        apply(Type.MAX_HEALTH, config.health_mul.get());
        AttributeInstance healthAttribute = mob.getAttribute(Type.MAX_HEALTH.attribute);
        if (healthAttribute != null)
        {
            mob.setHealth(mob.getMaxHealth());
        }
        apply(Type.ARMOR, config.armor_mul.get());
        apply(Type.ARMOR_TOUGHNESS, config.toughness_mul.get());
        apply(Type.ATTACK_DAMAGE, config.attack_mul.get());

        double speedMul = config.speed_mul.get();
        if (mob instanceof FlyingMob)
            apply(Type.FLYING_SPEED, speedMul);
        else if (mob instanceof WaterAnimal)
            apply(Type.SWIM_SPEED, speedMul);
        else
            apply(Type.MOVEMENT_SPEED, speedMul);
    }

    void restore()
    {
        for (Type type : Type.values())
        {
            if (type.attribute == null) continue;
            AttributeInstance attribute = mob.getAttribute(type.attribute);
            if (attribute == null) continue;
            attribute.removeModifier(type.uuid);
        }
    }

    private void apply(Type type, double multiplier)
    {
        if (type.attribute == null) return;
        AttributeInstance attribute = mob.getAttribute(type.attribute);
        if (attribute == null) return;

        attribute.removeModifier(type.uuid);
        AttributeModifier modifier =
                new AttributeModifier(type.uuid, type.name, multiplier - 1d, Operation.MULTIPLY_TOTAL);
        attribute.addPermanentModifier(modifier);
    }

    private enum Type
    {
        MAX_HEALTH(Attributes.MAX_HEALTH, UUIDS.MAX_HEALTH, Name.MAX_HEALTH),
        ARMOR(Attributes.ARMOR, UUIDS.ARMOR, Name.ARMOR),
        ARMOR_TOUGHNESS(Attributes.ARMOR_TOUGHNESS, UUIDS.ARMOR_TOUGHNESS, Name.ARMOR_TOUGHNESS),
        ATTACK_DAMAGE(Attributes.ATTACK_DAMAGE, UUIDS.ATTACK_DAMAGE, Name.ATTACK_DAMAGE),
        MOVEMENT_SPEED(Attributes.MOVEMENT_SPEED, UUIDS.MOVEMENT_SPEED, Name.SPEED),
        FLYING_SPEED(Attributes.FLYING_SPEED, UUIDS.FLYING_SPEED, Name.SPEED),
        SWIM_SPEED(ForgeMod.SWIM_SPEED.get(), UUIDS.SWIM_SPEED, Name.SPEED);

        private final Attribute attribute;
        private final UUID uuid;
        private final String name;

        Type(Attribute attribute, UUID uuid, String name)
        {
            this.attribute = attribute;
            this.uuid = uuid;
            this.name = name;
        }
    }

    private static class UUIDS
    {
        private static final UUID MAX_HEALTH = UUID.fromString("8efdbb46-4325-4ad0-8305-18b39821e7be");
        private static final UUID ARMOR = UUID.fromString("8f8d45cb-57ac-47cf-b299-8ccf009fd00b");
        private static final UUID ARMOR_TOUGHNESS = UUID.fromString("3a0e5412-afa1-4449-b4ca-2fe90b7a125c");
        private static final UUID ATTACK_DAMAGE = UUID.fromString("a114602c-8350-49a8-8412-c21452e066fc");
        private static final UUID MOVEMENT_SPEED = UUID.fromString("3c491b7f-84f7-4024-a24a-025c74336f3b");
        private static final UUID FLYING_SPEED = UUID.fromString("9026fa64-63ff-4794-b2b7-d0fdb8f1df85");
        private static final UUID SWIM_SPEED = UUID.fromString("63fe1bb5-079f-4fd1-b2db-6db3d5472d51");
    }

    private static class Name
    {
        private static final String MAX_HEALTH = Duskveil.MOD_ID + ".max_health";
        private static final String ARMOR = Duskveil.MOD_ID + ".armor";
        private static final String ARMOR_TOUGHNESS = Duskveil.MOD_ID + ".toughness";
        private static final String ATTACK_DAMAGE = Duskveil.MOD_ID + ".melee_damage";
        private static final String SPEED = Duskveil.MOD_ID + ".speed";
    }
}
