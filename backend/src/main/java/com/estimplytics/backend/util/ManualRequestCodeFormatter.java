package com.estimplytics.backend.util;

public final class ManualRequestCodeFormatter {

    private ManualRequestCodeFormatter() {
    }

    public static String format(String projectName, long sequence) {
        return "%s-%s".formatted(projectName.trim(), formatSequence(sequence));
    }

    static String formatSequence(long sequence) {
        int width = sequence > 99999L ? String.valueOf(sequence).length() : 5;
        return String.format("%0" + width + "d", sequence);
    }
}
