package me.thezombiepl.plugin.zguard.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import me.thezombiepl.plugin.zguard.listeners.AbstractVpnListener;
import me.thezombiepl.plugin.zguard.velocity.ZGuardVelocity;

import me.thezombiepl.plugin.zcore.messages.MessageManager;

import java.net.InetAddress;

public final class VelocityPreLoginListener extends AbstractVpnListener {

    private final ZGuardVelocity plugin;
    private PreLoginEvent event;
    private String ip;
    private String username;

    public VelocityPreLoginListener(ZGuardVelocity plugin) {
        super(plugin.getVpnService(), plugin.getVpnDecision());
        this.plugin = plugin;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        this.event = event;
        this.ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        this.username = event.getUsername();

        checkVpn();
    }

    @Override
    protected void kickPlayer(String message) {
		plugin.getPendingVpn().add(ip);

		if (isDebugEnabled()) {
			logInfo("[PreLogin] VPN detected, marked IP=" + ip);
		}
	}

    // Skip permissions check on velocity and wait for LoginEvent
    @Override protected boolean isBypassPerm() { return false; }

    @Override protected String getIp() { return ip; }
    @Override protected String getUsername() { return username; }

    @Override
    protected boolean isLoopbackOrLocal() {
        InetAddress addr = event.getConnection().getRemoteAddress().getAddress();
        return addr.isLoopbackAddress() || addr.isSiteLocalAddress();
    }

    @Override protected boolean isAntiVpnEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("settings.anti-vpn", false);
    }

    @Override protected boolean isDebugEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("settings.debug", false);
    }

    @Override protected void logInfo(String msg) { plugin.getLogger().info(msg); }
    @Override protected void logWarn(String msg) { plugin.getLogger().warn(msg); }
    @Override protected void logError(String msg, Throwable t) { plugin.getLogger().error(msg, t); }

    @Override protected String getPrefix() { return plugin.getPrefix(); }
    @Override protected MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }
}