package com.lotus.members;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Local-first group chat UI (Apple Messages style). Wire to WebSocket Lambda later. */
public class ChatActivity extends AppCompatActivity {
    private LinearLayout thread;
    private ScrollView scroll;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Build programmatically if layout inflation fails on older resources
        setContentView(R.layout.activity_chat);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // Use a simple vertical list inside RecyclerView parent: fallback to adding views to a container
        View list = findViewById(R.id.chatList);
        // RecyclerView present — add messages via a lightweight approach: replace with LinearLayout host
        ViewGroup parent = (ViewGroup) list.getParent();
        int idx = parent.indexOfChild(list);
        parent.removeViewAt(idx);
        scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        thread = new LinearLayout(this);
        thread.setOrientation(LinearLayout.VERTICAL);
        thread.setPadding(24, 16, 24, 16);
        scroll.addView(thread);
        parent.addView(scroll, idx);

        EditText input = findViewById(R.id.chatInput);
        Button send = findViewById(R.id.btnSend);
        addBubble("Welcome to Lotus Chat", false);
        addBubble("Voice & screen share live on Calls tab.", false);
        send.setOnClickListener(v -> {
            String t = input.getText().toString().trim();
            if (t.isEmpty()) return;
            addBubble(t, true);
            input.setText("");
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void addBubble(String text, boolean out) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(16f);
        tv.setPadding(36, 24, 36, 24);
        tv.setBackgroundResource(out ? R.drawable.bg_bubble_out : R.drawable.bg_bubble_in);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = out ? Gravity.END : Gravity.START;
        lp.topMargin = 8;
        lp.bottomMargin = 8;
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.78);
        tv.setLayoutParams(lp);
        thread.addView(tv);
        TextView time = new TextView(this);
        time.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date()));
        time.setTextColor(0xFF8E8E93);
        time.setTextSize(11f);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.gravity = out ? Gravity.END : Gravity.START;
        time.setLayoutParams(tp);
        thread.addView(time);
    }
}
