package me.thezombiepl.plugin.zguard.listeners;

import com.google.gson.JsonObject;
import me.thezombiepl.plugin.zcore.messages.MessageManager;
import me.thezombiepl.plugin.zguard.check.VpnCheckService;
import me.thezombiepl.plugin.zguard.check.VpnDecision;

import java.util.HashMap;
import java.util.Map;

/**
 * Base logic for VPN checks.
 */
public abstract class AbstractVpnListener {

    protected final VpnCheckService vpnService;
    protected final VpnDecision vpnDecision;

    public AbstractVpnListener(VpnCheckService vpnService, VpnDecision vpnDecision) {
        this.vpnService = vpnService;
        this.vpnDecision = vpnDecision;
    }

    protected abstract String getIp();
    protected abstract String getUsername();
    protected abstract boolean isLoopbackOrLocal();
    protected abstract void kickPlayer(String message);
    protected abstract MessageManager getMessageManager();
    protected abstract String getPrefix();
    protected abstract boolean isBypassPerm();
    protected abstract boolean isAntiVpnEnabled();
    protected abstract boolean isDebugEnabled();
    protected abstract void logInfo(String message);
    protected abstract void logWarn(String message);
    protected abstract void logError(String message, Throwable t);

    protected void checkVpn() {
        final String ip = getIp();
        final String username = getUsername();

        if (!isAntiVpnEnabled()) return;
        if (isLoopbackOrLocal()) {
            if (isDebugEnabled()) logInfo("Skipping VPN check for local/internal IP: " + username + " (" + ip + ")");
            return;
        }
        if (isBypassPerm()) return;

        long startTime = System.nanoTime();
        try {
            JsonObject json = vpnService.checkIp(ip);
            long duration = System.nanoTime() - startTime;

            if (isDebugEnabled()) logInfo("VPN check for " + username + " (" + ip + ") took " + formatDuration(duration));

            if (vpnDecision.isBlocked(json)) {
                MessageManager messages = getMessageManager();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", username);
                placeholders.put("ip", ip);

                String rawMsg = getPrefix() + messages.getMessage(
                        "vpn.kickMessage",
                        "Nie możesz dołączyć – wykryto VPN/Proxy.",
                        placeholders
                );

                kickPlayer(rawMsg);
                logInfo("Blocked connection from " + username + " (" + ip + ") - VPN/Proxy detected");
            }
        } catch (Exception ex) {
            long duration = System.nanoTime() - startTime;
            logWarn("VPN API check failed for " + username + " (" + ip + ") after " + formatDuration(duration) + ": " + ex.getMessage());
        }
    }

    private String formatDuration(long durationNs) {
        if (durationNs < 1_000_000) return String.format("%.1f μs", durationNs / 1_000.0);
        else if (durationNs < 1_000_000_000) return String.format("%.2f ms", durationNs / 1_000_000.0);
        else return String.format("%.2f s", durationNs / 1_000_000_000.0);
    }
}
