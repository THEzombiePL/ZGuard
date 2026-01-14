package me.thezombiepl.plugin.zguard.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import lombok.Getter;
import me.thezombiepl.plugin.zguard.check.VpnCheckService;
import me.thezombiepl.plugin.zguard.check.VpnDecision;
import me.thezombiepl.plugin.zguard.commands.ZGuardCommand;
import me.thezombiepl.plugin.zguard.velocity.listeners.VelocityPreLoginListener;
import me.thezombiepl.plugin.zcore.command.CommandRegistry;
import me.thezombiepl.plugin.zcore.config.ConfigManager;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(
    id = "zguard",
    name = "ZGuard",
    version = "@version@",
    description = "Advanced VPN and proxy protection - Velocity Proxy",
    authors = {"THEzombiePL"},
	url = "https://github.com/THEzombiePL/ZGuard",
	dependencies = {
        @Dependency(id = "zcore")
    }
)

public class ZGuardVelocity implements ZGuardCommand.ReloadablePlugin {
	@Getter private static ZGuardVelocity instance;

	@Getter @Inject private Logger logger;
    @Inject @DataDirectory private Path dataDirectory;
    @Getter @Inject private ProxyServer server;

    @Getter private ConfigManager configManager;
    @Getter private MessageManager messageManager;
    @Getter private VpnCheckService vpnService;
    @Getter private VpnDecision vpnDecision;
    @Getter private String prefix;

	@Getter private String version;
	@Getter private String author;
	@Getter private String website;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        try {
            configManager = new ConfigManager(dataDirectory.toFile(), "config.yml", getClass().getClassLoader().getResourceAsStream("config.yml"));
        } catch (IOException e) {
            logger.error("Nie udało się wczytać config.yml!", e);
            return;
        }

		try {
			messageManager = new MessageManager(
				dataDirectory.toFile(), 
				configManager, 
				"pl",
				fileName -> getClass().getClassLoader().getResourceAsStream("messages/" + fileName)
			);
		} catch (IOException e) {
			logger.error("Nie udało się wczytać plików wiadomości!", e);
			return;
		}

		Plugin pluginInfo = this.getClass().getAnnotation(Plugin.class);
		this.version = pluginInfo.version();
		this.author = String.join(", ", pluginInfo.authors());
		this.website = pluginInfo.url();

        loadPrefix();

        List<String> blockedKeys = configManager.getConfig().getStringList("settings.blocked-checks");
        if (blockedKeys.isEmpty()) {
            blockedKeys = Arrays.asList("vpn", "proxy", "hosting", "is_vpn", "is_proxy");
        }

        vpnDecision = new VpnDecision(blockedKeys);
        vpnService = createVpnServiceFromConfig();

        server.getEventManager().register(this, new VelocityPreLoginListener(this, server));
		
		if (configManager.getConfig().getBoolean("settings.register-commands", true)) {
			ZGuardCommand command = new ZGuardCommand(this, messageManager);
			CommandRegistry.register(this, command);
		} else {
			logger.info("Rejestracja komend została wyłączona w config.yml");
		}

        logger.info("ZGuard Velocity włączony!");
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
            logger.info("Konfiguracja i wiadomości zostały przeładowane!");
        } catch (IOException e) {
            logger.error("Błąd przy przeładowaniu: " + e.getMessage(), e);
        }
    }

    private Map<String, String> getHeadersFromConfig() {
        Map<String, String> headers = new HashMap<>();
        dev.dejvokep.boostedyaml.block.implementation.Section section = configManager.getConfig().getSection("api.headers");
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
        logger.info(message);
    }
    
    @Override
    public void logWarning(String message) {
        logger.warn(message);
    }
    
    @Override
    public void logSevere(String message) {
        logger.error(message);
    }
    
    @Override
    public void logDebug(String message) {
        if (isDebugEnabled()) {
            logger.info("[DEBUG] {}", message);
        }
    }
    
    @Override
    public boolean isDebugEnabled() {
        return configManager != null && configManager.getConfig().getBoolean("settings.debug", false);
    }
}
