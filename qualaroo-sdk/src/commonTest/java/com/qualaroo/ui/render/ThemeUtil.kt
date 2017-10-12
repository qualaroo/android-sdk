package com.qualaroo.ui.render

class ThemeUtil {
    companion object {
        fun theme(dimColor: Int = 1, backgroundColor: Int = 0, borderColor: Int = 0, textColor: Int = 0, buttonTextColor: Int = 0, buttonEnabledColor: Int = 0, buttonDisabledColor: Int = 0, accentColor: Int = 0): Theme {
            return Theme(dimColor, backgroundColor, borderColor, textColor, buttonTextColor, buttonEnabledColor, buttonDisabledColor, accentColor)
        }
    }
}
