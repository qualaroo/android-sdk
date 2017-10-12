package com.qualaroo.demo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.qualaroo.Qualaroo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import qualaroo.com.QualarooMobileDemo.R;

public class SurveyDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Runtime.getRuntime().exec("logcat -c");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_survey);
        final Settings settings = new Settings(this);
        String apiKey = settings.getApiKey();

        final EditText apiKeyEditText = findViewById(R.id.qualaroo__demo_api_key_edit_text);
        apiKeyEditText.setText(apiKey);
        findViewById(R.id.qualaroo__demo_set_api_key_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.storeApiKey(apiKeyEditText.getText().toString());
                ProcessPhoenix.triggerRebirth(SurveyDemoActivity.this);
            }
        });

        final View showSurveyButton = findViewById(R.id.qualaroo__demo_show_survey);
        final EditText surveyAlias = findViewById(R.id.qualaroo__demo_survey_alias_edit_text);
        surveyAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                showSurveyButton.setEnabled(editable.length() > 0);
            }
        });
        surveyAlias.setText(settings.recentSurveyAlias());
        showSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.storeRecentSurveyAlias(surveyAlias.getText().toString());
                try {
                    Qualaroo.getInstance().showSurvey("mobile_test_survey");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Crash occured. Did you set a proper API key?", Toast.LENGTH_SHORT).show();
                    Log.e("Demo", e.getMessage(), e);
                }
            }
        });

        findViewById(R.id.qualaroo__demo_reset_surveys).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getApplicationContext().deleteDatabase("qualaroo.db");
                getApplicationContext().getSharedPreferences("qualaroo_prefs", Context.MODE_PRIVATE).edit().clear().apply();
                ProcessPhoenix.triggerRebirth(SurveyDemoActivity.this);
            }
        });

        findViewById(R.id.qualaroo__demo_set_user_property).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddUserPropertyDialog().show(getSupportFragmentManager(), "propertyDialog");
            }
        });

        findViewById(R.id.qualaroo__demo_check_logs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LogsDialog().show(getSupportFragmentManager(), "logsDialog");
            }
        });
    }

    public static final class AddUserPropertyDialog extends BottomSheetDialogFragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.qualaroo__demo_dialog_set_user_property, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            final EditText userPropertyName = view.findViewById(R.id.qualaroo__demo_dialog_property_name);
            final EditText userPropertyValue = view.findViewById(R.id.qualaroo__demo_dialog_property_value);

            view.findViewById(R.id.qualaroo__demo_dialog_property_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = userPropertyName.getText().toString();
                    String value = userPropertyValue.getText().toString();
                    try {
                        Qualaroo.getInstance().setUserProperty(name, value);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Crash occured. Did you set a proper API key?", Toast.LENGTH_SHORT).show();
                        Log.e("Demo", e.getMessage(), e);
                    }
                    dismissAllowingStateLoss();
                }
            });
            view.findViewById(R.id.qualaroo__demo_dialog_property_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismissAllowingStateLoss();
                        }
                    }
            );
        }
    }

    public static final class LogsDialog extends BottomSheetDialogFragment {

        private TextView logsView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.qualaroo__demo_dialog_logs, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            logsView = view.findViewById(R.id.qualaroo__demo_dialog_logs);
            logsView.setMovementMethod(new ScrollingMovementMethod());
            view.findViewById(R.id.qualaroo__demo_dialog_logs_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissAllowingStateLoss();
                }
            });

            view.findViewById(R.id.qualaroo__demo_dialog_logs_copy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("logcat", logsView.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getContext(), "Logs copied to clipboard", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Could not copy logs", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            populateLogs();
        }

        private void populateLogs() {
            try {
                Process process = Runtime.getRuntime().exec("logcat -d -v long QualarooSDK:V");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                StringBuilder log=new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    log.append(line);
                    log.append('\n');
                }
                logsView.setText(log.toString());
            } catch (IOException e) {
                //ignore
            }

        }
    }
}
