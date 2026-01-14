package me.thezombiepl.plugin.zguard.velocity.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import me.thezombiepl.plugin.zguard.velocity.ZGuardVelocity;

public class VelocityZGuardCommand {

    private final ZGuardVelocity plugin;

    public VelocityZGuardCommand(ZGuardVelocity plugin) {
        this.plugin = plugin;
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> reloadNode = BrigadierCommand.literalArgumentBuilder("reload")
            .requires(source -> source.hasPermission("zguard.admin"))
            .executes(context -> {
                plugin.reloadAll();
                
                String prefix = plugin.getMessageManager().getMessage("prefix", "&f[&2Z&6Guard&f] &r");
                context.getSource().sendMessage(
                    ColorUtil.colorize(prefix + "&aZGuard został przeładowany!")
                );
                return Command.SINGLE_SUCCESS;
            })
            .build();

        LiteralCommandNode<CommandSource> mainNode = BrigadierCommand.literalArgumentBuilder("zguard")
            .requires(source -> source.hasPermission("zguard.admin"))
            .executes(context -> {
                String prefix = plugin.getMessageManager().getMessage("prefix", "&f[&2Z&6Guard&f] &r");
                context.getSource().sendMessage(
                    ColorUtil.colorize(prefix + "&eUżyj: /zguard reload")
                );
                return Command.SINGLE_SUCCESS;
            })
            .then(reloadNode)
            .build();

        return new BrigadierCommand(mainNode);
    }
}