package rip.simpleness.simpleessentials.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkUnloadEvent;
import rip.simpleness.simpleessentials.SimpleEssentials;
import rip.simpleness.simpleessentials.task.TaskClearEntities;

import javax.annotation.Nonnull;

public class ModuleClearLag implements TerminableModule {

    private TaskClearEntities taskClearEntities;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.taskClearEntities = new TaskClearEntities();
        Commands.create()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        if (commandContext.sender().hasPermission("simpleness.lag")) {
                            String root = commandContext.arg(0).parseOrFail(String.class);
                            switch (root.toLowerCase()) {
                                case "killmobs":
                                    int mobCount = 0;
                                    for (World world : SimpleEssentials.getInstance().getServer().getWorlds()) {
                                        for (Entity entity : world.getEntities()) {
                                            if (!entity.isCustomNameVisible() && !(entity instanceof Player) && !(entity instanceof Item) && !(entity instanceof ArmorStand)) {
                                                entity.remove();
                                                mobCount++;
                                            }
                                        }
                                    }
                                    commandContext.reply(SimpleEssentials.getInstance().getServerPrefix() + "&eYou have cleared &f" + mobCount + " &eentities");
                                    return;
                                case "unloadchunks":
                                    int chunkCount = 0;
                                    for (World world : SimpleEssentials.getInstance().getServer().getWorlds()) {
                                        for (Chunk loadedChunk : world.getLoadedChunks()) {
                                            ChunkUnloadEvent event = new ChunkUnloadEvent(loadedChunk);
                                            SimpleEssentials.getInstance().getServer().getPluginManager().callEvent(event);
                                            if (!event.isCancelled() && loadedChunk.unload(true, true)) {
                                                chunkCount++;
                                            }
                                        }
                                    }
                                    commandContext.reply(SimpleEssentials.getInstance().getServerPrefix() + "&eYou have unloaded &f" + chunkCount + " &echunks");
                                    return;
                                case "clear":
                                    int itemCount = 0;
                                    for (World world : SimpleEssentials.getInstance().getServer().getWorlds()) {
                                        for (Entity entity : world.getEntities()) {
                                            if (!entity.isCustomNameVisible() && entity instanceof Item) {
                                                entity.remove();
                                                itemCount++;
                                            }
                                        }
                                    }
                                    commandContext.reply(SimpleEssentials.getInstance().getServerPrefix() + "&eYou have cleared &f" + itemCount + " &eitems");
                                    return;
                            }
                        }
                    }
                    if (commandContext.sender().hasPermission("simpleness.lag")) {
                        commandContext.reply(" ");
                        commandContext.reply("&dClearLag:");
                        commandContext.reply("&f/lag killmobs &d- Kills all mobs!");
                        commandContext.reply("&f/lag clear &d- Clears all ground items!");
                        commandContext.reply("&f/lag unloadchunks &d- Unloads all chunks");
                        commandContext.reply(" ");
                        commandContext.reply("&dTime until clear lag: &e" + taskClearEntities.getRevolution() + " minutes");
                        commandContext.reply(" ");
                    } else {
                        commandContext.reply(" ");
                        commandContext.reply("&dTime until clear lag: &e" + taskClearEntities.getRevolution() + " minutes");
                        commandContext.reply(" ");
                    }
                }).registerAndBind(terminableConsumer, "lag");
        taskClearEntities.runTaskTimer(SimpleEssentials.getInstance(), 1200, 1200);
    }
}
