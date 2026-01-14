package me.thezombiepl.plugin.zguard.listeners;

import me.thezombiepl.plugin.zguard.ZGuardPlugin;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class PreLoginListener extends AbstractVpnListener implements Listener {

    private final ZGuardPlugin plugin;
    private Permission perms;

    private String ip;
    private String username;
	private AsyncPlayerPreLoginEvent event;

    public PreLoginListener(ZGuardPlugin plugin) {
        super(plugin.getVpnService(), plugin.getVpnDecision());
        this.plugin = plugin;

        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) perms = rsp.getProvider();
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        this.ip = event.getAddress().getHostAddress();
		this.event = event; 
        this.username = event.getName();
        checkVpn();
    }

    @Override
    protected String getIp() { return ip; }
    @Override
    protected String getUsername() { return username; }
    @Override
	protected boolean isLoopbackOrLocal() {
		try {
			java.net.InetAddress address = java.net.InetAddress.getByName(ip);
			return address.isLoopbackAddress() || address.isSiteLocalAddress();
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void kickPlayer(String message) {
		String colored = me.thezombiepl.plugin.zcore.utils.ColorUtil.serialize(
			me.thezombiepl.plugin.zcore.utils.ColorUtil.colorize(message)
		);
		event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, colored);
	}
	@Override
    protected MessageManager getMessageManager() { return plugin.getMessageManager(); }
    @Override
    protected String getPrefix() { return plugin.getPrefix(); }
    @SuppressWarnings("deprecation")
	@Override
    protected boolean isBypassPerm() {
        if (perms == null) return false;
        OfflinePlayer offline = Bukkit.getOfflinePlayer(username);
        return perms.playerHas(null, offline, "zauth.vpn.bypass");
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
    protected void logError(String message, Throwable t) { plugin.getLogger().severe(message); t.printStackTrace(); }
}
