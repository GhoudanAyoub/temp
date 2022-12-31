package com.syndicg5.networking.utils;

import java.util.Locale;

import kotlin.text.Regex;

public class Commun {

    public static String getClientNameInitials(String name) {
        String initials;
        Regex regex = new Regex("[-+.^:,]");
        String cleanName = regex.replace(name, "");
        if (!cleanName.isEmpty()) {
            initials = cleanName.length() < 2 ? cleanName.toUpperCase(Locale.ROOT) : cleanName.substring(0, 2).toUpperCase(Locale.ROOT);
        } else {
            initials = "AA";
        }
        return initials;
    }

}
