package rip.simpleness.simpleessentials.modules;

import com.google.common.base.Joiner;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import rip.simpleness.simpleessentials.SimpleEssentials;

import javax.annotation.Nonnull;

public class ModuleAdministration implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.gamemode.creative")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.sender().setGameMode(GameMode.CREATIVE);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have changed your gamemode to &aCreative");
                    } else if (commandContext.args().size() == 1) {
                        Player target = commandContext.arg(0).parseOrFail(Player.class);
                        target.setGameMode(GameMode.CREATIVE);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have set " + target.getName() + "'s gamemode to &aCreative");
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/gms {player}");
                    }
                }).registerAndBind(terminableConsumer, "gmc");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.gamemode.survival")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.sender().setGameMode(GameMode.SURVIVAL);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have changed your gamemode to &aSurvival");
                    } else if (commandContext.args().size() == 1) {
                        Player target = commandContext.arg(0).parseOrFail(Player.class);
                        target.setGameMode(GameMode.SURVIVAL);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have set " + target.getName() + "'s gamemode to &aSurvival");
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/gms {player}");
                    }
                }).registerAndBind(terminableConsumer, "gms");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.tp")
                .handler(commandContext -> {
                    Player target = commandContext.arg(0).parseOrFail(Player.class);
                    if (target.getName().equals(commandContext.sender().getName())) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't teleport to yourself");
                    } else {
                        commandContext.sender().teleport(target, PlayerTeleportEvent.TeleportCause.COMMAND);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have teleported to " + target.getName());
                    }
                }).registerAndBind(terminableConsumer, "tp");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.tphere")
                .handler(commandContext -> {
                    Player target = commandContext.arg(0).parseOrFail(Player.class);
                    if (target.getName().equals(commandContext.sender().getName())) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't teleport to yourself");
                    } else {
                        target.teleport(commandContext.sender(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have teleported " + target.getName() + " to yourself");
                    }
                }).registerAndBind(terminableConsumer, "tphere");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.feed")
                .handler(commandContext -> {
                    commandContext.reply(INSTANCE.getServerPrefix() + "&eYour food level has been saturated");
                    commandContext.sender().setFoodLevel(20);
                }).registerAndBind(terminableConsumer, "feed");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.heal")
                .handler(commandContext -> {
                    commandContext.reply(INSTANCE.getServerPrefix() + "&eYour health level has been saturated");
                    commandContext.sender().setHealth(20.0);
                }).registerAndBind(terminableConsumer, "heal");

        Commands.create()
                .handler(commandContext -> {
                    Player target;
                    if (commandContext.args().size() == 0 && commandContext.sender().hasPermission("simpleness.clearinventory")) {
                        if (commandContext.sender() instanceof Player) {
                            target = (Player) commandContext.sender();
                            target.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
                            target.getInventory().clear();
                            target.updateInventory();
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cYou've cleared your inventory");
                        } else {
                            commandContext.reply("&c/clearinventory [player]");
                        }
                    } else if (commandContext.args().size() == 1) {
                        if (commandContext.sender().hasPermission("simpleness.clearinventory.others")) {
                            target = commandContext.arg(0).parseOrFail(Player.class);
                            target.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
                            target.getInventory().clear();
                            target.updateInventory();
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cYou've cleared the inventory of " + target.getName());
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cYou don't have permission to clear other player's inventories");
                        }
                    }
                }).registerAndBind(terminableConsumer, "ci", "clear", "clearinventory");

        Commands.create()
                .assertPermission("simpleness.broadcast")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cYou must specify a message to be broadcasted.");
                    } else {
                        Bukkit.broadcastMessage(Text.colorize(INSTANCE.getBroadcastPrefix() + Joiner.on(" ").skipNulls().join(commandContext.args())));
                    }
                }).registerAndBind(terminableConsumer, "broadcast");

        Commands.create()
                .assertPermission("simpleness.vanish")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        if (commandContext.sender() instanceof Player) {
                            Player player = (Player) commandContext.sender();
                            if (player.hasMetadata("vanish")) {
                                Players.all()
                                        .stream()
                                        .filter(other -> !other.getName().equals(commandContext.sender().getName()))
                                        .forEach(other -> other.showPlayer(player));
                                commandContext.reply(INSTANCE.getServerPrefix() + "&eVanish &cDISABLED");
                                player.removeMetadata("vanish", INSTANCE);
                            } else {
                                Players.all()
                                        .stream()
                                        .filter(other -> !other.getName().equals(commandContext.sender().getName()))
                                        .forEach(other -> other.hidePlayer(player));
                                commandContext.reply(INSTANCE.getServerPrefix() + "&eVanish &aENABLED");
                                player.setMetadata("vanish", new FixedMetadataValue(INSTANCE, true));
                            }
                        } else {
                            commandContext.reply("&e/vanish [player]");
                        }
                    }
                }).registerAndBind(terminableConsumer, "vanish", "v");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.rename")
                .handler(commandContext -> {
                    ItemStack itemInHand = commandContext.sender().getItemInHand();
                    if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                        commandContext.reply("&cYou must have an item in your hand to edit the name.");
                    } else {
                        String joinedText = commandContext.args().size() >= 1 ? Joiner.on(" ").skipNulls().join(commandContext.args()) : "nothing";
                        ItemMeta itemMeta = itemInHand.getItemMeta();
                        itemMeta.setDisplayName(Text.colorize(joinedText));
                        itemInHand.setItemMeta(itemMeta);
                        commandContext.sender().setItemInHand(itemInHand);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have renamed the item in your hand to " + joinedText);
                    }
                }).registerAndBind(terminableConsumer, "rename");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.speed")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        int speed = commandContext.arg(0).parseOrFail(Integer.class);
                        if (speed > 10) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cUnable to set your speed to greater than 10.");
                        } else {
                            String type;
                            if (commandContext.sender().isFlying()) {
                                type = "flight";
                                commandContext.sender().setFlySpeed((float) speed / 10);
                            } else {
                                type = "walking";
                                commandContext.sender().setWalkSpeed((float) speed / 10);
                            }
                            commandContext.reply(INSTANCE.getServerPrefix() + "&aYou have set your " + type + " speed to " + speed);
                        }
                    } else {
                        commandContext.reply("&e/speed [amplifier]");
                    }
                }).registerAndBind(terminableConsumer, "speed");
    }
}
