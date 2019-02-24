package rip.simpleness.simpleessentials.objs;

import me.lucko.helper.serialize.InventorySerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Kit {

    private final String name, encodedInventoryContents, encodedArmorContents;
    private final List<String> commands;
    private final long cooldown;
    private final boolean oneTimeUse;
    private String permission;
    private transient ItemStack[] inventoryContents, armorContents;

    public Kit(@NotNull String name, @NotNull ItemStack[] inventoryContents, @NotNull ItemStack[] armorContents, long cooldown, boolean oneTimeUse, @NotNull String permission, @NotNull List<String> commands) {
        this.name = name;
        this.inventoryContents = inventoryContents;
        this.armorContents = armorContents;
        this.cooldown = cooldown;
        this.oneTimeUse = oneTimeUse;
        this.permission = permission;
        this.commands = commands;
        this.encodedInventoryContents = InventorySerialization.encodeItemStacksToString(inventoryContents);
        this.encodedArmorContents = InventorySerialization.encodeItemStacksToString(armorContents);
    }

    public boolean hasPermission(Player player) {
        return permission.isEmpty() || player.hasPermission(permission) || player.isOp();
    }

    public void setPermission(String permission) {
        this.permission = permission;
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

    public List<String> getCommands() {
        return commands;
    }

    public void setArmorContents(ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return cooldown == kit.cooldown &&
                oneTimeUse == kit.oneTimeUse &&
                Objects.equals(name, kit.name) &&
                Objects.equals(encodedInventoryContents, kit.encodedInventoryContents) &&
                Objects.equals(encodedArmorContents, kit.encodedArmorContents) &&
                Arrays.equals(inventoryContents, kit.inventoryContents) &&
                Arrays.equals(armorContents, kit.armorContents) &&
                Objects.equals(permission, kit.permission) &&
                Objects.equals(commands, kit.commands);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, encodedInventoryContents, encodedArmorContents, cooldown, oneTimeUse, permission, commands);
        result = 31 * result + Arrays.hashCode(inventoryContents);
        result = 31 * result + Arrays.hashCode(armorContents);
        return result;
    }

    @Override
    public String toString() {
        return "Kit{" +
                "name='" + name + '\'' +
                ", encodedInventoryContents='" + encodedInventoryContents + '\'' +
                ", encodedArmorContents='" + encodedArmorContents + '\'' +
                ", inventoryContents=" + Arrays.toString(inventoryContents) +
                ", armorContents=" + Arrays.toString(armorContents) +
                ", cooldown=" + cooldown +
                ", oneTimeUse=" + oneTimeUse +
                ", permission='" + permission + '\'' +
                ", commands=" + commands +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getEncodedInventoryContents() {
        return encodedInventoryContents;
    }

    public String getEncodedArmorContents() {
        return encodedArmorContents;
    }
}
