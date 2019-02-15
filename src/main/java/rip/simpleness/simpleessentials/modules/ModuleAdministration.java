package rip.simpleness.simpleessentials.modules;

import com.google.common.base.Joiner;
import me.lucko.helper.Commands;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.argument.ArgumentParser;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import rip.simpleness.simpleessentials.Enchantments;
import rip.simpleness.simpleessentials.SimpleEssentials;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ModuleAdministration implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();
    private CooldownMap<UUID> fixHandMap = CooldownMap.create(Cooldown.of(5, TimeUnit.MINUTES));
    private CooldownMap<UUID> fixAllMap = CooldownMap.create(Cooldown.of(30, TimeUnit.MINUTES));
    private Location spawnPoint;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        spawnPoint = new Location(Bukkit.getWorld(INSTANCE.getConfig().getString("spawn.world")), INSTANCE.getConfig().getDouble("spawn.x"), INSTANCE.getConfig().getDouble("spawn.y"), INSTANCE.getConfig().getDouble("spawn.z"));
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
                }).registerAndBind(terminableConsumer, "broadcast", "bc");

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

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> commandContext.sender().teleport(spawnPoint, PlayerTeleportEvent.TeleportCause.COMMAND))
                .registerAndBind(terminableConsumer, "spawn");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.enderchest")
                .handler(commandContext -> commandContext.sender().openInventory(commandContext.sender().getEnderChest())).registerAndBind(terminableConsumer, "enderchest", "ec", "echest");

        Commands.parserRegistry().register(Enchantment.class, ArgumentParser.of(s -> {
            final Enchantment enchantment = Enchantments.getByName(s);
            return enchantment == null ? Optional.empty() : Optional.of(enchantment);
        }, s -> new CommandInterruptException("&cUnable to parse " + s + " as an Enchantment")));

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.enchant")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 2) {
                        final ItemStack hand = commandContext.sender().getItemInHand();
                        if (hand == null) {
                            commandContext.reply("&cYou can't enchant nothing!");
                        } else {
                            Enchantment enchantment = commandContext.arg(0).parseOrFail(Enchantment.class);
                            int level = commandContext.arg(1).parseOrFail(Integer.class);
                            hand.addUnsafeEnchantment(enchantment, level);
                            commandContext.sender().updateInventory();
                            commandContext.reply(INSTANCE.getServerPrefix() + "&aYou have enchanted your item with " + enchantment.getName() + " " + level);
                        }
                    } else {
                        commandContext.reply("&e/enchant [enchantment] [level]");
                        commandContext.reply("&eEnchants: " + Joiner.on(", ").skipNulls().join(Enchantments.keySet()));
                    }
                }).registerAndBind(terminableConsumer, "enchant");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.craft")
                .handler(commandContext -> commandContext.sender().openInventory(Bukkit.createInventory(commandContext.sender(), InventoryType.CRAFTING))).registerAndBind(terminableConsumer, "craft");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.ptime")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        String time = commandContext.arg(0).parseOrFail(String.class);
                        if (time.equalsIgnoreCase("day")) {
                            commandContext.sender().setPlayerTime(15000, true);
                        } else if (time.equalsIgnoreCase("night")) {
                            commandContext.sender().setPlayerTime(5000, true);
                        } else {
                            commandContext.reply("&eTimes: night, day");
                            return;
                        }
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have set your time to " + time);
                    } else {
                        commandContext.reply("Times: night, day");
                        commandContext.reply("&e/ptime [time]");
                    }
                }).registerAndBind(terminableConsumer, "ptime");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.fixhand")
                .handler(commandContext -> {
                    ItemStack hand = commandContext.sender().getItemInHand();
                    if (hand == null || hand.getType().isBlock() || hand.getDurability() == 0) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cUnable to fix this Item");
                    } else if (commandContext.sender().hasPermission("simpleness.fixhand.bypass") || fixHandMap.testSilently(commandContext.sender().getUniqueId())) {
                        hand.setDurability((short) 0);
                        commandContext.sender().updateInventory();
                        commandContext.reply(INSTANCE.getServerPrefix() + "&aYou have fixed a " + StringUtils.capitalize(hand.getType().name().replace("_", "").toLowerCase()));
                        fixHandMap.put(commandContext.sender().getUniqueId(), fixHandMap.getBase());
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't fixhand for another " + fixHandMap.remainingTime(commandContext.sender().getUniqueId(), TimeUnit.SECONDS) + " seconds");
                    }
                }).registerAndBind(terminableConsumer, "fixhand", "repair");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.fixall")
                .handler(commandContext -> {
                    HashSet<ItemStack> repairableItemStacks = getRepairableItemStacks(commandContext.sender().getInventory());
                    final String join = Joiner.on(", ")
                            .skipNulls()
                            .join(repairableItemStacks.stream()
                                    .map(itemStack -> StringUtils.capitalize(itemStack.getType().name().replace("_", " ")))
                                    .collect(Collectors.toList()));
                    if (repairableItemStacks.isEmpty()) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cThere's no repairable items in your inventory!");
                    } else if (commandContext.sender().hasPermission("simpleness.fixall.bypass") || fixAllMap.testSilently(commandContext.sender().getUniqueId())) {
                        repairableItemStacks.forEach(itemStack -> itemStack.setDurability((short) 0));
                        commandContext.sender().updateInventory();
                        commandContext.reply(INSTANCE.getServerPrefix() + "&aYou have fixed " + join);
                        fixAllMap.put(commandContext.sender().getUniqueId(), fixAllMap.getBase());
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&cYou can't fixall for another " + fixAllMap.remainingTime(commandContext.sender().getUniqueId(), TimeUnit.MINUTES) + " minutes");
                    }
                }).registerAndBind(terminableConsumer, "fixall", "repairall");

        /*HashSet<UUID> test = new HashSet<>();

        Commands.create()
                .handler(commandContext -> {
                    int amount = commandContext.arg(0).parseOrFail(Integer.class);
                    final long l = System.currentTimeMillis();
                    for (int i = 0; i < amount; i++) {
                        UUID uuid = UUID.randomUUID();
                        INSTANCE.getJedis().set(uuid.toString(), GsonProvider.prettyPrinting().toJson(INSTANCE.createAccount(uuid, RandomStringUtils.random(10))));
                        System.out.println("Created user test in the redis " + uuid);
                        test.add(uuid);
                    }
                    System.out.println("Took " + (System.currentTimeMillis() - l) + " ms to complete this task");
                }).registerAndBind(terminableConsumer, "papi");
        Commands.create()
                .handler(commandContext -> {
                    for (UUID uuid : test) {
                        INSTANCE.getAccountData().remove(uuid);
                        System.out.println("Removed " + uuid.toString());
                    }
                }).registerAndBind(terminableConsumer, "shit");*/
    }

    private HashSet<ItemStack> getRepairableItemStacks(PlayerInventory playerInventory) {
        HashSet<ItemStack> itemStacks = new HashSet<>();
        for (ItemStack content : playerInventory.getArmorContents()) {
            if (content != null && content.getDurability() > 0 && !content.getType().isBlock()) {
                itemStacks.add(content);
            }
        }
        for (ItemStack content : playerInventory.getContents()) {
            if (content != null && content.getDurability() > 0 && !content.getType().isBlock()) {
                itemStacks.add(content);
            }
        }
        return itemStacks;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }
}
