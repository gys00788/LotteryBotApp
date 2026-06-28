package com.lotterybot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BotLogic {
    public static class SelectionResult {
        public List<Integer> digits;
        public String reason;
    }
    public static class ProfitResult {
        public double profit;
        public int newMultiplier;
    }
    
    public static boolean hasDup(int[] arr, int[] idxs) {
        Set<Integer> seen = new HashSet<>();
        for (int i : idxs) { if (seen.contains(arr[i])) return true; seen.add(arr[i]); }
        return false;
    }
    public static List<Integer> uniqueVals(int[] arr, int[] idxs) {
        List<Integer> res = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        for (int i : idxs) { if (!set.contains(arr[i])) { res.add(arr[i]); set.add(arr[i]); } }
        return res;
    }
    public static SelectionResult selectBetDigits(int[] draw) {
        SelectionResult r = new SelectionResult();
        int[] front = {0,1,2}, back = {2,3,4}, mid = {1,2,3};
        if (hasDup(draw, front) && uniqueVals(draw, front).size() >= 2) {
            r.digits = uniqueVals(draw, front); r.reason = "前三位有叠号"; return r;
        }
        if (hasDup(draw, back) && uniqueVals(draw, back).size() >= 2) {
            r.digits = uniqueVals(draw, back); r.reason = "后三位有叠号"; return r;
        }
        if (hasDup(draw, mid) && uniqueVals(draw, mid).size() >= 2) {
            r.digits = uniqueVals(draw, mid); r.reason = "中间三位有叠号"; return r;
        }
        r.digits = new ArrayList<>(); r.digits.add(draw[1]); r.digits.add(draw[3]);
        r.reason = "无叠号"; return r;
    }
    public static List<Integer> fix2(List<Integer> bet, int[] draw) {
        List<Integer> res = new ArrayList<>(bet);
        if (res.size() >= 2) return res.subList(0, 2);
        if (res.isEmpty()) { res.add(draw[1]); res.add(draw[3]); return res; }
        if (res.size() == 1) {
            for (int d : draw) { if (d != res.get(0)) { res.add(d); return res; } }
            res.add(draw[1]);
        }
        return res;
    }
    public static int judge6Bet(List<Integer> bet, int[] next) {
        int[][] groups = {{0,1,2},{1,2,3},{2,3,4}};
        int total = 0;
        for (int gi = 0; gi < 3; gi++) {
            int[] pos = groups[gi];
            boolean hasPair = false;
            for (int a = 0; a < pos.length && !hasPair; a++)
                for (int b = a+1; b < pos.length && !hasPair; b++)
                    if (next[pos[a]] == next[pos[b]]) hasPair = true;
            int odds = hasPair ? 6 : 3;
            for (int d : bet) {
                boolean matched = false;
                for (int p : pos) { if (next[p] == d) { matched = true; break; } }
                if (matched) total += odds;
            }
        }
        return total - 6;
    }
    public static ProfitResult calcProfit(List<Integer> bet, int[] next, int mult) {
        int profitUnits = judge6Bet(bet, next);
        double cost = 0.32 * mult * 2;
        double profit = profitUnits * (cost / 6);
        ProfitResult r = new ProfitResult();
        r.profit = profit;
        r.newMultiplier = (profit >= 0) ? 1 : ((mult >= 256) ? 1 : mult * 2);
        return r;
    }
}