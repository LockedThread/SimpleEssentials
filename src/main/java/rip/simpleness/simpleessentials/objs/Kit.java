package rip.simpleness.simpleessentials.objs;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class Kit {

    private final String name;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;
    private final long cooldown;
    private final boolean oneTimeUse;
    private final String permission;
    private final String[] commands;

    public Kit(String name, ItemStack[] inventoryContents, ItemStack[] armorContents, long cooldown, boolean oneTimeUse, String permission, String[] commands) {
        this.name = name;
        this.inventoryContents = inventoryContents;
        this.armorContents = armorContents;
        this.cooldown = cooldown;
        this.oneTimeUse = oneTimeUse;
        this.permission = permission;
        this.commands = commands;
    }

    public boolean hasPermission(Player player) {
        return permission.isEmpty() || player.hasPermission(permission);
    }

    public ItemStack[] getInventoryContents() {
        return inventoryContents;
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    public long getCooldown() {
        return cooldown;
    }

    public boolean isOneTimeUse() {
        return oneTimeUse;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getCommands() {
        return commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return cooldown == kit.cooldown &&
                oneTimeUse == kit.oneTimeUse &&
                Arrays.equals(inventoryContents, kit.inventoryContents) &&
                Arrays.equals(armorContents, kit.armorContents) &&
                Objects.equals(permission, kit.permission) &&
                Arrays.equals(commands, kit.commands);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(cooldown, oneTimeUse, permission);
        result = 31 * result + Arrays.hashCode(inventoryContents);
        result = 31 * result + Arrays.hashCode(armorContents);
        result = 31 * result + Arrays.hashCode(commands);
        return result;
    }

    @Override
    public String toString() {
        return "Kit{" +
                "inventoryContents=" + Arrays.toString(inventoryContents) +
                ", armorContents=" + Arrays.toString(armorContents) +
                ", cooldown=" + cooldown +
                ", oneTimeUse=" + oneTimeUse +
                ", permission='" + permission + '\'' +
                ", commands=" + Arrays.toString(commands) +
                '}';
    }

    public String getName() {
        return name;
    }
}
