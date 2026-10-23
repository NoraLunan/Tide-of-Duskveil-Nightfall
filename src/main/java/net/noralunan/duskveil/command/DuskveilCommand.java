package net.noralunan.duskveil.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.duskveil.command.argument.AlgorithmArgument;
import net.noralunan.duskveil.config.CommandConfig;
import net.noralunan.duskveil.config.DuskveilConfig;

public class DuskveilCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Duskveil.MOD_ID);
        CommandConfig config = DuskveilConfig.COMMON.command;
        if (config.marker.get()) {
            builder.then(Commands.literal("marker")
                    .requires(source -> source.hasPermission(2))
                    .executes(Marker::execute)
            );
        }
        if (config.start.get()) {
            builder.then(Commands.literal("start")
                    .requires(source -> source.hasPermission(2))
                    .executes(Start::execute)
            );
        }
        if (config.stop.get()) {
            builder.then(Commands.literal("stop")
                    .requires(source -> source.hasPermission(2))
                    .executes(Stop::execute)
            );
        }
        if (config.count.get()) {
            builder.then(Commands.literal("count")
                    .then(Commands.literal("set")
                            .requires(source -> source.hasPermission(3))
                            .then(Commands.argument(Count.VALUE, IntegerArgumentType.integer(0))
                                    .executes(Count::set)
                            )
                    ).then(Commands.literal("add")
                            .requires(source -> source.hasPermission(3))
                            .then(Commands.argument(Count.VALUE, IntegerArgumentType.integer(1))
                                    .executes(Count::add)
                            )
                    ).then(Commands.literal("decrease")
                            .requires(source -> source.hasPermission(3))
                            .then(Commands.argument(Count.VALUE, IntegerArgumentType.integer(1))
                                    .executes(Count::decrease)
                            )
                    ).then(Commands.literal("query")
                            .requires(source -> source.hasPermission(0))
                            .executes(Count::query)
                    )
            );
        }
        if (config.clear.get()) {
            builder.then(Commands.literal("clear")
                    .requires(source -> source.hasPermission(2))
                    .executes(Clear::execute)
            );
        }
        if (config.gaze.get()) {
            builder.then(Commands.literal("gaze")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(Gaze::execute)
                    )
            );
        }
        if (config.reload.get()) {
            builder.then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(1))
                    .then(Commands.literal("server")
                            .executes(Reload::server)
                    ).then(Commands.literal("moblist")
                            .executes(Reload::moblist)
                    )
            );
        }
        if (config.spawn.get()) {
            builder.then(Commands.literal("spawn")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("at")
                            .then(Commands.literal(AlgorithmArgument.SURFACE)
                                    .then(Commands.argument(Spawn.POS, ColumnPosArgument.columnPos())
                                            .executes(Spawn::spawnWithSurfaceAtSpecified)))
                            .then(Commands.literal(AlgorithmArgument.DYNAMIC)
                                    .then(Commands.argument(Spawn.POS, BlockPosArgument.blockPos())
                                            .executes(Spawn::spawnWithDynamicAtSpecified))))
                    .then(Commands.literal("for")
                            .then(Commands.argument(Spawn.TARGET, EntityArgument.player())
                                    .then(Commands.argument(Spawn.TYPE, AlgorithmArgument.type())
                                            .executes(Spawn::spawnWithAlgorithmForPlayer))
                                    .executes(Spawn::spawnForPlayer))
                            .then(Commands.argument(Spawn.TYPE, AlgorithmArgument.type())
                                    .then(Commands.argument(Spawn.TARGET, EntityArgument.player())
                                            .executes(Spawn::spawnWithAlgorithmForPlayer))
                                    .executes(Spawn::spawnWithAlgorithmForSource)
                            )
                    )
            );
        }
        event.getDispatcher().register(builder);
    }

    @SubscribeEvent
    public static void client(RegisterClientCommandsEvent event) {
        if (DuskveilConfig.COMMON.command.reload.get()) {
            event.getDispatcher().register(Commands.literal("duskveil:client")
                    .requires(source -> source.hasPermission(1))
                    .then(Commands.literal("reload")
                            .executes(Reload::client)
                    )
            );
        }
    }
}
