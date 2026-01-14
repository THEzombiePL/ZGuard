package me.thezombiepl.plugin.zguard.check;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public final class VpnDecision {

    private List<String> blockedKeys;

    public VpnDecision(List<String> blockedKeys) {
        this.blockedKeys = blockedKeys;
    }

    public void setBlockedKeys(List<String> blockedKeys) {
        this.blockedKeys = blockedKeys;
    }

    public boolean isBlocked(JsonObject json) {
        if (json == null || blockedKeys == null) return false;

        for (String key : blockedKeys) {
            if (checkKey(json, key)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkKey(JsonObject json, String path) {
        String[] parts = path.split("\\.");
        JsonElement current = json;

        for (int i = 0; i < parts.length; i++) {
            if (current == null || !current.isJsonObject()) return false;

            JsonObject obj = current.getAsJsonObject();
            if (!obj.has(parts[i])) return false;

            current = obj.get(parts[i]);
        }

        if (!current.isJsonPrimitive()) return false;

        try {
            return current.getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }
}
