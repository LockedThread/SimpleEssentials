package rip.simpleness.simpleessentials;


import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.argument.ArgumentParser;

import java.util.Optional;
import java.util.function.Function;

public class ParseRegistrar {

    public static <T> ArgumentParser<T> buildParser(String name, Function<String, Optional<T>> stringOptionalFunction) {
        return ArgumentParser.of(stringOptionalFunction, s -> new CommandInterruptException(SimpleEssentials.getInstance().getServerPrefix() + "&cUnable to parse " + s + " as a " + name));
    }
}
