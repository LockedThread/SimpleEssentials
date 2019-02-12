package rip.simpleness.simpleessentials.modules;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.simpleness.simpleessentials.SimpleEssentials;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ModuleTeleportation implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    private HashBiMap<UUID, UUID> teleportationRequests;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.teleportationRequests = HashBiMap.create();

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        Player target = commandContext.arg(0).parseOrFail(Player.class);
                        teleportationRequests.put(commandContext.sender().getUniqueId(), target.getUniqueId());
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have requested to teleport to " + target.getName());
                        target.sendMessage(Text.colorize(INSTANCE.getServerPrefix() + "&eYou have been requested to be tp'd to by " + commandContext.sender().getName() + "\n" +
                                INSTANCE.getServerPrefix() + "&eDo /tpaccept to accept the tpa."));
                    } else {
                        commandContext.reply("&e/tpa [player]");
                    }
                }).registerAndBind(terminableConsumer, "tpa");

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        Player target = commandContext.arg(0).parseOrFail(Player.class);
                        teleportationRequests.put(target.getUniqueId(), commandContext.sender().getUniqueId());
                        commandContext.reply(INSTANCE.getServerPrefix() + "&eYou have requested for" + target.getName() + "to teleport to you ");
                        target.sendMessage(Text.colorize(INSTANCE.getServerPrefix() + "&eYou have been requested to be tp'd to " + commandContext.sender().getName() + "\n" +
                                INSTANCE.getServerPrefix() + "&eDo /tpaccept to accept the tpahere."));
                    } else {
                        commandContext.reply("&e/tpahere [player]");
                    }
                }).registerAndBind(terminableConsumer, "tpahere");

        Commands.create()
                .assertPlayer()
                .handler(commandContext -> {
                    BiMap<UUID, UUID> inverse = teleportationRequests.inverse();
                    UUID uuid = inverse.get(commandContext.sender().getUniqueId());
                    if (uuid == null) {
                        commandContext.reply("&cYou don't have any pending teleport requests.");
                    } else {
                        Player target = INSTANCE.getServer().getPlayer(uuid);
                        target.teleport(commandContext.sender(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        commandContext.reply("&eYou have been teleported to " + target.getName());
                    }
                }).registerAndBind(terminableConsumer, "tpyes", "tpaccept");
    }
}
