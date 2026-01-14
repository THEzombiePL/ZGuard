package me.thezombiepl.plugin.zguard.listeners;

import me.thezombiepl.plugin.zguard.ZGuardPlugin;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.net.InetAddress;
import java.util.UUID;

public final class PreLoginListener extends AbstractVpnListener implements Listener {

    private final ZGuardPlugin plugin;
    private String ip;
    private String username;
    private UUID uuid;
    private AsyncPlayerPreLoginEvent event;

    public PreLoginListener(ZGuardPlugin plugin) {
        super(plugin.getVpnService(), plugin.getVpnDecision());
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        this.event = event;
        this.ip = event.getAddress().getHostAddress();
        this.username = event.getName();
        this.uuid = event.getUniqueId();
        checkVpn();
    }

    @Override
    protected String getIp() { return ip; }

    @Override
    protected String getUsername() { return username; }

    @Override
    protected boolean isLoopbackOrLocal() {
        try {
            InetAddress address = event.getAddress();
            return address.isLoopbackAddress() || address.isSiteLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void kickPlayer(String message) {
        String colored = ColorUtil.serialize(ColorUtil.colorize(message));
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, colored);
    }

    @Override
    protected MessageManager getMessageManager() { return plugin.getMessageManager(); }

    @Override
    protected String getPrefix() { return plugin.getPrefix(); }

    @Override
    protected boolean isBypassPerm() {
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
                net.luckperms.api.model.user.User user = lp.getUserManager().getUser(uuid);
                
                if (user == null) {
                    user = lp.getUserManager().loadUser(uuid).join();
                }

                if (user != null) {
                    return user.getCachedData().getPermissionData().checkPermission("zguard.bypass").asBoolean();
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            logError("Error while checking LuckPerms bypass", e);
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
    protected void logWarn(String message) { plugin.getLogger().warning(message); }

    @Override
    protected void logError(String message, Throwable t) { 
        plugin.getLogger().severe(message); 
        if (isDebugEnabled()) t.printStackTrace(); 
    }
}