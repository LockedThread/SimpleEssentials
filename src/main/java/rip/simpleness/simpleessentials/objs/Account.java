package rip.simpleness.simpleessentials.objs;

import me.lucko.helper.serialize.Point;
import rip.simpleness.simpleessentials.SimpleEssentials;

import java.util.HashMap;
import java.util.Objects;

public final class Account {

    private String lastKnownName;
    private double money;
    private HashMap<String, Long> usedKits;
    private HashMap<String, Point> homes;

    public Account(String lastKnownName) {
        this.lastKnownName = lastKnownName;
        this.usedKits = new HashMap<>();
        this.homes = new HashMap<>();
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

    public HashMap<String, Point> getHomes() {
        return homes;
    }

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
                Objects.equals(usedKits, account.usedKits) &&
                Objects.equals(homes, account.homes);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = lastKnownName != null ? lastKnownName.hashCode() : 0;
        temp = Double.doubleToLongBits(money);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (usedKits != null ? usedKits.hashCode() : 0);
        result = 31 * result + (homes != null ? homes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Account{" +
                "lastKnownName='" + lastKnownName + '\'' +
                ", money=" + money +
                ", usedKits=" + usedKits +
                ", homes=" + homes +
                '}';
    }
}
