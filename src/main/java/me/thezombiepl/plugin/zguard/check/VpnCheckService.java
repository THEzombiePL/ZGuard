package me.thezombiepl.plugin.zguard.check;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VpnCheckService {

    private String baseUrl;
    private int timeoutMs;
    private long cacheMillis;
    private Map<String, String> headers;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
	private static final Gson GSON = new Gson();

    public VpnCheckService(String baseUrl, int timeoutMs, Duration cacheTtl, Map<String, String> headers) {
        setConfig(baseUrl, timeoutMs, cacheTtl, headers);
    }

    public void setConfig(String baseUrl, int timeoutMs, Duration cacheTtl, Map<String, String> headers) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : (baseUrl + "/");
        this.timeoutMs = timeoutMs;
        this.cacheMillis = cacheTtl.toMillis();
        this.headers = headers;
    }

    public JsonObject checkIp(String ip) throws IOException {
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(ip);
        if (cached != null && (now - cached.savedAt) < cacheMillis) {
            return cached.json;
        }

        String encodedIp = URLEncoder.encode(ip, StandardCharsets.UTF_8.name());
        URI uri = URI.create(baseUrl + encodedIp);

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);

        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "ZGuard/1.0");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) return null;

        String response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            response = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
        }

        if (code != 200) {
            System.err.println("VPN API returned HTTP " + code + ": " + response);
            return null;
        }
		
		JsonObject json = GSON.fromJson(response, JsonObject.class);
        cache.put(ip, new CacheEntry(json, now));
        return json;
    }

    public void clearCache() {
        cache.clear();
    }

    private static class CacheEntry {
        final JsonObject json;
        final long savedAt;

        CacheEntry(JsonObject json, long savedAt) {
            this.json = json;
            this.savedAt = savedAt;
        }
    }
}