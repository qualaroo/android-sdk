package qualaroo.com.QualarooMobileDemo;

import android.app.Application;

import com.qualaroo.Qualaroo;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Qualaroo.with(this)
                .setApiKey("API_KEY_HERE")
                .setDebugMode(false)
                .init();
    }
}
