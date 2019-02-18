package rip.simpleness.simpleessentials.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.argument.ArgumentParser;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ModuleAccount implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        if (!Players.all().isEmpty()) {
            for (Player player : Players.all()) {
                Account account = GsonProvider.prettyPrinting().fromJson(INSTANCE.getJedis().get(player.getUniqueId().toString()), Account.class);
                INSTANCE.getAccountData().put(player.getUniqueId(), account);
            }
        }

        Commands.parserRegistry().register(Account.class, ArgumentParser.of(s -> Optional.of(INSTANCE.getAccount(s)), s -> new CommandInterruptException("&cUnable to find account \"" + s + "\"")));

        Events.subscribe(PlayerJoinEvent.class)
                .handler(event -> {
                    event.setJoinMessage(null);
                    Player player = event.getPlayer();
                    if (INSTANCE.getJedis().exists(player.getUniqueId().toString())) {
                        Account account = GsonProvider.standard().fromJson(INSTANCE.getJedis().get(player.getUniqueId().toString()), Account.class);
                        INSTANCE.getAccountData().put(player.getUniqueId(), account);
                    } else {
                        INSTANCE.createAccount(player.getUniqueId(), player.getName());
                        INSTANCE.setPlayerJoins(INSTANCE.getPlayerJoins() + 1);
                        Bukkit.broadcastMessage(INSTANCE.getFirstJoinMessage()
                                .replace("{player}", player.getName())
                                .replace("{join-times}", String.valueOf(INSTANCE.getPlayerJoins())));
                        Bukkit.getScheduler().runTaskLater(INSTANCE, () -> player.teleport(INSTANCE.getModuleAdministration().getSpawnPoint(), PlayerTeleportEvent.TeleportCause.PLUGIN), 2L);
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> {
                    event.setQuitMessage(null);
                    Player player = event.getPlayer();
                    INSTANCE.getJedis().set(player.getUniqueId().toString(), GsonProvider.standard().toJson(INSTANCE.getAccount(player)));
                    INSTANCE.getAccountData().remove(player.getUniqueId());
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerKickEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .handler(event -> {
                    Player player = event.getPlayer();
                    INSTANCE.getJedis().set(player.getUniqueId().toString(), GsonProvider.standard().toJson(INSTANCE.getAccount(player)));
                    INSTANCE.getAccountData().remove(player.getUniqueId());
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerDeathEvent.class).handler(event -> {
            event.setDeathMessage(null);
            Schedulers.sync().runLater(() -> event.getEntity().spigot().respawn(), 3L);
        }).bindWith(terminableConsumer);

        Events.subscribe(PlayerRespawnEvent.class).handler(event -> event.setRespawnLocation(INSTANCE.getModuleAdministration().getSpawnPoint())).bindWith(terminableConsumer);

        Events.subscribe(PluginDisableEvent.class)
                .filter(event -> event.getPlugin().getName().equalsIgnoreCase(INSTANCE.getName()))
                .handler(event -> INSTANCE.getAccountData().forEach((key, value) -> INSTANCE.getJedis().set(key.toString(), GsonProvider.standard().toJson(value)))).bindWith(terminableConsumer);
    }
}
