package org.archivision.pickbot.service;

public class Util {
    public static boolean isWholeStringNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String toCamelCase(String input) {
        final String[] words = input.split("\\s+");
        final StringBuilder camelCaseString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                camelCaseString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
        }

        return camelCaseString.toString();
    }
}
