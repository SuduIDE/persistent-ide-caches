package com.github.SuduIDE.persistentidecaches.ccsearch;

import java.util.ArrayList;

public class Matcher {

    public static final int NEG_INF = -100000;
    public static final int FIRST_SYMBOL = 1000;
    public static final int HUMP_CASE_MATCH = 50;
    public static final int HUMP_SKIP = 10;

    public static int match(final String pattern, final String symbol) {
        final String symbolLC = symbol.toLowerCase();
        final String patternLC = pattern.toLowerCase();

        final int n = symbol.length();
        final int m = pattern.length();

        final boolean[] isHump = new boolean[n];

        final ArrayList<Integer> humps = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i == 0 || Character.isUpperCase(symbol.charAt(i)) || Character.isDigit(symbol.charAt(i))
                || (symbol.charAt(i - 1) == '_' && symbol.charAt(i) != '_')) {
                humps.add(i);
                isHump[i] = true;
            }
        }
        final int h = humps.size();

        int curH = 0;

        final int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dp[i][j] = NEG_INF;
            }
        }
        dp[0][0] = 0; // dp[i][j] - best score for prefixes of size i and j, where characters i and j
        // are taken

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (dp[i][j] == NEG_INF) {
                    continue;
                }
                if (symbolLC.charAt(i) == patternLC.charAt(j)) {
                    final int value = dp[i][j]
                        + (symbol.charAt(i) == pattern.charAt(j) ? 1 : 0) * (isHump[i] ? HUMP_CASE_MATCH : 1)
                        + (i == 0 ? FIRST_SYMBOL : 0);
                    if (value > dp[i + 1][j + 1]) {
                        dp[i + 1][j + 1] = value;
                    }
                }
                for (int k = curH; k < h; k++) {
                    final int pos = humps.get(k);
                    if (symbolLC.charAt(pos) == patternLC.charAt(j)) {
                        final int value = dp[i][j] + (symbol.charAt(pos) == pattern.charAt(j) ? HUMP_CASE_MATCH : 0)
                            - HUMP_SKIP * (curH - k);
                        if (value > dp[pos + 1][j + 1]) {
                            dp[pos + 1][j + 1] = value;
                        }
                    }
                }
            }
            if (isHump[i]) {
                curH++;
            }
        }

        int ans = NEG_INF;
        for (int i = 0; i <= n; i++) {
            if (dp[i][m] >= ans) {
                ans = dp[i][m];
            }
        }
        return ans - h;
    }

    public static int[] letters(final String pattern, final String symbol) {
        final String symbolLC = symbol.toLowerCase();
        final String patternLC = pattern.toLowerCase();

        final int n = symbol.length();
        final int m = pattern.length();

        final boolean[] isHump = new boolean[n];

        final ArrayList<Integer> humps = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i == 0 || Character.isUpperCase(symbol.charAt(i)) || Character.isDigit(symbol.charAt(i))
                || (symbol.charAt(i - 1) == '_' && symbol.charAt(i) != '_')) {
                humps.add(i);
                isHump[i] = true;
            }
        }
        final int h = humps.size();

        int curH = 0;

        final int[][] dp = new int[n + 1][m + 1];
        final int[][] prev = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dp[i][j] = NEG_INF;
                prev[i][j] = -1;
            }
        }
        dp[0][0] = 0; // dp[i][j] - best score for prefixes of size i and j, where characters i and j
        // are taken

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (dp[i][j] == NEG_INF) {
                    continue;
                }
                if (symbolLC.charAt(i) == patternLC.charAt(j)) {
                    final int value = dp[i][j]
                        + (symbol.charAt(i) == pattern.charAt(j) ? 1 : 0) * (isHump[i] ? HUMP_CASE_MATCH : 1)
                        + (i == 0 ? FIRST_SYMBOL : 0);
                    if (value > dp[i + 1][j + 1]) {
                        dp[i + 1][j + 1] = value;
                        prev[i + 1][j + 1] = i;
                    }
                }
                for (int k = curH; k < h; k++) {
                    final int pos = humps.get(k);
                    if (symbolLC.charAt(pos) == patternLC.charAt(j)) {
                        final int value = dp[i][j] + (symbol.charAt(pos) == pattern.charAt(j) ? HUMP_CASE_MATCH : 0)
                            - HUMP_SKIP * (curH - k);
                        if (value > dp[pos + 1][j + 1]) {
                            dp[pos + 1][j + 1] = value;
                            prev[pos + 1][j + 1] = i;
                        }
                    }
                }
            }
            if (isHump[i]) {
                curH++;
            }
        }

        int ans = NEG_INF;
        int curI = -1;
        for (int i = 0; i <= n; i++) {
            if (dp[i][m] >= ans) {
                ans = dp[i][m];
                curI = i;
            }
        }
        final ArrayList<Integer> answer = new ArrayList<>();
        int curJ = m;
        while (curI > 0) {
            answer.add(curI - 1);
            curI = prev[curI][curJ];
            curJ--;
        }
        final int[] res = new int[answer.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = answer.get(res.length - 1 - i);
        }
        return res; // score = ans - h
    }
}
