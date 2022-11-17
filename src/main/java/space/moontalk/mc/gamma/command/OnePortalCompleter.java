package space.moontalk.mc.gamma.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import space.moontalk.mc.gamma.PortalManager;

@Getter
@AllArgsConstructor
public class OnePortalCompleter implements TabCompleter {
    private final @NotNull PortalManager portalManager;

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, 
        @NotNull Command       command, 
        @NotNull String        label, 
        @NotNull String[]      args
    ) {
        return switch (args.length) {
            case 1  -> portalManager.getPortals()
                                    .keySet()
                                    .stream()
                                    .map(n -> n.substring(1, n.length() - 1))
                                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                                    .collect(Collectors.toList());

            default -> Collections.emptyList();
        };
    }
}
