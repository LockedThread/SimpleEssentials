package rip.simpleness.simpleessentials;

import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.ServicePriority;
import redis.clients.jedis.Jedis;
import rip.simpleness.simpleessentials.economy.SimpleEconomy;
import rip.simpleness.simpleessentials.modules.*;
import rip.simpleness.simpleessentials.objs.Account;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Plugin(name = "SimpleEssentials",
        version = "1.0",
        description = "Simpleness's Essentials alternative",
        load = PluginLoadOrder.STARTUP,
        authors = "Simpleness",
        website = "www.simpleness.rip",
        depends = {@PluginDependency("Vault"), @PluginDependency("helper")})
public final class SimpleEssentials extends ExtendedJavaPlugin {

    private HashMap<UUID, Account> accountData;
    private SimpleEconomy provider;

    private double defaultMoney;
    private String firstJoinMessage, motdMessage, serverPrefix, broadcastPrefix;
    private int playerJoins;

    private Jedis jedis;

    private ModuleWarp moduleWarp;

    @Override
    protected void enable() {
        /*
         * Config
         */
        saveDefaultConfig();
        this.defaultMoney = getConfig().getDouble("default-money");
        this.firstJoinMessage = Text.colorize(getConfig().getString("first-join-message"));
        this.motdMessage = getConfig().getStringList("motd-message")
                .stream()
                .map(s -> Text.colorize(s) + "\n")
                .collect(Collectors.joining());
        this.serverPrefix = Text.colorize(getConfig().getString("server-prefix"));
        this.playerJoins = getConfig().getInt("player-joins");
        this.broadcastPrefix = Text.colorize(getConfig().getString("broadcast-prefix"));

        /*
         * Storage & Data
         */

        jedis = new Jedis(getConfig().getString("redis.address"), getConfig().getInt("redis.port"));
        jedis.auth(getConfig().getString("redis.password"));
        this.accountData = new HashMap<>();

        /*
         * Modules
         */
        this.moduleWarp = new ModuleWarp();
        bindModule(moduleWarp);
        bindModule(new ModuleAccount());
        bindModule(new ModuleEconomy());
        bindModule(new ModuleTeleportation());
        bindModule(new ModuleAdministration());
        bindModule(new ModuleKit());

        /*
         * Economy/Vault
         */
        this.provider = new SimpleEconomy(this);
        getServer().getServicesManager().register(Economy.class, this.provider, this, ServicePriority.Highest);
        getLogger().info("Vault hooked");
    }

    @Override
    protected void disable() {
        getServer().getServicesManager().unregister(Economy.class, this.provider);
        getConfig().set("player-joins", playerJoins);
        saveConfig();
        accountData.forEach((key, value) -> jedis.set(key.toString(), GsonProvider.prettyPrinting().toJson(value)));
    }

    public static SimpleEssentials getInstance() {
        return getPlugin(SimpleEssentials.class);
    }

    public HashMap<UUID, Account> getAccountData() {
        return accountData;
    }

    public void createAccount(UUID uuid, String name) {
        accountData.put(uuid, new Account(name));
    }

    public Account getAccount(UUID uuid) {
        return accountData.get(uuid);
    }

    public Account getAccount(Player player) {
        return getAccount(player.getUniqueId());
    }

    public Account getOfflineAccount(UUID uuid) {
        return getJedis().exists(uuid.toString()) ?
                GsonProvider.prettyPrinting().fromJson(getJedis().get(uuid.toString()), Account.class) :
                null;
    }

    public Account getAccount(String s) {
        UUID uuid;
        try {
            uuid = UUID.fromString(s);
        } catch (IllegalArgumentException ignored) {
            return getAccount(Bukkit.getPlayer(s));
        }
        return getAccount(uuid);
    }

    public boolean hasAccount(UUID uuid) {
        return getAccountData().containsKey(uuid);
    }

    public SimpleEconomy getProvider() {
        return provider;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public ModuleWarp getModuleWarp() {
        return moduleWarp;
    }

    /*
     * Config Entries
     */

    public double getDefaultMoney() {
        return defaultMoney;
    }

    public String getFirstJoinMessage() {
        return firstJoinMessage;
    }

    public String getMotdMessage() {
        return motdMessage;
    }

    public String getServerPrefix() {
        return serverPrefix;
    }

    public int getPlayerJoins() {
        return playerJoins;
    }

    public void setPlayerJoins(int playerJoins) {
        this.playerJoins = playerJoins;
    }

    public String getBroadcastPrefix() {
        return broadcastPrefix;
    }
}
