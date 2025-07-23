package org.prombot.utils;

public class FormatUtil {

    public static String formatValue(double value, String format) {
        if (format == null) {
            return Double.toString(value);
        }

        switch (format.toLowerCase()) {
            case "percentage":
                // Format as percentage with 2 decimal places
                return String.format("%.2f%%", value);

            case "dataspeed":
                // Format as bits per second with units (bps, Kbps, Mbps, Gbps)
                return formatDataSpeed(value);

            default:
                // Default: just return value as string with 2 decimal places
                return String.format("%.2f", value);
        }
    }

    private static String formatDataSpeed(double bitsPerSecond) {
        String[] units = {"bps", "Kbps", "Mbps", "Gbps", "Tbps"};
        int unitIndex = 0;
        double speed = bitsPerSecond;

        while (speed >= 1000 && unitIndex < units.length - 1) {
            speed /= 1000;
            unitIndex++;
        }

        return String.format("%.2f %s", speed, units[unitIndex]);
    }
}
