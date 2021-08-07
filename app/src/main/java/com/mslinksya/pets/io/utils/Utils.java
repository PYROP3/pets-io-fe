package com.mslinksya.pets.io.utils;

import java.util.regex.Pattern;

public class Utils {

    // A username validation check
    public static boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }

        return Pattern.compile(Constants.EMAIL_REGEX_STRING).matcher(username).matches();
    }

    public static boolean isUserAliasValid(String useralias) {
        if (useralias == null) {
            return false;
        }

        return 4 < useralias.length() && useralias.length() < 16;
    }

    // A password validation check
    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public static int parseIntOrDefault(String string, int defaultValue) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
