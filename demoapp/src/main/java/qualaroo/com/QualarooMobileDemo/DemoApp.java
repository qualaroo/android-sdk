package qualaroo.com.QualarooMobileDemo;

import android.app.Application;

import com.qualaroo.Qualaroo;

/*
 * Created by Artem Orynko on 4/26/17.
 */

public class DemoApp extends Application {

    private static final String API_KEY = "eyJ2IjoxLCJ1Ijoia2kuanMvNjQ4MDEvZjJOLmpzIn0=";

    @Override
    public void onCreate() {
        super.onCreate();

        Qualaroo.Builder builder = new Qualaroo.Builder(this.getBaseContext(), API_KEY)
                .autoTrackScreen()
                .logLevel(Qualaroo.LogLevel.DEBUG);

        Qualaroo.setSingletonInstance(builder.build());
    }
}