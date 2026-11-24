package com.edutrack.util;

import java.util.List;
import java.util.Scanner;

/**
 * Utility for console user interface rendering: headers, alerts, ASCII tables,
 * and robust user input reading.
 * Uses clean ASCII styling that renders perfectly on any Windows terminal.
 */
public final class ConsoleUtils {
    private static final Scanner SCANNER = new Scanner(System.in);

    // ANSI Colors for console styling (graceful on modern terminals)
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String PURPLE = "\u001B[35m";

    private ConsoleUtils() {}

    public static void printHeader(String title) {
        int width = 76;
        String line = "=".repeat(width);
        System.out.println();
        System.out.println(CYAN + BOLD + "+" + line + "+" + RESET);
        int pad = (width - title.length()) / 2;
        String padded = " ".repeat(Math.max(0, pad)) + title + " ".repeat(Math.max(0, width - pad - title.length()));
        System.out.println(CYAN + BOLD + "| " + padded.substring(1, padded.length() - 1) + " |" + RESET);
        System.out.println(CYAN + BOLD + "+" + line + "+" + RESET);
    }

    public static void printSection(String title) {
        System.out.println();
        int lineLen = Math.max(0, 70 - title.length());
        System.out.println(BLUE + BOLD + "[--- " + title + " " + "-".repeat(lineLen) + "]" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "[SUCCESS] " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "[WARNING] " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "[ERROR] " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "[INFO] " + message + RESET);
    }

    public static String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(BOLD + prompt + ": " + RESET);
            String input = SCANNER.nextLine();
            if (input != null) {
                input = input.trim();
                if (!input.isEmpty() || allowEmpty) {
                    return input;
                }
            }
            printWarning("Input cannot be empty. Please enter a valid value.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(BOLD + prompt + " (" + min + " - " + max + "): " + RESET);
            String input = SCANNER.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int val = Integer.parseInt(input.trim());
                    if (val >= min && val <= max) {
                        return val;
                    }
                    printWarning(String.format("Please enter a number between %d and %d.", min, max));
                } catch (NumberFormatException e) {
                    printWarning("Invalid input. Please enter a numeric integer value.");
                }
            } else {
                printWarning("Input cannot be empty.");
            }
        }
    }

    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(BOLD + prompt + " (" + min + " - " + max + "): " + RESET);
            String input = SCANNER.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                try {
                    double val = Double.parseDouble(input.trim());
                    if (val >= min && val <= max) {
                        return val;
                    }
                    printWarning(String.format("Please enter a number between %.1f and %.1f.", min, max));
                } catch (NumberFormatException e) {
                    printWarning("Invalid input. Please enter a valid decimal number.");
                }
            } else {
                printWarning("Input cannot be empty.");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(BOLD + prompt + " (y/n): " + RESET);
            String input = SCANNER.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                String clean = input.trim().toLowerCase();
                if (clean.equals("y") || clean.equals("yes")) return true;
                if (clean.equals("n") || clean.equals("no")) return false;
            }
            printWarning("Please enter 'y' for yes or 'n' for no.");
        }
    }

    public static void pressEnterToContinue() {
        System.out.println();
        System.out.print(CYAN + "Press [Enter] to continue..." + RESET);
        SCANNER.nextLine();
    }

    /**
     * Prints a robust ASCII formatted table.
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return;

        int cols = headers.length;
        int[] colWidths = new int[cols];

        for (int i = 0; i < cols; i++) {
            colWidths[i] = headers[i].length();
        }

        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < Math.min(cols, row.length); i++) {
                    if (row[i] != null && row[i].length() > colWidths[i]) {
                        colWidths[i] = row[i].length();
                    }
                }
            }
        }

        // Add 2 padding spaces
        for (int i = 0; i < cols; i++) {
            colWidths[i] += 2;
        }

        // Top border: +----+----+
        StringBuilder border = new StringBuilder("+");
        for (int i = 0; i < cols; i++) {
            border.append("-".repeat(colWidths[i])).append("+");
        }
        System.out.println(border);

        // Header Row
        StringBuilder headerRow = new StringBuilder("|");
        for (int i = 0; i < cols; i++) {
            headerRow.append(centerString(headers[i], colWidths[i])).append("|");
        }
        System.out.println(BOLD + CYAN + headerRow + RESET);
        System.out.println(border);

        // Data Rows
        if (rows == null || rows.isEmpty()) {
            int totalWidth = 0;
            for (int w : colWidths) totalWidth += w;
            totalWidth += cols - 1;
            String emptyMsg = centerString("No records found", totalWidth);
            System.out.println("|" + emptyMsg + "|");
        } else {
            for (String[] row : rows) {
                StringBuilder rowStr = new StringBuilder("|");
                for (int i = 0; i < cols; i++) {
                    String val = (i < row.length && row[i] != null) ? row[i] : "";
                    rowStr.append(padRight(" " + val, colWidths[i])).append("|");
                }
                System.out.println(rowStr);
            }
        }

        // Bottom Border
        System.out.println(border);
    }

    private static String centerString(String s, int width) {
        if (s == null) s = "";
        int pad = (width - s.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + s + " ".repeat(Math.max(0, width - pad - s.length()));
    }

    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
