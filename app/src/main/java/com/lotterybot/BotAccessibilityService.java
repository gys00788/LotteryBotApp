package com.lotterybot;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;

public class BotAccessibilityService extends AccessibilityService {
    private static BotAccessibilityService instance;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;
    private int multiplier = 1;
    private double balance = 1000, pnl = 0;
    private int maxLoss = 0, curLoss = 0, round = 0;
    private String lastDraw = null;
    private List<Integer> lastBetDigits = null;
    private int lastBetMult = 1;
    
    public interface LogCallback { void onLog(String msg); }
    private LogCallback logCb;
    public void setLogCallback(LogCallback cb) { this.logCb = cb; }
    
    public static BotAccessibilityService getInstance() { return instance; }
    @Override public void onCreate() { super.onCreate(); instance = this; }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!running) return;
        if (handler.hasMessages(0)) handler.removeMessages(0);
        handler.postDelayed(this::runCycle, 1500);
    }
    @Override public void onInterrupt() {}
    
    public void start() { running = true; addLog("启动自动化"); }
    public void stop() { running = false; addLog("停止"); }
    public boolean isRunning() { return running; }
    
    private void addLog(String msg) { if (logCb != null) logCb.onLog(msg); }
    
    // Read lottery number from screen using accessibility tree
    private int[] readDrawNumber() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;
        try {
            int[] result = findDigits(root);
            if (result != null) return result;
            List<AccessibilityNodeInfo> all = new ArrayList<>();
            collectNodes(root, all);
            for (AccessibilityNodeInfo n : all) {
                result = findDigits(n);
                if (result != null) { addLog("读号: " + result[0]+result[1]+result[2]+result[3]+result[4]); return result; }
            }
        } finally { root.recycle(); }
        return null;
    }
    
    private int[] findDigits(AccessibilityNodeInfo node) {
        if (node == null || node.getText() == null) return null;
        String t = node.getText().toString().replaceAll("\\D", "");
        if (t.length() >= 5) {
            int[] r = new int[5];
            for (int i = 0; i < 5; i++) r[i] = t.charAt(i) - '0';
            return r;
        }
        return null;
    }
    
    private void collectNodes(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) return;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) { list.add(child); collectNodes(child, list); }
        }
    }
    
    private AccessibilityNodeInfo findButton(String text) {
        return findBtnInTree(getRootInActiveWindow(), text);
    }
    
    private AccessibilityNodeInfo findBtnInTree(AccessibilityNodeInfo node, String text) {
        if (node == null) return null;
        CharSequence t = node.getText();
        CharSequence cd = node.getContentDescription();
        String s = (t != null ? t.toString() : "") + (cd != null ? cd.toString() : "");
        if (s.equals(text) && node.isClickable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo found = findBtnInTree(child, text);
            if (found != null) return found;
        }
        return null;
    }
    
    private boolean clickNode(AccessibilityNodeInfo node) {
        if (node == null) return false;
        if (node.isClickable()) { node.performAction(AccessibilityNodeInfo.ACTION_CLICK); return true; }
        AccessibilityNodeInfo parent = node.getParent();
        while (parent != null) {
            if (parent.isClickable()) { parent.performAction(AccessibilityNodeInfo.ACTION_CLICK); return true; }
            parent = parent.getParent();
        }
        return false;
    }
    
    public void runCycle() {
        int[] draw = readDrawNumber();
        if (draw == null) { addLog("未识别到号码"); return; }
        
        StringBuilder sb = new StringBuilder();
        for (int d : draw) sb.append(d);
        String ds = sb.toString();
        if (ds.equals(lastDraw)) { return; }
        lastDraw = ds;
        
        boolean allSame = true;
        for (int i = 1; i < draw.length; i++) { if (draw[i] != draw[0]) { allSame = false; break; } }
        if (allSame) { addLog("全同号跳过"); return; }
        
        if (lastBetDigits != null) {
            int[] nd = draw;
            BotLogic.ProfitResult pr = BotLogic.calcProfit(new ArrayList<>(lastBetDigits), nd, lastBetMult);
            balance += pr.profit; pnl += pr.profit;
            if (pr.profit < 0) { curLoss++; if (curLoss > maxLoss) maxLoss = curLoss; } else curLoss = 0;
            multiplier = pr.newMultiplier;
        }
        
        BotLogic.SelectionResult sel = BotLogic.selectBetDigits(draw);
        List<Integer> bet = BotLogic.fix2(sel.digits, draw);
        lastBetDigits = new ArrayList<>(bet);
        lastBetMult = multiplier;
        addLog(sel.reason + " -> " + bet.toString() + " x" + multiplier);
        
        for (int d : bet) {
            AccessibilityNodeInfo btn = findButton(String.valueOf(d));
            if (btn != null) { clickNode(btn); sleep(150); }
        }
        
        String[] keywords = {"确认", "下单", "投注"};
        for (String kw : keywords) {
            AccessibilityNodeInfo confirm = findButton(kw);
            if (confirm != null) { clickNode(confirm); addLog("下单"); break; }
        }
        round++;
    }
    
    private void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) {} }
}