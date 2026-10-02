package com.lotus.members;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final long POLL_MS = 3500;

    private SwipeRefreshLayout swipe;
    private TextView statusChip, bannerKicker, bannerTitle, bannerSub;
    private TextView addressText, addressMeta;
    private TextView metricPlayers, metricUptime, metricRoster, metricInstance;
    private TextView onlineCount, rosterCount;
    private LinearLayout onlineList, rosterList;
    private MaterialButton btnJoin, btnStart, btnCopy, btnShare;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private String lastJson = "";
    private String currentHost = "lotus.taspmail.online";
    private int currentPort = 19132;
    private String serverStatus = "unknown";
    private boolean canStart = false;
    private long uptimeBaseSec = -1;
    private long uptimeBaseAt = 0;

    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            fetchStatus(false);
            handler.postDelayed(this, POLL_MS);
        }
    };

    private final Runnable tickUptime = new Runnable() {
        @Override public void run() {
            if (uptimeBaseSec >= 0) {
                long elapsed = (System.currentTimeMillis() - uptimeBaseAt) / 1000;
                metricUptime.setText(formatUptime(uptimeBaseSec + elapsed));
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = findViewById(R.id.swipe);
        statusChip = findViewById(R.id.statusChip);
        bannerKicker = findViewById(R.id.bannerKicker);
        bannerTitle = findViewById(R.id.bannerTitle);
        bannerSub = findViewById(R.id.bannerSub);
        addressText = findViewById(R.id.addressText);
        addressMeta = findViewById(R.id.addressMeta);
        metricPlayers = findViewById(R.id.metricPlayers);
        metricUptime = findViewById(R.id.metricUptime);
        metricRoster = findViewById(R.id.metricRoster);
        metricInstance = findViewById(R.id.metricInstance);
        onlineCount = findViewById(R.id.onlineCount);
        rosterCount = findViewById(R.id.rosterCount);
        onlineList = findViewById(R.id.onlineList);
        rosterList = findViewById(R.id.rosterList);
        btnJoin = findViewById(R.id.btnJoinMinecraft);
        btnStart = findViewById(R.id.btnStart);
        btnCopy = findViewById(R.id.btnCopy);
        btnShare = findViewById(R.id.btnShare);

        addressText.setText(getString(R.string.default_host));
        addressMeta.setText("Port " + getString(R.string.default_port));

        swipe.setColorSchemeColors(Color.parseColor("#0A84FF"), Color.parseColor("#30D158"));
        swipe.setOnRefreshListener(() -> fetchStatus(true));

        btnCopy.setOnClickListener(v -> {
            String text = addressText.getText().toString();
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Lotus", text));
            toast("Copied " + text);
            vibrate();
        });

        btnJoin.setOnClickListener(v -> openMinecraftAddServer());
        btnStart.setOnClickListener(v -> startServer());
        btnShare.setOnClickListener(v -> shareJoin());
    }

    @Override protected void onResume() {
        super.onResume();
        fetchStatus(false);
        handler.removeCallbacks(pollRunnable);
        handler.postDelayed(pollRunnable, POLL_MS);
        handler.removeCallbacks(tickUptime);
        handler.post(tickUptime);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollRunnable);
        handler.removeCallbacks(tickUptime);
    }

    private String apiUrl(String extra) {
        String base = getString(R.string.api_base);
        String key = getString(R.string.api_key);
        return base + "?key=" + Uri.encode(key) + "&format=json" + (extra == null ? "" : extra);
    }

    private void fetchStatus(boolean fromSwipe) {
        Request req = new Request.Builder().url(apiUrl(null)).header("Cache-Control", "no-cache").build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (fromSwipe) swipe.setRefreshing(false);
                    if (fromSwipe) toast("Network error");
                });
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (fromSwipe) swipe.setRefreshing(false);
                    if (!response.isSuccessful()) return;
                    // Skip UI work if payload unchanged (smooth, no flicker)
                    if (body.equals(lastJson) && !fromSwipe) return;
                    lastJson = body;
                    applyJson(body);
                });
            }
        });
    }

    private void applyJson(String body) {
        try {
            JSONObject d = new JSONObject(body);
            serverStatus = d.optString("status", "unknown");
            boolean ready = d.optBoolean("ready", false);
            canStart = d.optBoolean("can_start", false);
            String ip = d.optString("ip", getString(R.string.default_host));
            currentPort = d.optInt("port", 19132);
            // Prefer domain for display / join
            currentHost = getString(R.string.default_host);
            addressText.setText(currentHost);
            addressMeta.setText("Port " + currentPort + (ip.isEmpty() ? "" : " · IP " + ip));

            JSONObject players = d.optJSONObject("players");
            int online = players != null ? players.optInt("online", 0) : 0;
            int max = players != null ? players.optInt("max", 10) : 10;
            metricPlayers.setText(online + "/" + max);
            onlineCount.setText(String.valueOf(online));

            long uptimeS = d.optLong("uptime_s", -1);
            if ("running".equals(serverStatus) && uptimeS >= 0) {
                uptimeBaseSec = uptimeS;
                uptimeBaseAt = System.currentTimeMillis();
                metricUptime.setText(formatUptime(uptimeS));
            } else {
                uptimeBaseSec = -1;
                metricUptime.setText("—");
            }

            metricInstance.setText(d.optString("instance_type", "—"));
            JSONArray roster = d.optJSONArray("roster");
            int rosterN = roster != null ? roster.length() : 0;
            metricRoster.setText(String.valueOf(rosterN));
            rosterCount.setText(String.valueOf(rosterN));

            // Status chip + banner
            if ("running".equals(serverStatus) && ready) {
                setChip("● Online", R.drawable.bg_chip_online, R.color.green);
                bannerKicker.setText("READY");
                bannerTitle.setText("Fully joinable");
                bannerTitle.setTextColor(getColor(R.color.green));
                bannerSub.setText("Tap Open Minecraft to add & join");
            } else if ("running".equals(serverStatus)) {
                setChip("● Starting", R.drawable.bg_chip_warn, R.color.orange);
                bannerKicker.setText("LOADING");
                bannerTitle.setText("World preparing");
                bannerTitle.setTextColor(getColor(R.color.orange));
                bannerSub.setText("Hang tight…");
            } else if ("pending".equals(serverStatus)) {
                setChip("● Starting", R.drawable.bg_chip_warn, R.color.orange);
                bannerKicker.setText("STARTING");
                bannerTitle.setText("Booting server");
                bannerTitle.setTextColor(getColor(R.color.orange));
                bannerSub.setText("Usually 1–2 minutes");
            } else {
                setChip("○ Offline", R.drawable.bg_chip_off, R.color.red);
                bannerKicker.setText("OFFLINE");
                bannerTitle.setText("Server sleeping");
                bannerTitle.setTextColor(getColor(R.color.label));
                bannerSub.setText("Start when the group wants to play");
            }

            btnStart.setVisibility(canStart ? View.VISIBLE : View.GONE);
            btnJoin.setEnabled(true);

            // Online names
            Set<String> onlineNames = new HashSet<>();
            onlineList.removeAllViews();
            if (players != null) {
                JSONArray names = players.optJSONArray("names");
                if (names != null && names.length() > 0) {
                    for (int i = 0; i < names.length(); i++) {
                        String n = names.optString(i, "Player");
                        onlineNames.add(n);
                        onlineList.addView(playerRow(n, "Online now", true));
                    }
                } else {
                    onlineList.addView(emptyRow("No one online"));
                }
            } else {
                onlineList.addView(emptyRow("No one online"));
            }

            // Roster
            rosterList.removeAllViews();
            if (roster != null && roster.length() > 0) {
                for (int i = 0; i < Math.min(roster.length(), 40); i++) {
                    JSONObject r = roster.optJSONObject(i);
                    if (r == null) continue;
                    String n = r.optString("name", "?");
                    boolean on = onlineNames.contains(n);
                    String last = r.optString("last_seen", "");
                    if (last.length() > 16) last = last.substring(0, 16).replace('T', ' ');
                    String tag = on ? "Online" : ("Offline · last " + last);
                    rosterList.addView(playerRow(n, tag, on));
                }
            } else {
                rosterList.addView(emptyRow("No members recorded yet"));
            }
        } catch (Exception e) {
            // keep previous UI
        }
    }

    private void setChip(String text, int bg, int colorRes) {
        statusChip.setText(text);
        statusChip.setBackgroundResource(bg);
        statusChip.setTextColor(getColor(colorRes));
    }

    private View playerRow(String name, String tag, boolean online) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_player, rosterList, false);
        TextView avatar = row.findViewById(R.id.avatar);
        TextView nameTv = row.findViewById(R.id.name);
        TextView tagTv = row.findViewById(R.id.tag);
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        avatar.setText(initial);
        nameTv.setText(name);
        tagTv.setText(tag);
        tagTv.setTextColor(getColor(online ? R.color.green : R.color.tertiary));
        return row;
    }

    private View emptyRow(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(getColor(R.color.secondary));
        tv.setTextSize(14);
        tv.setPadding(8, 24, 8, 24);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return tv;
    }

    private void openMinecraftAddServer() {
        String name = getString(R.string.server_display_name);
        String host = currentHost;
        int port = currentPort;
        // Official Bedrock deep link
        String value = name + "|" + host + ":" + port;
        String uri = "minecraft://?addExternalServer=" + Uri.encode(value).replace("%7C", "|");
        // encode carefully: name|host:port — Microsoft docs: name|address:port
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8").replace("+", "%20");
            uri = "minecraft://?addExternalServer=" + encodedName + "|" + host + ":" + port;
        } catch (Exception ignored) {}

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            toast("Opening Minecraft…");
            vibrate();
        } catch (Exception e) {
            // Fallback: copy + launch Minecraft PE package
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Lotus", host));
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.mojang.minecraftpe");
                if (launch != null) {
                    startActivity(launch);
                    toast("Address copied — add server in Minecraft");
                } else {
                    toast("Install Minecraft Bedrock, address copied");
                }
            } catch (Exception e2) {
                toast("Copied " + host + " — open Minecraft manually");
            }
        }
    }

    private void startServer() {
        btnStart.setEnabled(false);
        Request req = new Request.Builder().url(apiUrl("&action=start")).get().build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnStart.setEnabled(true);
                    toast("Could not start");
                });
            }
            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    btnStart.setEnabled(true);
                    toast("Start signal sent");
                    vibrate();
                    lastJson = "";
                    fetchStatus(false);
                });
            }
        });
    }

    private void shareJoin() {
        String link = "minecraft://?addExternalServer=Lotus|" + currentHost + ":" + currentPort;
        String text = "Join Lotus Minecraft Bedrock\n"
                + currentHost + ":" + currentPort + "\n"
                + "Or open: " + link + "\n"
                + "Members portal: " + getString(R.string.api_base) + "?key=" + getString(R.string.api_key);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, "Share Lotus"));
    }

    private static String formatUptime(long s) {
        if (s < 0) return "—";
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m " + sec + "s";
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } catch (Exception ignored) {}
    }
}
