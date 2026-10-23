package net.noralunan.duskveil.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.noralunan.duskveil.command.argument.AlgorithmArgument;
import net.noralunan.duskveil.command.argument.AlgorithmArgument.Algorithm;
import net.noralunan.duskveil.spawner.SpawnExecutor;

class Spawn {
    static final String POS = "pos";
    static final String TARGET = "target";
    static final String TYPE = "type";
    private static final MobSpawnType MOB_SPAWN_TYPE = MobSpawnType.COMMAND;

    static int spawnWithAlgorithmForPlayer(CommandContext<CommandSourceStack> context) {
        Context command = new Context(context);

        if (command.targetPlayer == null) {
            command.source.sendFailure(Component.translatable("duskveil.command.fail.player_exception"));
        }
        else if (command.algorithm == null) {
            command.source.sendFailure(Component.translatable("command.failed"));
        }
        else {
            int spawnedCount = SpawnExecutor.run(command.targetPlayer, command.algorithm.value, MOB_SPAWN_TYPE);

            Component message = Component.translatable("duskveil.command.spawn.success", spawnedCount);
            command.source.sendSuccess(() -> message, true);
            return spawnedCount;
        }
        return -1;
    }

    static int spawnWithSurfaceAtSpecified(CommandContext<CommandSourceStack> context) {
        Context command = new Context(context);

        ColumnPos pos = ColumnPosArgument.getColumnPos(context, POS);
        int spawnedCount = SpawnExecutor.run(command.level, pos, MOB_SPAWN_TYPE);

        Component message = Component.translatable("duskveil.command.spawn.success", spawnedCount);

        command.source.sendSuccess(() -> message, true);

        return spawnedCount;
    }

    static int spawnWithDynamicAtSpecified(CommandContext<CommandSourceStack> context) {
        Context command = new Context(context);

        BlockPos pos = BlockPosArgument.getBlockPos(context, POS);
        Vec3i vec3i = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
        int spawnedCount = SpawnExecutor.run(command.level, vec3i, false, MOB_SPAWN_TYPE);

        Component message = Component.translatable("duskveil.command.spawn.success", spawnedCount);

        command.source.sendSuccess(() -> message, true);

        return spawnedCount;
    }

    static int spawnWithAlgorithmForSource(CommandContext<CommandSourceStack> context) {
        Context command = new Context(context);

        Entity sourceEntity = command.source.getEntity();

        if (sourceEntity instanceof ServerPlayer player && command.algorithm != null) {
            int result = SpawnExecutor.run(player, MOB_SPAWN_TYPE);

            Component message = Component.translatable("duskveil.command.spawn.success", result);
            command.source.sendSuccess(() -> message, true);
            return result;
        }
        else {
            command.source.sendFailure(Component.translatable("command.unknown.argument"));
            return -1;
        }
    }

    static int spawnForPlayer(CommandContext<CommandSourceStack> context) {
        Context command = new Context(context);

        if (command.targetPlayer != null) {
            int spawnedCount = SpawnExecutor.run(command.targetPlayer, MOB_SPAWN_TYPE);

            Component message =
                    Component.translatable("duskveil.command.spawn.success", spawnedCount);
            command.source.sendSuccess(() -> message, true);
            return spawnedCount;
        }
        else {
            command.source.sendFailure(Component.translatable("duskveil.command.fail.player_exception"));
            return -1;
        }
    }

    private static class Context {
        private final CommandSourceStack source;
        private final ServerLevel level;
        private final Algorithm algorithm;
        private final ServerPlayer targetPlayer;

        private Context(CommandContext<CommandSourceStack> context) {
            this.source = context.getSource();
            this.level = source.getLevel();

            Algorithm algorithm;
            try {
                algorithm = AlgorithmArgument.getAlgorithm(context, TYPE);
            }
            catch (IllegalArgumentException exception) {
                algorithm = null;
            }
            this.algorithm = algorithm;

            ServerPlayer targetPlayer;
            try {
                targetPlayer = EntityArgument.getPlayer(context, TARGET);
            }
            catch (Exception exception) {
                targetPlayer = null;
            }
            this.targetPlayer = targetPlayer;
        }
    }
}
