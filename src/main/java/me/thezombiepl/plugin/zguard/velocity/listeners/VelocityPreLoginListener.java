package me.thezombiepl.plugin.zguard.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import me.thezombiepl.plugin.zguard.listeners.AbstractVpnListener;
import me.thezombiepl.plugin.zguard.velocity.ZGuardVelocity;

import java.net.InetAddress;

public final class VelocityPreLoginListener extends AbstractVpnListener {

    private final ZGuardVelocity plugin;
    private final ProxyServer server;
    private String ip;
    private String username;
    private PreLoginEvent event;

    public VelocityPreLoginListener(ZGuardVelocity plugin, ProxyServer server) {
        super(plugin.getVpnService(), plugin.getVpnDecision());
        this.plugin = plugin;
        this.server = server;
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
        InetAddress addr = event.getConnection().getRemoteAddress().getAddress();
        return addr.isLoopbackAddress() || addr.isSiteLocalAddress();
    }

    @Override
    protected void kickPlayer(String message) {
        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                ColorUtil.colorize(message)
        ));
    }

    @Override
    protected MessageManager getMessageManager() { return plugin.getMessageManager(); }

    @Override
    protected String getPrefix() { return plugin.getPrefix(); }

    @Override
    protected boolean isBypassPerm() {
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            
            if (server.getPluginManager().getPlugin("luckperms").isPresent()) {
                net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
                net.luckperms.api.model.user.User user = null;
                
                // 1.19+
                java.util.UUID uuid = event.getUniqueId();
                
                if (uuid != null) {
                    user = lp.getUserManager().getUser(uuid);
                    if (user == null) {
                        user = lp.getUserManager().loadUser(uuid).join();
                    }
                } else {
                    user = lp.getUserManager().getUser(username);
                    if (user == null) {
                        java.util.UUID foundUuid = lp.getUserManager().lookupUniqueId(username).join();
                        if (foundUuid != null) {
                            user = lp.getUserManager().loadUser(foundUuid).join();
                        }
                    }
                }

                if (user != null) {
                    return user.getCachedData().getPermissionData().checkPermission("zguard.bypass").asBoolean();
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            logError("Error while checking LuckPerms bypass (Velocity)", e);
        }
        return false;
    }

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