package com.qualaroo.MobileSDK;

public enum QMBackgroundColor {
    DARK,
    GREY,
    LIGHT;

    public static String toString(QMBackgroundColor color) {
        String result = "";

        switch (color) {
            case DARK:
                result = "Dark";
                break;
            case GREY:
                result = "Grey";
                break;
            case LIGHT:
                result = "Light";
                break;
        }

        return result;
    }
}
