package qualaroo.com.QualarooMobileDemo;

import android.app.Application;

import com.qualaroo.Qualaroo;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Qualaroo.initializeWith(this)
                .setApiKey("API_KEY_HERE")
                .setDebugMode(true)
                .init();
    }
}
