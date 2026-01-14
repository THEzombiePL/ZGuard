package me.thezombiepl.plugin.zguard.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import me.thezombiepl.plugin.zguard.velocity.ZGuardVelocity;

import java.util.HashMap;
import java.util.Map;

public final class VelocityLoginListener {

    private final ZGuardVelocity plugin;

    public VelocityLoginListener(ZGuardVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();

        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[Login] Player=" + player.getUsername()
                + " UUID=" + player.getUniqueId()
                + " IP=" + ip);
        }

        if (!plugin.getPendingVpn().remove(ip)) {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Login] No pending VPN entry -> allow");
            }
            return;
        }

        boolean hasBypass = player.hasPermission("zguard.bypass");
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[Login] zguard.bypass = " + hasBypass);
        }

        if (hasBypass) {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Login] Bypass granted -> allow");
            }
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getUsername());
        placeholders.put("ip", ip);

        String rawMsg = plugin.getPrefix() + plugin.getMessageManager().getMessage(
            "vpn.kickMessage",
            "&cYou cannot join – VPN/Proxy detected.\n&7IP: &f{ip}",
            placeholders
        );

        event.setResult(LoginEvent.ComponentResult.denied(
            ColorUtil.colorize(rawMsg)
        ));

        plugin.getLogger().info(
            "[Login] Blocked connection from "
            + player.getUsername() + " (" + ip + ") - VPN/Proxy detected"
        );
    }
}