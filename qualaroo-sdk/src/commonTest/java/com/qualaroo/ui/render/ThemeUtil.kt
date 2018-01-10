package com.qualaroo.ui.render

class ThemeUtil {
    companion object {
        fun theme(dimColor: Int = 1, backgroundColor: Int = 0, textColor: Int = 0, buttonEnabledColor: Int = 0, buttonDisabledColor: Int = 0, buttonTextEnabled: Int = 0, buttonTextDisabled: Int = 0, uiNormal: Int = 0, uiSelected: Int = 0): Theme {
            return Theme(backgroundColor, dimColor, textColor, buttonEnabledColor, buttonDisabledColor, buttonTextEnabled, buttonTextDisabled, uiNormal, uiSelected)
        }
    }
}
