package com.lotterybot;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnStart;
    private TextView tvStatus, tvLog;
    private StringBuilder logBuilder = new StringBuilder();
    private boolean started = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnStart = findViewById(R.id.btnStart);
        Button btnSettings = findViewById(R.id.btnOpenSettings);
        tvStatus = findViewById(R.id.tvStatus);
        tvLog = findViewById(R.id.tvLog);
        
        btnStart.setOnClickListener(v -> toggleService());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        updateStatus();
    }
    
    private void toggleService() {
        if (!isAccessibilityEnabled()) {
            tvStatus.setText("请先开启无障碍服务");
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        BotAccessibilityService bot = BotAccessibilityService.getInstance();
        if (bot != null) {
            if (started) {
                bot.stop(); btnStart.setText("启动"); started = false;
                tvStatus.setText("状态: 已停止");
            } else {
                bot.setLogCallback(this::appendLog);
                bot.start(); btnStart.setText("停止"); started = true;
                tvStatus.setText("状态: 运行中");
            }
        } else {
            tvStatus.setText("状态: 服务未加载");
        }
    }
    
    private boolean isAccessibilityEnabled() {
        String service = getPackageName() + "/.BotAccessibilityService";
        try {
            String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return enabled != null && enabled.contains(service);
        } catch (Exception e) { return false; }
    }
    
    private void updateStatus() {
        if (isAccessibilityEnabled()) {
            tvStatus.setText("状态: 无障碍已开启");
        } else {
            tvStatus.setText("状态: 请开启无障碍");
        }
    }
    
    private void appendLog(String msg) {
        runOnUiThread(() -> {
            logBuilder.insert(0, msg + "\n");
            tvLog.setText(logBuilder.toString());
            if (logBuilder.length() > 10000) logBuilder.setLength(8000);
        });
    }
}