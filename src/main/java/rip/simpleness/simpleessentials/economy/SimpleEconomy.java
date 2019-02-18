package rip.simpleness.simpleessentials.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.objs.Account;

import java.util.List;
import java.util.UUID;

public class SimpleEconomy implements Economy {

    private SimpleEssentials instance;

    public SimpleEconomy(SimpleEssentials instance) {
        this.instance = instance;
    }

    @Override
    public boolean isEnabled() {
        return instance != null && instance.isEnabled();
    }

    @Override
    public String getName() {
        return "SimpleEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        amount = Math.ceil(amount);
        return amount == 1 ? String.format("%d %s", (int) amount, currencyNameSingular()) : String.format("%d %s", (int) amount, currencyNamePlural());
    }

    @Override
    public String currencyNamePlural() {
        return "dollars";
    }

    @Override
    public String currencyNameSingular() {
        return "dollar";
    }

    public boolean hasAccount(UUID uuid) {
        return instance.hasAccount(uuid);
    }

    @Override
    public boolean hasAccount(String s) {
        return hasAccount(Bukkit.getPlayer(s));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.getUniqueId() == null) {
            return hasAccount(offlinePlayer.getName());
        }
        return hasAccount(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return hasAccount(s);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return hasAccount(offlinePlayer);
    }

    public double getBalance(UUID uuid) {
        return instance.getAccount(uuid).getMoney();
    }

    @Override
    public double getBalance(String s) {
        return instance.getAccount(s).getMoney();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            return instance.getAccount(offlinePlayer.getUniqueId()).getMoney();
        }
        System.out.println(offlinePlayer.getUniqueId() + " is not online");
        final Account offlineAccount = instance.getOfflineAccount(offlinePlayer.getUniqueId());
        if (offlineAccount == null) {
            return 0.0;
        }
        return offlineAccount.getMoney();
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(s);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    public boolean has(UUID uuid, double amount) {
        return instance.getAccount(uuid).getMoney() >= amount;
    }

    @Override
    public boolean has(String s, double amount) {
        return instance.getAccount(s).getMoney() >= amount;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        return has(offlinePlayer.getUniqueId(), amount);
    }

    @Override
    public boolean has(String s, String s1, double amount) {
        return has(s, amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double amount) {
        return has(offlinePlayer, amount);
    }

    private EconomyResponse withdrawPlayer(UUID uuid, double amount) {
        Account account = instance.getAccount(uuid);
        final double balance = account.getMoney();
        if (balance - amount > 0.0 && balance >= amount) {
            account.setMoney(balance - amount);
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "Error");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double amount) {
        final Account account = instance.getAccount(s);
        final double balance = account.getMoney();
        if (balance - amount > 0.0 && balance >= amount) {
            account.setMoney(balance - amount);
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, "");
        }
        return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "Error");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        return withdrawPlayer(offlinePlayer.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double amount) {
        return withdrawPlayer(s, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double amount) {
        return withdrawPlayer(offlinePlayer.getUniqueId(), amount);
    }

    public EconomyResponse depositPlayer(UUID uuid, double amount) {
        final Account account = instance.getAccount(uuid);
        account.setMoney(account.getMoney() + amount);
        return new EconomyResponse(amount, account.getMoney(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String s, double amount) {
        final Account account = instance.getAccount(s);
        account.setMoney(account.getMoney() + amount);
        return new EconomyResponse(amount, account.getMoney(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        return depositPlayer(offlinePlayer.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double amount) {
        return depositPlayer(s, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double amount) {
        return depositPlayer(offlinePlayer.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double amount) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}