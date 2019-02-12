package rip.simpleness.simpleessentials.modules;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import me.lucko.helper.Commands;
import me.lucko.helper.serialize.GsonStorageHandler;
import me.lucko.helper.serialize.InventorySerialization;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.simpleness.simpleessentials.ParseRegistrar;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.map.SimplenessMap;
import rip.simpleness.simpleessentials.objs.Account;
import rip.simpleness.simpleessentials.objs.Kit;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;

public class ModuleKit implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    private SimplenessMap<String, Kit> kitData;
    private GsonStorageHandler<SimplenessMap<String, Kit>> kitDataGsonStorage;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.kitDataGsonStorage = new GsonStorageHandler<>("kits", ".json", INSTANCE.getDataFolder(), new TypeToken<SimplenessMap<String, Kit>>() {
        });
        this.kitData = kitDataGsonStorage.load().orElse(new SimplenessMap<>());
        kitData.forEach((name, kit) -> {
            kit.setArmorContents(InventorySerialization.decodeItemStacks(kit.getEncodedArmorContents()));
            kit.setInventoryContents(InventorySerialization.decodeItemStacks(kit.getEncodedInventoryContents()));
        });
        terminableConsumer.bind(() -> kitDataGsonStorage.save(kitData));

        Commands.parserRegistry().register(Kit.class, ParseRegistrar.buildParser("kit", s -> isKit(s) ? Optional.of(getKit(s)) : Optional.empty()));

        Commands.create()
                .assertPermission("simpleness.deletekit")
                .handler(commandContext -> {
                    Kit kit = commandContext.arg(0).parseOrFail(Kit.class);
                    deleteKit(kit.getName());
                    commandContext.reply(INSTANCE.getServerPrefix() + "&cYou have deleted the " + kit.getName() + " kit");
                }).registerAndBind(terminableConsumer, "deletekit", "delkit");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.createkit")
                .assertArgument(0, s -> true)
                .handler(commandContext -> {
                    if (commandContext.args().size() > 0) {
                        String kitName = commandContext.arg(0).parseOrFail(String.class);
                        if (isKit(kitName)) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cThere's already a kit with the name " + kitName + " defined.");
                        } else if (commandContext.args().size() == 1) {
                            createKit(commandContext.sender(), kitName);
                        } else if (commandContext.args().size() == 2) {
                            createKit(commandContext.sender(), kitName, commandContext.arg(1).parseOrFail(Long.class));
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&e/createkit [name] {cooldown}");
                        }
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/createkit [name] {cooldown}");
                    }
                });

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.addkitcommand")
                .handler(commandContext -> {
                    if (commandContext.args().size() >= 2) {
                        Kit kit = commandContext.arg(0).parseOrFail(Kit.class);
                        String command = Joiner.on(" ").skipNulls().join(commandContext.args().subList(1, commandContext.args().size()));
                        kit.getCommands().add(command.startsWith("/") ? command.substring(1) : command);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&aYou've added the kit command \"" + command + "\" to " + kit.getName());
                    } else {
                        commandContext.reply("&e/addkitcommand [kit] [command]");
                    }
                }).registerAndBind(terminableConsumer, "addkitcommand");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.kits")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        final String kits = getKits(commandContext.sender());
                        commandContext.reply(kits.isEmpty() ? INSTANCE.getServerPrefix() + "&cYou don't have access to any kits!" : INSTANCE.getServerPrefix() + "&eKits: " + kits);
                    } else if (commandContext.label().equalsIgnoreCase("kits")) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/kits");
                    } else if (commandContext.args().size() == 1) {
                        Kit kit = commandContext.arg(0).parseOrFail(Kit.class);
                        if (kit.hasPermission(commandContext.sender())) {
                            String error = giveKit(commandContext.sender(), kit);
                            if (error.isEmpty()) {
                                commandContext.reply(INSTANCE.getServerPrefix() + "&aYou have recieved the &e" + kit.getName() + " kit");
                            } else {
                                commandContext.reply(INSTANCE.getServerPrefix() + "&cYou have to wait " + error + " before you do this kit again!");
                            }
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cYou don't have permission to use this kit!");
                        }
                    } else if (commandContext.args().size() == 2) {
                        Kit kit = commandContext.arg(0).parseOrFail(Kit.class);
                        Player target = commandContext.arg(1).parseOrFail(Player.class);
                        giveKit(target, kit);
                        commandContext.reply(INSTANCE.getServerPrefix() + "&aYou have given &e" + target.getName() + " &athe " + kit.getName() + " kit");
                    } else {
                        commandContext.reply("&e/kit [name]");
                    }
                }).registerAndBind(terminableConsumer, "kits", "kit");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.createkit")
                .handler(commandContext -> {
                    if (commandContext.args().size() >= 1) {
                        String name = commandContext.arg(0).parseOrFail(String.class);
                        if (isKit(name)) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cYou're unable to create a kit that already exists.");
                        } else if (commandContext.args().size() == 2) {
                            long cooldown = commandContext.arg(1).parseOrFail(Long.class);
                            createKit(commandContext.sender(), name, cooldown);
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have created a kit with a cooldown of " + cooldown + " seconds");
                        } else {
                            createKit(commandContext.sender(), name);
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have created a kit with no cooldown");
                        }
                    }
                }).registerAndBind(terminableConsumer, "createkit");
    }


    public String getKits(Player player) {
        if (player.isOp()) {
            return Joiner.on(", ").skipNulls().join(kitData.keySet());
        }
        final Account account = INSTANCE.getAccount(player);
        ArrayList<String> list = new ArrayList<>();
        kitData.forEach((key, kit) -> {
            if (kit.hasPermission(player)) {
                if (account.getUsedKits().containsKey(kit.getName()) && ((System.currentTimeMillis() / 1000) - account.getUsedKits().get(kit.getName())) < kit.getCooldown()) {
                    list.add(ChatColor.STRIKETHROUGH + key + ChatColor.YELLOW);
                } else {
                    list.add(ChatColor.YELLOW + key);
                }
            }
        });
        return Joiner.on(", ").skipNulls().join(list);
    }

    public void createKit(Player player, String name, long cooldown) {
        name = name.toLowerCase();
        Kit kit = new Kit(name, player.getInventory().getContents(), player.getInventory().getArmorContents(), cooldown, false, "", new ArrayList<>());
        kitData.put(name, kit);
    }

    public void createKit(Player player, String name) {
        createKit(player, name, 0L);
    }

    public void deleteKit(String key) {
        kitData.remove(key);
    }

    public Kit getKit(String key) {
        return kitData.get(key.toLowerCase());
    }

    public boolean isKit(String key) {
        return kitData.containsKey(key.toLowerCase());
    }

    public String giveKit(Player player, Kit kit) {
        final Account account = INSTANCE.getAccount(player);

        long currentTime = System.currentTimeMillis() / 1000;
        if (account.getUsedKits().containsKey(kit.getName())) {
            long time = account.getUsedKits().get(kit.getName());
            long subtracted = currentTime - time;
            if (subtracted < kit.getCooldown()) {
                return TimeUtil.toLongForm(Math.abs((time - currentTime) + kit.getCooldown()));
            } else {
                account.getUsedKits().remove(kit.getName());
            }
        }
        if (kit.getCooldown() > 0) {
            account.getUsedKits().put(kit.getName(), currentTime);
        }
        boolean drop = false;
        for (ItemStack itemStack : kit.getInventoryContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                drop = true;
            } else {
                player.getInventory().addItem(itemStack);
            }
        }
        for (ItemStack itemStack : kit.getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                drop = true;
            } else {
                player.getInventory().addItem(itemStack);
            }
        }
        for (String command : kit.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }
        if (drop) {
            player.sendMessage(Text.colorize(INSTANCE.getServerPrefix() + "&cSome items don't fit in your inventory and were dropped."));
        }
        return "";
    }
}
