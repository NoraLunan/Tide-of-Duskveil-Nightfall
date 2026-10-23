package net.noralunan.duskveil.command.argument;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.noralunan.duskveil.Duskveil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class AlgorithmArgument implements ArgumentType<AlgorithmArgument.Algorithm> {
    public static final String SURFACE = Algorithm.SURFACE.name.toLowerCase();
    public static final String DYNAMIC = Algorithm.DYNAMIC.name.toLowerCase();
    private static final Collection<String> EXAMPLES = Lists.newArrayList(SURFACE, DYNAMIC);

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT =
            DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Duskveil.MOD_ID);
    private static final RegistryObject<ArgumentTypeInfo<AlgorithmArgument, ?>> ALGORITHM =
            ARGUMENT.register("algorithm", () -> ArgumentTypeInfos.registerByClass(AlgorithmArgument.class, AlgorithmInfo.INSTANCE));
    public static void register(IEventBus mod) {
        ARGUMENT.register(mod);
    }

    private AlgorithmArgument() {}

    public static AlgorithmArgument type() {
        return new AlgorithmArgument();
    }

    @Override
    public Algorithm parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String input = reader.readUnquotedString();

        if (input.equals(SURFACE)) return Algorithm.SURFACE;
        else if (input.equals(DYNAMIC)) return Algorithm.DYNAMIC;
        else reader.setCursor(cursor);

        var exception = CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument();
        throw new CommandSyntaxException(
                exception,
                exception.create().getRawMessage(),
                input,
                cursor
        );
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();

        if (SURFACE.startsWith(remaining))
            builder.suggest(SURFACE);

        if (DYNAMIC.startsWith(remaining))
            builder.suggest(DYNAMIC);

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static Algorithm getAlgorithm(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Algorithm.class);
    }

    private static class AlgorithmInfo implements ArgumentTypeInfo<AlgorithmArgument, InfoTemplate> {
        private static final AlgorithmInfo INSTANCE = new AlgorithmInfo();

        @Override
        public void serializeToNetwork(@NotNull InfoTemplate pTemplate, @NotNull FriendlyByteBuf pBuffer) {
        }

        @Override
        public @NotNull InfoTemplate deserializeFromNetwork(@NotNull FriendlyByteBuf buffer) {
            return InfoTemplate.INSTANCE;
        }

        @Override
        public @NotNull InfoTemplate unpack(@NotNull AlgorithmArgument pArgument) {
            return InfoTemplate.INSTANCE;
        }

        @Override
        public void serializeToJson(@NotNull InfoTemplate template, @NotNull JsonObject json) {
        }
    }

    private static class InfoTemplate implements ArgumentTypeInfo.Template<AlgorithmArgument> {
        private static final InfoTemplate INSTANCE = new InfoTemplate();

        @Override
        public @NotNull AlgorithmArgument instantiate(@NotNull CommandBuildContext pContext) {
            return AlgorithmArgument.type();
        }

        @Override
        public @NotNull ArgumentTypeInfo<AlgorithmArgument, ?> type() {
            return AlgorithmInfo.INSTANCE;
        }
    }

    public enum Algorithm {
        SURFACE(true, "Surface"),
        DYNAMIC(false, "Dynamic");

        public final boolean value;
        public final String name;
        Algorithm(boolean surface, String name) {
            this.value = surface;
            this.name = name;
        }
    }
}
