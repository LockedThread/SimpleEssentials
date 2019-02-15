package rip.simpleness.simpleessentials.task;

import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import rip.simpleness.simpleessentials.SimpleEssentials;

public class TaskClearEntities extends BukkitRunnable {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();
    private int revolution;

    public TaskClearEntities() {
        this.revolution = 15;
    }

    @Override
    public void run() {
        revolution--;
        if (revolution == 10 || revolution == 5 || revolution == 4 || revolution == 3 || revolution == 2 || revolution == 1) {
            Bukkit.broadcastMessage(Text.colorize("&c&lClearLag&8» &cYou have " + revolution + " minute" + plural(revolution) + " remaining until the clearing of ground entities!"));
        } else if (revolution == 0) {
            int entities = 0;
            for (World world : INSTANCE.getServer().getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!entity.isCustomNameVisible() && !(entity instanceof Player) && !(entity instanceof ArmorStand)) {
                        if (entity instanceof Item) {
                            Item item = (Item) entity;
                            if (item.getItemStack().getType() == Material.MOB_SPAWNER || item.getItemStack().getType() == Material.BEACON) {
                                continue;
                            }
                        }
                        entity.remove();
                        entities++;
                    }
                }
            }
            Bukkit.broadcastMessage(Text.colorize("&c&lClearLag&8» &f" + entities + " &centities have been cleared!"));
            revolution = 15;
        }
    }

    private String plural(int i) {
        return i == 1 ? "" : "s";
    }

    public int getRevolution() {
        return revolution;
    }
}
