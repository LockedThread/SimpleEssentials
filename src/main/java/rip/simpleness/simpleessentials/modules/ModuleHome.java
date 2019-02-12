package rip.simpleness.simpleessentials.modules;

import com.google.common.base.Joiner;
import me.lucko.helper.Commands;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import javax.annotation.Nonnull;

public class ModuleHome implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    final Account account = INSTANCE.getAccount(commandContext.sender());
                    if (commandContext.args().size() == 0) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eHomes: " + getStringHomes(account));
                    } else if (commandContext.args().size() == 1) {
                        String homeName = commandContext.arg(0).parseOrFail(String.class);
                        if (account.getHomes().containsKey(homeName)) {
                            teleportHome(account, commandContext.sender(), homeName);
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cUnable to find home " + homeName);
                        }
                    } else if (commandContext.args().size() == 2) {
                        Account targetAccount = commandContext.arg(0).parseOrFail(Account.class);
                        String homeName = commandContext.arg(1).parseOrFail(String.class);
                        if (targetAccount.getHomes().containsKey(homeName)) {
                            teleportHome(targetAccount, commandContext.sender(), homeName);
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eUnable to find home " + homeName);
                        }
                    } else
                        commandContext.reply(commandContext.sender().hasPermission("simpleness.home.others") ? "&e/home [homeame]\n&e/home [player] [homename]" : "&e/home [homename]");
                }).registerAndBind(terminableConsumer, "home");

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        final Account account = INSTANCE.getAccount(commandContext.sender());
                        if (canSetAnotherHome(commandContext.sender(), account.getHomes().size())) {
                            String homeName = commandContext.rawArg(0);
                            account.getHomes().put(homeName, Point.of(commandContext.sender().getLocation()));
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eYou've set your \"" + homeName + "\" home");
                        }
                    } else {
                        commandContext.reply("&e/sethome [homename]");
                    }
                }).registerAndBind(terminableConsumer, "sethome");

        Commands.create()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        if (commandContext.sender() instanceof Player) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eHomes: " + getStringHomes(INSTANCE.getAccount((Player) commandContext.sender())));
                        } else {
                            commandContext.reply("&cYou can't run this command as Console.");
                        }
                    } else if (commandContext.args().size() == 1) {
                        if (commandContext.sender().hasPermission("simpleness.homes.others")) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eHomes: " + getStringHomes(commandContext.arg(0).parseOrFail(Account.class)));
                        } else {
                            commandContext.reply("&e/homes");
                        }
                    } else {
                        commandContext.reply(commandContext.sender().hasPermission("simpleness.homes.others") ? "&e/homes [player]" : "&e/homes");
                    }

                }).registerAndBind(terminableConsumer, "homes");
    }

    private boolean canSetAnotherHome(Player player, int currentSize) {
        if (player.hasPermission("simpleness.homes.unlimited")) {
            return true;
        }
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().startsWith("simpleness.homes") && permission.getPermission().split(".").length == 3) {
                if (StringUtils.isNumeric(permission.getPermission().split(".")[2])) {
                    return Integer.parseInt(permission.getPermission().split(".")[2]) > currentSize;
                }
            }
        }
        return false;
    }

    private String getStringHomes(Account account) {
        return Joiner.on(", ").skipNulls().join(account.getHomes().keySet());
    }

    private void teleportHome(Account account, Player player, String home) {
        player.teleport(account.getHomes().get(home.toLowerCase()).toLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
