package com.github.SuduIDE.persistentidecaches.ccsearch;

import java.util.ArrayList;

public class Matcher {

    final static int NEG_INF = -100000;

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
        dp[0][0] = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (dp[i][j] == NEG_INF) {
                    continue;
                }
                if (symbolLC.charAt(i) == patternLC.charAt(j)) {
                    final int value = dp[i][j] + (symbol.charAt(i) == pattern.charAt(j) ? 1 : 0) * (isHump[i] ? 50 : 1)
                            + (i == 0 ? 1000 : 0);
                    if (value > dp[i + 1][j + 1]) {
                        dp[i + 1][j + 1] = value;
                    }
                }
                for (int k = curH; k < h; k++) {
                    final int pos = humps.get(k);
                    if (symbolLC.charAt(pos) == patternLC.charAt(j)) {
                        final int value = dp[i][j] + (symbol.charAt(pos) == pattern.charAt(j) ? 50 : 0) - 10 * (curH - k);
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

}
