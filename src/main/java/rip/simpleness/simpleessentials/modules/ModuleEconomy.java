package rip.simpleness.simpleessentials.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import rip.simpleness.simpleessentials.MathUtils;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import javax.annotation.Nonnull;

public class ModuleEconomy implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Commands.create()
                .assertPlayer()
                .handler(commandContext -> commandContext.reply("&eBalance: &f" + INSTANCE.getAccount(commandContext.sender()).getMoney()))
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
                        Player target = commandContext.arg(1).parseOrFail(Player.class);
                        final Account account = INSTANCE.getAccount(target);
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
                            INSTANCE.getAccount(target).setMoney(INSTANCE.getDefaultMoney());
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have reset " + target.getName() + "'s money!");
                        } else {
                            commandContext.reply("", "&e/eco reset [player]", "&e/eco give [player] [money]", "&e/eco withdraw [player] [money]", "&e/eco set [player] [amount]", "");
                        }
                    } else {
                        commandContext.reply("", "&e/eco reset [player]", "&e/eco give [player] [money]", "&e/eco withdraw [player] [money]", "&e/eco set [player] [amount]", "");
                    }
                }).registerAndBind(terminableConsumer, "economy", "eco");
    }
}
