package qualaroo.com.QualarooMobileDemo;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String PREFS_NAME = "qualaroo_demo_prefs";
    private static final String KEY_API_KEY = "qualaroo_demo_api_key";
    private static final String KEY_RECENTLY_SHOWN_SURVEY = "qualaroo_demo_recent_survey";

    private final SharedPreferences sharedPreferences;

    public Settings(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void storeApiKey(String apiKey) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).commit();
    }

    public String getApiKey() {
        return sharedPreferences.getString(KEY_API_KEY, "");
    }

    void storeRecentSurveyAlias(String alias) {
        sharedPreferences.edit().putString(KEY_RECENTLY_SHOWN_SURVEY, alias).commit();
    }

    String recentSurveyAlias() {
        return sharedPreferences.getString(KEY_RECENTLY_SHOWN_SURVEY, "");
    }
}
