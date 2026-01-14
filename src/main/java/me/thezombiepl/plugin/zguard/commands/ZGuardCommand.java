package me.thezombiepl.plugin.zguard.commands;

import me.thezombiepl.plugin.zcore.command.CommandContext;
import me.thezombiepl.plugin.zcore.command.CommandHandler;
import me.thezombiepl.plugin.zcore.config.ConfigManager;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import static me.thezombiepl.plugin.zcore.command.CommandHandler.SubCommands.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main ZGuard command with subcommand support.
 * Works on both Paper and Velocity platforms.
 */
public class ZGuardCommand extends CommandHandler {
    
    // Message path constants
    private static final String MSG_NO_PERMISSION = "messages.no-permission";
    private static final String MSG_UNKNOWN = "messages.unknown-subcommand";
    private static final String MSG_USAGE = "messages.command-usage";
    private static final String MSG_RELOAD_SUCCESS = "messages.reload-success";
    private static final String MSG_RELOAD_ERROR = "messages.reload-error";
    
    private final ReloadablePlugin plugin;
    
    public ZGuardCommand(ReloadablePlugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        setMessageManager(messageManager);
        
        // Register subcommands with clean syntax
        registerSubCommand("reload", withPermission("zguard.admin", simple(this::handleReload)));
        registerSubCommand("help", simple(this::handleHelp));
        registerSubCommand("info", simple(this::handleInfo));
    }
    
    @Override
    public String getName() {
        return "zguard";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"zg"};
    }
    
    @Override
    public String getPermission() {
        return "zguard.admin";
    }
    
    @Override
    public String getDescription() {
        return "Main ZGuard command";
    }
    
    /* =========================
       Helper Methods
       ========================= */
    
    /**
     * Gets the plugin prefix from MessageManager
     */
    private String prefix() {
        return msg("prefix", "&f[&2Z&6Guard&f] &r");
    }
    
    /**
     * Shorthand for getting messages
     */
    private String msg(String path, String def) {
        return getMessageManager().getMessage(path, def);
    }
    
    
    /* =========================
    Command Handlers
    ========================= */
    
    @Override
    protected boolean onNoArgs(CommandContext context) {
        String usage = msg(MSG_USAGE, "&eUsage: /zguard <reload|help|info>");
        context.sendMessage(prefix() + usage);
        return true;
    }
    
    private boolean handleReload(CommandContext context) {
        try {
            plugin.reloadAll();
            
            String success = msg(MSG_RELOAD_SUCCESS, "&aZGuard has been reloaded!");
            context.sendMessage(prefix() + success);
        } catch (Exception e) {
            String error = msg(MSG_RELOAD_ERROR, "&cError while reloading!");
            context.sendMessage(prefix() + error);
            
            // Better logging instead of printStackTrace
            plugin.logSevere("Error while reloading ZGuard: " + e.getMessage());
            if (e.getCause() != null) {
                plugin.logSevere("Caused by: " + e.getCause().getMessage());
            }
        }
        
        return true;
    }
    
    private boolean handleHelp(CommandContext context) {
		sendList(
			context,
			"messages.help",
			Arrays.asList(
				"&e&lZGuard &8- &fLista komend:",
				"&7/zguard reload",
				"&7/zguard help",
				"&7/zguard info"
			)
		);
		return true;
	}

    
	private boolean handleInfo(CommandContext context) {
		boolean enabled = plugin.getConfigManager()
			.getConfig().getBoolean("settings.anti-vpn", true);

		String protection = enabled
			? msg("messages.protection-enabled", "&aWłączona")
			: msg("messages.protection-disabled", "&cWyłączona");

		Map<String, String> ph = new HashMap<>();
		ph.put("version", plugin.getVersion());
		ph.put("platform", context.getPlatform().toString());
		ph.put("author", plugin.getAuthor());
		ph.put("website", plugin.getWebsite());
		ph.put("protection", protection);

		sendList(
			context,
			"messages.info",
			Arrays.asList(
                "&e&lZGuard &7v{version}",
                "&7Platforma: &f{platform}",
                "&7Autor: &f{author}",
                "&7Strona: &f{website}",
                "&7Ochrona: &f{protection}"
            ),
			ph
		);
		return true;
	}



    private void sendList(CommandContext context, String key, List<String> def) {
		for (String line : getMessageManager().getMessageList(key, def)) {
			context.sendMessage(prefix() + line);
		}
	}

	private void sendList(CommandContext context, String key, List<String> def, Map<String, String> placeholders) {
		for (String line : getMessageManager().getMessageList(key, def, placeholders)) {
			context.sendMessage(prefix() + line);
		}
	}

    /* =========================
       Message Overrides
       ========================= */
    
    @Override
    protected String getNoPermissionMessage(CommandContext context) {
        return prefix() + msg(MSG_NO_PERMISSION, "&cYou don't have permission!");
    }
    
    @Override
    protected String getUnknownSubCommandMessage(CommandContext context) {
        return prefix() + msg(MSG_UNKNOWN, "&eUnknown subcommand. Use: /zguard help");
    }
    
    /* =========================
    	Plugin Interface
       ========================= */
    
    /**
     * Interface for reloadable plugins (avoids reflection)
     */
    public interface ReloadablePlugin {
        void reloadAll();
		void logInfo(String message);
		void logWarning(String message);
		void logSevere(String message);
		void logDebug(String message);
		
		boolean isDebugEnabled();
		String getVersion();
        String getAuthor();
        String getWebsite();
        ConfigManager getConfigManager();
    }
}