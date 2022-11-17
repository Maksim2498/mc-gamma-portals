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
public class FixPortalExecutor implements CommandExecutor {
    private final @NotNull PortalManager portalManager;

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender, 
        @NotNull Command       command, 
        @NotNull String        label,
        @NotNull String[]      args
    ) {
        if (args.length != 1)
            return false;

        val portal          = args[0];
        val messageProvider = portalManager.getMessageProvider();

        sender.sendMessage(portalManager.fixPortal(portal) ? messageProvider.makePortalFixedMessage(portal) 
                                                           : messageProvider.makePortalNotFoundMessage(portal));

        return true;
    }
}
