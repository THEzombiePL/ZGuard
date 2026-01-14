package me.thezombiepl.plugin.zguard.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;

import me.thezombiepl.plugin.zguard.listeners.AbstractVpnListener;
import me.thezombiepl.plugin.zguard.velocity.ZGuardVelocity;
import me.thezombiepl.plugin.zcore.messages.MessageManager;

public final class VelocityPreLoginListener extends AbstractVpnListener {

    private final ZGuardVelocity plugin;
    private String ip;
    private String username;
    private PreLoginEvent event;

    public VelocityPreLoginListener(ZGuardVelocity plugin, ProxyServer server) {
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
    protected String getIp() { return ip; }
    @Override
    protected String getUsername() { return username; }
    @Override
    protected boolean isLoopbackOrLocal() {
        return event.getConnection().getRemoteAddress().getAddress().isLoopbackAddress()
                || event.getConnection().getRemoteAddress().getAddress().isSiteLocalAddress();
    }
    @Override
    protected void kickPlayer(String message) {
        event.setResult(com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult.denied(
                me.thezombiepl.plugin.zcore.utils.ColorUtil.colorize(message)
        ));
    }
    @Override
    protected MessageManager getMessageManager() { return plugin.getMessageManager(); }
    @Override
    protected String getPrefix() { return plugin.getPrefix(); }
    @Override
    protected boolean isBypassPerm() { return false; }
    @Override
    protected boolean isAntiVpnEnabled() { return plugin.getConfigManager().getConfig().getBoolean("settings.anti-vpn", false); }
    @Override
    protected boolean isDebugEnabled() { return plugin.getConfigManager().getConfig().getBoolean("settings.debug", false); }
    @Override
    protected void logInfo(String message) { plugin.getLogger().info(message); }
    @Override
    protected void logWarn(String message) { plugin.getLogger().warn(message); }
    @Override
    protected void logError(String message, Throwable t) { plugin.getLogger().error(message, t); }
}
