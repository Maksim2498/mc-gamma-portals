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
public class RemovePortalsExecutor implements CommandExecutor {
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
            portalManager.removePortals();
            sender.sendMessage(messageProvider.makePortalsRemovedMessage());
        }

        return true;
    }
}
