package rip.simpleness.simpleessentials.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.argument.ArgumentParser;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

public class ModuleAccount implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        /*if (!Players.all().isEmpty()) {
            for (Player player : Players.all()) {
                Account account = GsonProvider.prettyPrinting().fromJson(INSTANCE.getRedis().get(player.getUniqueId().toString()), Account.class);
                INSTANCE.getAccountData().put(player.getUniqueId(), account);
            }
        }*/

        Commands.parserRegistry().register(Account.class, ArgumentParser.of(s -> {
            if (Bukkit.getOfflinePlayer(s).isOnline()) {
                return Optional.of(INSTANCE.getAccount(s));
            }
            return Optional.of(INSTANCE.getOfflineAccount(Bukkit.getOfflinePlayer(s).getUniqueId()));
        }, s -> new CommandInterruptException("&cUnable to find account \"" + s + "\"")));

        Events.subscribe(PlayerJoinEvent.class)
                .handler(event -> event.setJoinMessage(null))
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerLoginEvent.class)
                .filter(EventFilters.ignoreDisallowedLogin())
                .handler(event -> {
                    Player player = event.getPlayer();
                    if (INSTANCE.getRedis().exists(player.getUniqueId().toString()) == 0) {
                        System.out.println("created Account and put it in memory");
                        INSTANCE.createAccount(player.getUniqueId(), player.getName());
                        INSTANCE.setPlayerJoins(INSTANCE.getPlayerJoins() + 1);
                        Bukkit.broadcastMessage(INSTANCE.getFirstJoinMessage()
                                .replace("{player}", player.getName())
                                .replace("{join-times}", String.valueOf(INSTANCE.getPlayerJoins())));
                        Schedulers.sync().runLater(() -> player.teleport(INSTANCE.getModuleAdministration().getSpawnPoint(), PlayerTeleportEvent.TeleportCause.PLUGIN), 2L);
                    } else {
                        try {
                            INSTANCE.getAccountData().put(player.getUniqueId(), SimpleEssentials.json.beanFrom(Account.class, INSTANCE.getRedis().get(player.getUniqueId().toString())));
                            System.out.println(player.getUniqueId().toString() + " put in memory from db");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> {
                    event.setQuitMessage(null);
                    Player player = event.getPlayer();
                    try {
                        INSTANCE.getRedis().set(player.getUniqueId().toString(), SimpleEssentials.json.asString(INSTANCE.getOnlineAccount(player)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    INSTANCE.getAccountData().remove(player.getUniqueId());
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerKickEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .handler(event -> {
                    Player player = event.getPlayer();
                    try {
                        INSTANCE.getRedis().set(player.getUniqueId().toString(), SimpleEssentials.json.asString(INSTANCE.getAccount(player)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    INSTANCE.getAccountData().remove(player.getUniqueId());
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerDeathEvent.class).handler(event -> {
            event.setDeathMessage(null);
            Schedulers.sync().runLater(() -> event.getEntity().spigot().respawn(), 3L);
        }).bindWith(terminableConsumer);

        Events.subscribe(PlayerRespawnEvent.class).handler(event -> event.setRespawnLocation(INSTANCE.getModuleAdministration().getSpawnPoint())).bindWith(terminableConsumer);
    }
}
