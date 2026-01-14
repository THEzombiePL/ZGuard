package me.thezombiepl.plugin.zguard.velocity.listeners;

import com.google.gson.JsonObject;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.proxy.ProxyServer;
import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import me.thezombiepl.plugin.zguard.check.VpnCheckService;
import me.thezombiepl.plugin.zguard.check.VpnDecision;
import me.thezombiepl.plugin.zguard.velocity.ZGuardVelocity;
import net.kyori.adventure.text.Component;

public class VelocityPreLoginListenerOld {

    private final ZGuardVelocity plugin;
    private final VpnCheckService vpnService;
    private final VpnDecision vpnDecision;

    public VelocityPreLoginListenerOld(ZGuardVelocity plugin, VpnCheckService vpnService, VpnDecision vpnDecision, ProxyServer server) {
        this.plugin = plugin;
        this.vpnService = vpnService;
        this.vpnDecision = vpnDecision;
    }

    @SuppressWarnings("deprecation")
    @Subscribe(order = PostOrder.FIRST)
	public EventTask onPreLogin(PreLoginEvent event) {
		String username = event.getUsername();
		String ipAddress = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

		return EventTask.async(() -> {
			boolean antiVpn = plugin.getConfigManager().getConfig().getBoolean("settings.anti-vpn", false);
			if (!antiVpn) return;
			
			// Skip local/internal addresses
			if (event.getConnection().getRemoteAddress().getAddress().isLoopbackAddress() ||
				event.getConnection().getRemoteAddress().getAddress().isSiteLocalAddress()) {

				if (plugin.getConfigManager().getConfig().getBoolean("settings.debug", false)) {
					plugin.getLogger().info("Skipping VPN check for local/internal IP: " + username + " (" + ipAddress + ")");
				}
				return;
			}

			try {
				JsonObject response = vpnService.checkIp(ipAddress);

				if (response == null) {
					if (plugin.getConfigManager().getConfig().getBoolean("settings.debug", false)) {
						plugin.getLogger().warn("VPN API returned null for " + username);
					}
					return;
				}

				if (vpnDecision.isBlocked(response)) {
					String rawReason = plugin.getMessageManager().getMessage(
							"messages.kick-message",
							"&cZakazane jest łączenie się z serwerem przez VPN lub proxy!"
					);

					Component kickReason = ColorUtil.colorize(rawReason);
					event.setResult(PreLoginComponentResult.denied(kickReason));

					plugin.getLogger().info("Blocked connection from " + username + " (" + ipAddress + ") - VPN/Proxy detected");
				}

			} catch (Exception e) {
				plugin.getLogger().error("Error checking VPN for " + username + " (" + ipAddress + ")", e);
			}
		});
	}
}
