package space.moontalk.mc.gamma.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import space.moontalk.mc.gamma.PortalManager;

@Getter
@AllArgsConstructor
public class ListPortalsExecutor implements CommandExecutor {
    private final @NotNull PortalManager portalManager;
    
    @Override
    public boolean onCommand(
        @NotNull CommandSender sender, 
        @NotNull Command       command, 
        @NotNull String        label,
        @NotNull String[]      args
    ) {
        if (args.length != 0)
            return false;

        val messageProvider = portalManager.getMessageProvider();

        if (portalManager.noPortals()) 
            sender.sendMessage(messageProvider.makeNoPortalsMessage());
        else {
            val portalMap = portalManager.getPortals();

            sender.sendMessage(messageProvider.makePortalListHeaderMessage(portalMap));

            for (val entry : portalMap.entrySet()) {
                val portals = entry.getValue();

                if (portals.isEmpty())
                    continue;

                val name = entry.getKey();

                sender.sendMessage(messageProvider.makePortalListItemMessage(name.substring(1, name.length() - 1), portals));
            }
        }

        return true;
    }
}
