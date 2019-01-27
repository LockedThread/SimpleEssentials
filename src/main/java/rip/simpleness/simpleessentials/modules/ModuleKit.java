package rip.simpleness.simpleessentials.modules;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import me.lucko.helper.Commands;
import me.lucko.helper.serialize.GsonStorageHandler;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.map.SimplenessMap;
import rip.simpleness.simpleessentials.objs.Account;
import rip.simpleness.simpleessentials.objs.Kit;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleKit implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    private SimplenessMap<String, Kit> kitData;
    private GsonStorageHandler<SimplenessMap<String, Kit>> kitDataGsonStorage;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.kitDataGsonStorage = new GsonStorageHandler<>("kits", ".json", INSTANCE.getDataFolder(), new TypeToken<SimplenessMap<String, Kit>>() {
        });
        this.kitData = kitDataGsonStorage.load().orElse(new SimplenessMap<>());
        terminableConsumer.bind(() -> kitDataGsonStorage.save(kitData));

        Commands.parserRegistry().register(Kit.class, s -> Optional.of(getKit(s)));

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.createkit")
                .assertArgument(0, s -> true)
                .handler(commandContext -> {
                    if (commandContext.args().size() > 0) {
                        String warpName = commandContext.arg(0).parseOrFail(String.class);
                        if (isKit(warpName)) {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&cThere's already a kit with the name " + warpName + " defined.");
                        } else if (commandContext.args().size() == 1) {
                            createKit(commandContext.sender(), warpName);
                        } else if (commandContext.args().size() == 2) {
                            createKit(commandContext.sender(), warpName, commandContext.arg(1).parseOrFail(Long.class));
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&e/createkit [name] {cooldown}");
                        }
                    } else {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/createkit [name] {cooldown}");
                    }
                });

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.kits")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        final String kits = getKits(commandContext.sender());
                        commandContext.reply(kits.isEmpty() ?
                                INSTANCE.getServerPrefix() + "&eYou don't have access to any kits!" :
                                INSTANCE.getServerPrefix() + "&eKits: " + kits);
                    } else if (commandContext.label().equalsIgnoreCase("kits")) {
                        commandContext.reply(INSTANCE.getServerPrefix() + "&e/kits");
                    } else if (commandContext.args().size() == 1) {
                        Kit kit = commandContext.arg(0).parseOrFail(Kit.class);
                        if (kit.hasPermission(commandContext.sender())) {
                            giveKit(commandContext.sender(), kit);
                        } else {
                            commandContext.reply(INSTANCE.getServerPrefix() + "&eYou don't have permission to use this kit!");
                        }
                    } else {
                        commandContext.reply("&e/kit [name]");
                    }
                }).registerAndBind(terminableConsumer, "kits", "kit");

        Commands.create()
                .assertPlayer()
                .assertPermission("simpleness.createkit")
                .handler(commandContext -> {

                }).registerAndBind(terminableConsumer, "createkit");

    }


    public String getKits(Player player) {
        final Account account = INSTANCE.getAccount(player);
        ArrayList<String> list = kitData.entrySet()
                .stream()
                .filter(entry -> entry.getValue().hasPermission(player))
                .map(entry -> account.getUsedKits().containsKey(entry.getValue().getName()) ? ChatColor.STRIKETHROUGH + entry.getKey() + ChatColor.YELLOW : ChatColor.YELLOW + entry.getKey())
                .collect(Collectors.toCollection(ArrayList::new));
        return Joiner.on(", ").skipNulls().join(list);
    }

    public void createKit(Player player, String name, long cooldown) {
        Kit kit = new Kit(name, player.getInventory().getContents(), player.getInventory().getArmorContents(), cooldown, false, "", new String[0]);
        kitData.put(name, kit);
    }

    public void createKit(Player player, String name) {
        createKit(player, name, 0L);
    }

    public void deleteKit(String warpName) {
        kitData.remove(warpName);
    }

    public Kit getKit(String key) {
        return kitData.get(key.toLowerCase());
    }

    public boolean isKit(String key) {
        return kitData.containsKey(key.toLowerCase());
    }

    public void giveKit(Player player, Kit kit) {
        boolean drop = false;
        for (ItemStack itemStack : kit.getInventoryContents()) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                drop = true;
            } else {
                player.getInventory().addItem(itemStack);
            }
        }
        for (ItemStack itemStack : kit.getArmorContents()) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                drop = true;
            } else {
                player.getInventory().addItem(itemStack);
            }
        }

        INSTANCE.getAccount(player).getUsedKits().put(kit.getName(), System.currentTimeMillis());

        for (String command : kit.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        if (drop) {
            player.sendMessage(Text.colorize(INSTANCE.getServerPrefix() + "&cSome items don't fit in your inventory and were dropped."));
        }
    }
}
