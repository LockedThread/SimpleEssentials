package rip.simpleness.simpleessentials.modules;

import me.lucko.helper.Events;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import javax.annotation.Nonnull;

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

        Events.subscribe(PlayerJoinEvent.class)
                .handler(event -> {
                    Player player = event.getPlayer();
                    if (player.hasPlayedBefore()) {
                        Account account = GsonProvider.prettyPrinting().fromJson(INSTANCE.getJedis().get(player.getUniqueId().toString()), Account.class);
                        INSTANCE.getAccountData().put(player.getUniqueId(), account);
                    } else {
                        INSTANCE.createAccount(player.getUniqueId(), player.getName());
                        INSTANCE.setPlayerJoins(INSTANCE.getPlayerJoins() + 1);
                        Bukkit.broadcastMessage(INSTANCE.getFirstJoinMessage()
                                .replace("{player}", player.getName())
                                .replace("{join-times}", String.valueOf(INSTANCE.getPlayerJoins())));
                        if (INSTANCE.getModuleAdministration().getSpawnPoint() != null) {
                            player.teleport(INSTANCE.getModuleAdministration().getSpawnPoint().toLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> {
                    Player player = event.getPlayer();
                    Account account = INSTANCE.getAccount(player);
                    INSTANCE.getJedis().set(player.getUniqueId().toString(), GsonProvider.prettyPrinting().toJson(account));
                    INSTANCE.getAccountData().remove(player.getUniqueId());
                    System.out.println(event.getEventName() + " was called");
                }).bindWith(terminableConsumer);
    }
}
