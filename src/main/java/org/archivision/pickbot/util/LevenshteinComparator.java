package org.archivision.pickbot.util;

import org.springframework.stereotype.Component;

@Component
public class LevenshteinComparator {
    private final double SIMILARITY_THRESHOLD = 0.8;

    public boolean compare(String word1, String word2) {
        int distance = calculateLevenshteinDistance(word1, word2);
        int maxLength = Math.max(word1.length(), word2.length());

        double similarity = 1.0 - (double) distance / maxLength;

        return similarity >= SIMILARITY_THRESHOLD;
    }

    private int calculateLevenshteinDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (word1.charAt(i - 1) == word2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[len1][len2];
    }
}
