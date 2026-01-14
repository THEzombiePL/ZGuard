package me.thezombiepl.plugin.zguard;

import lombok.Getter;
import me.thezombiepl.plugin.zguard.check.VpnCheckService;
import me.thezombiepl.plugin.zguard.check.VpnDecision;
import me.thezombiepl.plugin.zguard.commands.ZGuardCommand;
import me.thezombiepl.plugin.zguard.listeners.PreLoginListener;
import me.thezombiepl.plugin.zcore.command.CommandRegistry;
import me.thezombiepl.plugin.zcore.config.ConfigManager;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ZGuardPlugin extends JavaPlugin implements ZGuardCommand.ReloadablePlugin {

    @Getter private static ZGuardPlugin instance;

    @Getter private ConfigManager configManager;
    @Getter private MessageManager messageManager;
    @Getter private VpnCheckService vpnService;
    @Getter private VpnDecision vpnDecision;

	@Getter private String prefix;

    @Override
    public void onEnable() {
        instance = this;

        try {
            configManager = new ConfigManager(this, "config.yml");
        } catch (IOException e) {
            getLogger().severe("Nie udało się wczytać config.yml!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            messageManager = new MessageManager(this, configManager, "pl");
        } catch (IOException e) {
            getLogger().severe("Nie udało się wczytać plików wiadomości!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		loadPrefix();

        List<String> blockedKeys = configManager.getConfig().getStringList("settings.blocked-checks");
        if (blockedKeys.isEmpty()) {
            blockedKeys = Arrays.asList("vpn", "proxy", "hosting", "is_vpn", "is_proxy");
        }

        vpnDecision = new VpnDecision(blockedKeys);
        vpnService = createVpnServiceFromConfig();

        getServer().getPluginManager().registerEvents(new PreLoginListener(this), this);

		if (configManager.getConfig().getBoolean("settings.register-commands", true)) {
			ZGuardCommand command = new ZGuardCommand(this, messageManager);
			CommandRegistry.register(this, command);
		} else {
			getLogger().info("Rejestracja komend została wyłączona w config.yml");
		}

        getLogger().info("ZGuard włączony!");
    }

    private VpnCheckService createVpnServiceFromConfig() {
        String apiUrl = configManager.getConfig().getString("api.url", "https://antivpn.zombiebot.pl/check/");
        int timeout = configManager.getConfig().getInt("api.timeout", 3000);
        Duration cacheTtl = Duration.ofMinutes(configManager.getConfig().getInt("api.cacheTTL", 10));
        Map<String, String> headers = getHeadersFromConfig();
        return new VpnCheckService(apiUrl, timeout, cacheTtl, headers);
    }
	private void loadPrefix() {
        if (messageManager != null) {
            this.prefix = messageManager.getMessage("prefix", "&f[&2Z&cGuard&f] &r");
        } else {
            this.prefix = "&f[&2Z&cGuard&f] &r";
        }
    }
    public void reloadAll() {
        try {
            if (configManager != null) configManager.reload();
            if (messageManager != null) messageManager.reload(configManager);

            if (vpnService != null) {
                vpnService.setConfig(
                        configManager.getConfig().getString("api.url", "https://antivpn.zombiebot.pl/check/"),
                        configManager.getConfig().getInt("api.timeout", 3000),
                        Duration.ofMinutes(configManager.getConfig().getInt("api.cacheTTL", 10)),
                        getHeadersFromConfig()
                );
            }
            if (vpnDecision != null) {
                List<String> keys = configManager.getConfig().getStringList("settings.blocked-checks");
                vpnDecision.setBlockedKeys(keys);
            }
			loadPrefix();
            getLogger().info("Konfiguracja i wiadomości zostały przeładowane!");
        } catch (IOException e) {
            getLogger().severe("Błąd przy przeładowaniu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, String> getHeadersFromConfig() {
        Map<String, String> headers = new HashMap<>();
        Section section = configManager.getConfig().getSection("api.headers");
        if (section != null) {
            for (Object keyObj : section.getKeys()) {
                String key = keyObj.toString();
                String value = section.getString(key);
                if (value != null) headers.put(key, value);
            }
        }
        return headers;
    }

    @Override
    public void logInfo(String message) {
        getLogger().info(message);
    }
    
    @Override
    public void logWarning(String message) {
        getLogger().warning(message);
    }
    
    @Override
    public void logSevere(String message) {
        getLogger().severe(message);
    }
    
    @Override
    public void logDebug(String message) {
        if (isDebugEnabled()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
    
    @Override
    public boolean isDebugEnabled() {
        return configManager != null && configManager.getConfig().getBoolean("settings.debug", false);
    }

	@Override
	public String getVersion() {
		return getDescription().getVersion();
	}

	@Override
	public String getAuthor() {
		return String.join(", ", getDescription().getAuthors());
	}
	@Override
	public String getWebsite() {
		return getDescription().getWebsite();
	}

}