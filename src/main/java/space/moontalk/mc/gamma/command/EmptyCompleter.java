package space.moontalk.mc.gamma.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, 
        @NotNull Command       command, 
        @NotNull String        label, 
        @NotNull String[]      args
    ) {
        return Collections.emptyList();
    }
}
