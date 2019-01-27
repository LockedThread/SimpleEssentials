package rip.simpleness.simpleessentials.modules;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import me.lucko.helper.Commands;
import me.lucko.helper.serialize.GsonStorageHandler;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.map.SimplenessMap;
import rip.simpleness.simpleessentials.objs.Warp;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleWarp implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    private SimplenessMap<String, Warp> warpData;
    private GsonStorageHandler<SimplenessMap<String, Warp>> warpDataGsonStorage;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.warpDataGsonStorage = new GsonStorageHandler<>("warpdata", ".json", INSTANCE.getDataFolder(), new TypeToken<SimplenessMap<String, Warp>>() {
        });
        this.warpData = warpDataGsonStorage.load().orElse(new SimplenessMap<>());
        terminableConsumer.bind(() -> warpDataGsonStorage.save(warpData));

        Commands.parserRegistry().register(Warp.class, s -> Optional.of(getWarp(s)));

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.warps")
                .handler(commandContext -> {
                    final String warps = getWarps(commandContext.sender());
                    if (warpData.isEmpty()) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cThere's no warps set! /setwarp [name]");
                    } else if (warps.isEmpty()) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cYou don't have permission to any of the current warps!");
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eWarps: " + warps);
                    }
                }).registerAndBind(terminableConsumer, "warps");

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        Warp warp = commandContext.arg(0).parseOrFail(Warp.class);
                        commandContext.reply(!warp.hasPermission(commandContext.sender()) ?
                                INSTANCE.getServerPrefix() + "&cYou don't have permission to warp to \"" + commandContext.rawArg(0) + "\"" :
                                teleport(commandContext.sender(), warp) ?
                                        INSTANCE.getServerPrefix() + "&eYou have been teleported to \"" + commandContext.rawArg(0) + "\"" :
                                        INSTANCE.getServerPrefix() + "&cUnable to teleport to \"" + commandContext.rawArg(0) + "\"");
                    } else {
                        commandContext.reply("&e/warp [name]");
                    }
                }).registerAndBind(terminableConsumer, "warp");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.setwarp")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        Warp warp = setWarp(commandContext.rawArg(0), Point.of(commandContext.sender().getLocation()));
                        Position position = warp.getPoint().getPosition();
                        double x = position.getX(), y = position.getY(), z = position.getZ();
                        String world = position.getWorld();
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cSuccessfully set warp " + commandContext.rawArg(0) + " in world " + world + " at " + x + ", " + y + ", " + z);
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/setwarp [name]");
                    }
                }).registerAndBind(terminableConsumer, "setwarp");

        Commands.create()
                .assertPermission("simpleness.delwarp")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        commandContext.arg(0).parseOrFail(Warp.class);
                        deleteWarp(commandContext.rawArg(0));
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have &asuccessfully successfully &edelete the " + commandContext.rawArg(0) + " warp");
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/delwarp [name]");
                    }
                }).registerAndBind(terminableConsumer, "delwarp", "deletewarp");
    }

    public void deleteWarp(String warpName) {
        warpData.remove(warpName);
    }

    public String getWarps(Player player) {
        return Joiner.on(", ")
                .skipNulls()
                .join(warpData.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().hasPermission(player))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList()));
    }

    public Warp setWarp(String warpName, Point point, String permission) {
        return warpData.putAndReturn(warpName.toLowerCase(), new Warp(point, permission));
    }

    public Warp setWarp(String warpName, Point point) {
        return setWarp(warpName, point, "");
    }

    public Warp getWarp(String key) {
        return warpData.get(key.toLowerCase());
    }

    public boolean isWarp(String key) {
        return warpData.containsKey(key.toLowerCase());
    }

    public boolean teleport(Player player, Warp warp) {
        if (warp == null) return false;
        return player.teleport(warp.getPoint().toLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
