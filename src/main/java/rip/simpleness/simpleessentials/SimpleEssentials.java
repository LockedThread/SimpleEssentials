package rip.simpleness.simpleessentials;

import com.fasterxml.jackson.jr.ob.JSON;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.ServicePriority;
import rip.simpleness.simpleessentials.economy.SimpleEconomy;
import rip.simpleness.simpleessentials.modules.*;
import rip.simpleness.simpleessentials.objs.Account;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(name = "SimpleEssentials",
        version = "1.0",
        description = "Simpleness's Essentials alternative",
        load = PluginLoadOrder.STARTUP,
        authors = "Simpleness",
        website = "www.simpleness.rip",
        depends = {@PluginDependency("Vault"), @PluginDependency("helper")})
public final class SimpleEssentials extends ExtendedJavaPlugin {

    public static JSON json;
    private SimpleEconomy provider;
    private double defaultMoney;
    private String firstJoinMessage, motdMessage, serverPrefix, broadcastPrefix;
    private int playerJoins;
    private boolean disableWeather;
    private ConcurrentHashMap<UUID, Account> accountData;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;


    private ModuleAdministration moduleAdministration;

    @Override
    protected void enable() {
        this.accountData = new ConcurrentHashMap<>();
        /*
         * Config
         */
        saveDefaultConfig();
        this.disableWeather = getConfig().getBoolean("disable-weather");
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

        json = JSON.std.with(JSON.Feature.WRITE_NULL_PROPERTIES);

        this.redisClient = RedisClient.create(RedisURI.builder().withHost(getConfig().getString("redis.address"))
                .withPort(getConfig().getInt("redis.port"))
                .withPassword(getConfig().getString("redis.password"))
                .build());
        this.redisClient.setDefaultTimeout(20, TimeUnit.SECONDS);
        this.connection = redisClient.connect();

        /*
         * Modules
         */
        bindModule(new ModuleAdministration());
        bindModule(new ModuleEconomy());
        bindModule(new ModuleAccount());
        bindModule(new ModuleWarp());
        bindModule(new ModuleTeleportation());
        bindModule(new ModuleKit());
        bindModule(new ModuleFixes());
        this.moduleAdministration = new ModuleAdministration();
        bindModule(moduleAdministration);
        bindModule(new ModuleHome());
        bindModule(new ModuleClearLag());

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
        Iterator<Map.Entry<UUID, Account>> iterator = getAccountData().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Account> next = iterator.next();
            try {
                getRedis().set(next.getKey().toString(), json.asString(next.getValue()));
                iterator.remove();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getAccountData().clear();

        try {
            connection.close();
            redisClient.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SimpleEssentials getInstance() {
        return getPlugin(SimpleEssentials.class);
    }

    public ConcurrentHashMap<UUID, Account> getAccountData() {
        return accountData;
    }

    public Account createAccount(UUID uuid, String name) {
        Account account = new Account(name);
        accountData.put(uuid, account);
        return account;
    }

    public Account getAccount(UUID uuid) {
        return accountData.get(uuid);
    }

    public Account getOnlineAccount(Player player) {
        return accountData.get(player.getUniqueId());
    }

    public Account getAccount(Player player) {
        return getAccount(player.getUniqueId());
    }

    public Account getOfflineAccount(UUID uuid) {
        try {
            return SimpleEssentials.json.beanFrom(Account.class, getRedis().get(uuid.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public Account getOrCreateAccount(UUID uuid) {
        if (getAccount(uuid) != null) {
            return getAccount(uuid);
        }
        Player player = Bukkit.getPlayer(uuid);
        if (getAccount(player) != null) {
            return getAccount(player);
        }
        final Account offlineAccount = getOfflineAccount(uuid);
        if (offlineAccount != null) {
            return offlineAccount;
        }
        return createAccount(uuid, player.getName());
    }

    public boolean hasAccount(UUID uuid) {
        return getAccountData().containsKey(uuid);
    }

    public SimpleEconomy getProvider() {
        return provider;
    }

    public RedisCommands<String, String> getRedis() {
        return connection.sync();
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

    public boolean isDisableWeather() {
        return disableWeather;
    }

    public ModuleAdministration getModuleAdministration() {
        return moduleAdministration;
    }

    public Set<Account> getAllAccounts() throws IOException {
        Set<Account> set = new HashSet<>();
        for (String key : getRedis().keys("*")) {
            Account account = Bukkit.getOfflinePlayer(UUID.fromString(key)).isOnline() ? getAccount(UUID.fromString(key)) : SimpleEssentials.json.beanFrom(Account.class, getRedis().get(key));
            set.add(account);
        }
        return set;
    }
}
