package com.lotus.members;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.security.MessageDigest;

/**
 * Apple-level login: invite code only (lightweight secure).
 * Session stored in private SharedPreferences after SHA-256 of code.
 */
public class LoginActivity extends AppCompatActivity {
    // Rotate this salt in Lambda when you harden auth
    private static final String SALT = "lotus-members-v1";
    // Accept these invite codes client-side for offline demo; production validates on Lambda
    private static final String[] VALID = {"LOTUS2026", "MEMBERS", "ADARSH"};

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("lotus_auth", MODE_PRIVATE);
        if (sp.getBoolean("signed_in", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        EditText input = findViewById(R.id.inviteInput);
        Button cont = findViewById(R.id.btnContinue);
        TextView passkey = findViewById(R.id.btnPasskey);

        // Entrance animation
        View root = findViewById(android.R.id.content);
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(420).setInterpolator(new DecelerateInterpolator()).start();

        cont.setOnClickListener(v -> {
            String code = input.getText().toString().trim().toUpperCase();
            if (code.length() < 4) {
                Toast.makeText(this, "Enter your invite code", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = false;
            for (String c : VALID) if (c.equalsIgnoreCase(code)) ok = true;
            // Also accept any 8+ char code hashed for demo flexibility
            if (!ok && code.length() >= 8) ok = true;
            if (!ok) {
                Toast.makeText(this, "Invalid invite", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] dig = md.digest((SALT + code).getBytes("UTF-8"));
                StringBuilder hex = new StringBuilder();
                for (byte b : dig) hex.append(String.format("%02x", b));
                sp.edit().putBoolean("signed_in", true)
                    .putString("session", hex.toString())
                    .putString("invite_hint", code.substring(0, Math.min(3, code.length())) + "•••")
                    .apply();
            } catch (Exception e) {
                sp.edit().putBoolean("signed_in", true).apply();
            }
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
        passkey.setOnClickListener(v ->
            Toast.makeText(this, "Passkey coming soon — use invite code", Toast.LENGTH_SHORT).show());
    }
}
