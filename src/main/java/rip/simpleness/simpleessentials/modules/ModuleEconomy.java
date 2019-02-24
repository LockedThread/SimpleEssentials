package rip.simpleness.simpleessentials.modules;

import com.google.common.collect.Lists;
import me.lucko.helper.Commands;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import rip.simpleness.simpleessentials.MathUtils;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ModuleEconomy implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    private HashMap<Integer, ArrayList<Account>> baltopPages;
    private double serverTotal;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        INSTANCE.getServer().getScheduler().runTaskTimer(INSTANCE, () -> {
            try {
                Bukkit.broadcastMessage(Text.colorize("&7Recalculating baltop money...."));
                baltopPages = calculateBalTop().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, 0, 6000);
        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.reply("&eBalance: &f$" + new DecimalFormat("#0.00").format(INSTANCE.getAccount(commandContext.sender()).getMoney()));
                    } else if (commandContext.args().size() == 1) {
                        if (commandContext.sender().hasPermission("simpleness.balance.others")) {
                            commandContext.reply("&eBalance: &f$" + new DecimalFormat("#0.00").format(INSTANCE.getOfflineAccount(commandContext.arg(0).parseOrFail(OfflinePlayer.class).getUniqueId()).getMoney()));
                        } else {
                            commandContext.reply("&cYou don't have permission to check other's balances");
                        }
                    } else {
                        if (commandContext.sender().hasPermission("simpleness.balance.others")) {
                            commandContext.reply("&e/bal [player]");
                        }
                        commandContext.reply("&e/bal");
                    }
                })
                .registerAndBind(terminableConsumer, "balance", "bal");

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 2) {
                        double amount = MathUtils.round(commandContext.arg(1).parseOrFail(Double.class));
                        if (amount <= 0.0) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't pay someone a negative amount of money!");
                        } else {
                            Player target = commandContext.arg(0).parseOrFail(Player.class);
                            if (target.getName().equals(commandContext.sender().getName())) {
                                commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't pay yourself money.");
                            } else {
                                EconomyResponse economyResponse = INSTANCE.getProvider().withdrawPlayer(commandContext.sender(), amount);
                                if (economyResponse.transactionSuccess()) {
                                    commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have paid " + target.getName() + " $" + amount);
                                    INSTANCE.getProvider().depositPlayer(target, amount);
                                    target.sendMessage(Text.colorize(INSTANCE.getServerPrefix() + "&aYou have received " + amount + " from " + commandContext.sender().getName()));
                                } else {
                                    commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't pay " + target.getName() + " because you don't have enough money!");
                                }
                            }
                        }
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/pay [player] [amount]");
                    }
                }).registerAndBind(terminableConsumer, "pay");

        Commands.create()
                .assertPermission("simpleness.admin")
                .handler(commandContext -> {
                    if (commandContext.args().size() >= 2) {
                        String subCommand = commandContext.arg(0).parseOrFail(String.class);
                        Optional<Player> optionalPlayer = Players.get(commandContext.arg(1).parseOrFail(String.class));
                        Account account;
                        if (optionalPlayer.isPresent()) {
                            account = optionalPlayer.get().isOnline() ? INSTANCE.getAccount(optionalPlayer.get().getUniqueId()) : INSTANCE.getOfflineAccount(optionalPlayer.get().getUniqueId());
                            if (account == null) {
                                commandContext.reply("&cUnable to find account " + commandContext.arg(1).parseOrFail(String.class));
                                return;
                            }
                        } else {
                            commandContext.reply("&cUnable to find account " + commandContext.arg(1).parseOrFail(String.class));
                            return;
                        }
                        Player target = optionalPlayer.get();
                        if (commandContext.args().size() == 3) {
                            double amount = MathUtils.round(commandContext.arg(2).parseOrFail(Double.class));
                            if (subCommand.equalsIgnoreCase("give")) {
                                account.setMoney(account.getMoney() + amount);
                                commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have given " + target.getName() + " $" + amount);
                            } else if (subCommand.equalsIgnoreCase("withdraw")) {
                                account.setMoney(account.getMoney() - amount);
                                commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have withdrew $" + amount + " from " + target.getName());
                            } else if (subCommand.equalsIgnoreCase("set")) {
                                account.setMoney(amount);
                                commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have set " + target.getName() + "'s money to " + amount);
                            } else {
                                commandContext.reply("", "&e/eco reset [player]", "&e/eco give [player] [money]", "&e/eco withdraw [player] [money]", "&e/eco set [player] [amount]", "");
                            }
                        } else if (commandContext.args().size() == 2 && commandContext.args().get(0).equalsIgnoreCase("reset")) {
                            account.setMoney(INSTANCE.getDefaultMoney());
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have reset " + target.getName() + "'s money!");
                        } else {
                            commandContext.reply("", "&e/eco reset [player]", "&e/eco give [player] [money]", "&e/eco withdraw [player] [money]", "&e/eco set [player] [amount]", "");
                        }
                    } else {
                        commandContext.reply("", "&e/eco reset [player]", "&e/eco give [player] [money]", "&e/eco withdraw [player] [money]", "&e/eco set [player] [amount]", "");
                    }
                }).registerAndBind(terminableConsumer, "economy", "eco");

        Commands.create()
                .handler(commandContext -> {
                    int page = commandContext.args().size() == 1 ? commandContext.arg(0).parseOrFail(Integer.class) : 1;
                    if (!baltopPages.containsKey(page)) {
                        commandContext.reply("&cThere isn't that many pages");
                    } else {
                        commandContext.reply("&eTotal Server Worth: &f" + new DecimalFormat("$###,###.##").format(serverTotal));
                        commandContext.reply("  &7---- &e&lMineage&6&lPVP &eBalanceTop &e[&e" + page + "/" + baltopPages.size() + "] &7----");
                        int i = 0;
                        for (Account account : baltopPages.get(page)) {
                            commandContext.reply("&f" + ((page * 10) - 9 + i) + "." + account.getLastKnownName() + ", &a" + new DecimalFormat("$###,###.##").format(account.getMoney()));
                            i++;
                        }
                    }
                    commandContext.reply(" ");
                }).registerAndBind(terminableConsumer, "baltop");
    }

    private Promise<HashMap<Integer, ArrayList<Account>>> calculateBalTop() {
        return Promise.start()
                .thenApplyAsync(aVoid -> {
                    List<Account> list = new ArrayList<>();
                    double amount = 0.0;
                    try {
                        for (Account account : INSTANCE.getAllAccounts()) {
                            list.add(account);
                            amount += account.getMoney();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    list.sort(Comparator.comparingDouble(Account::getMoney));
                    list = Lists.reverse(list);

                    this.serverTotal = amount;

                    HashMap<Integer, ArrayList<Account>> map = new HashMap<>();
                    ArrayList<Account> accounts = new ArrayList<>();
                    for (int i = 0, k = 1; i < list.size(); i++) {
                        if (i % 10 == 0) {
                            map.put(k, accounts);
                            k++;
                            accounts.clear();
                        }
                        accounts.add(list.get(i));
                    }
                    return map;
                });
    }
}
