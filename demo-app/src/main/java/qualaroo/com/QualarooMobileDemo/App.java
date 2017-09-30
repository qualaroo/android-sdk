package qualaroo.com.QualarooMobileDemo;

import android.app.Application;
import android.widget.Toast;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.qualaroo.Qualaroo;

public class App extends Application {

    private Settings settings;

    @Override public void onCreate() {
        super.onCreate();
        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return;
        }
        settings = new Settings(this);
        String apiKey = settings.getApiKey();
        try {
            Qualaroo.initializeWith(this)
                    .setApiKey(apiKey)
                    .setDebugMode(true)
                    .init();
        } catch (Exception e) {
            Toast.makeText(this, "Invalid api key provided!", Toast.LENGTH_SHORT).show();
        }
    }
}
