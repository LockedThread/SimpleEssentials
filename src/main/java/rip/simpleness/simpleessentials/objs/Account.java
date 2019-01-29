package rip.simpleness.simpleessentials.objs;

import rip.simpleness.simpleessentials.SimpleEssentials;

import java.util.HashMap;
import java.util.Objects;

public final class Account {

    private String lastKnownName;
    private double money;
    private HashMap<String, Long> usedKits;

    public Account(String lastKnownName) {
        this.lastKnownName = lastKnownName;
        this.usedKits = new HashMap<>();
        this.money = SimpleEssentials.getInstance().getDefaultMoney();
    }

    /*
     * NAME
     */

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    /*
     * ECONOMY
     */

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    /*
     * Custom Data
     */

    public HashMap<String, Long> getUsedKits() {
        return usedKits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Double.compare(account.money, money) == 0 &&
                Objects.equals(lastKnownName, account.lastKnownName) &&
                Objects.equals(usedKits, account.usedKits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastKnownName, money, usedKits);
    }

    @Override
    public String toString() {
        return "Account{" +
                "lastKnownName='" + lastKnownName + '\'' +
                ", money=" + money +
                ", usedKits=" + usedKits +
                '}';
    }
}
