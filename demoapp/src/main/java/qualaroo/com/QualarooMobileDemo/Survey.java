package qualaroo.com.QualarooMobileDemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.qualaroo.MobileSDK.*;

import static com.qualaroo.MobileSDK.QMBackgroundColor.DARK;
import static com.qualaroo.MobileSDK.QMBackgroundColor.GREY;
import static com.qualaroo.MobileSDK.QMBackgroundColor.LIGHT;

import static com.qualaroo.MobileSDK.QMPosition.BOTTOM;
import static com.qualaroo.MobileSDK.QMPosition.BOTTOM_LEFT;
import static com.qualaroo.MobileSDK.QMPosition.BOTTOM_RIGHT;
import static com.qualaroo.MobileSDK.QMPosition.LEFT;
import static com.qualaroo.MobileSDK.QMPosition.RIGHT;
import static com.qualaroo.MobileSDK.QMPosition.TOP;
import static com.qualaroo.MobileSDK.QMPosition.TOP_LEFT;
import static com.qualaroo.MobileSDK.QMPosition.TOP_RIGHT;

public class Survey extends AppCompatActivity {

    EditText apiKeyEditText;
    EditText apiSecretKeyEditText;
    EditText surveyAliasEditText;

    Spinner positionSpinner;
    Spinner backgroundColorSpinner;

    SeekBar backgroundAlphaSeekBar;

    Button showSurveyButton;
    Button attachSurveyButton;

    Button removeQualarooMobileButton;
    Button getStateButton;

    QualarooMobile qualaroo;

    String apiKey;
    String apiSecretKey;
    String surveyAlias;

    int alpha = 128;

    SharedPreferences sharedPreferences;
    QMPosition position;

    // region Accessors

    SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        return sharedPreferences;
    }

    String getApiKey() {
        apiKey = getSharedPreferences().getString("DemoApiKey", "");
        return apiKey;
    }

    void setApiKey(String newValue) {
        getSharedPreferences().edit().putString("DemoApiKey", newValue).apply();

        apiKey = newValue;
    }

    String getApiSecretKey() {
        apiSecretKey = getSharedPreferences().getString("DemoApiSecretKey", "");
        return apiSecretKey;
    }

    void setApiSecretKey(String newValue) {
        sharedPreferences.edit().putString("DemoApiSecretKey", newValue).apply();

        apiSecretKey = newValue;
    }

    String getSurveyAlias() {
        surveyAlias = getSharedPreferences().getString("DemoSurveyAlias", "");
        return surveyAlias;
    }

    void setSurveyAlias(String newValue) {
        getSharedPreferences().edit().putString("DemoSurveyAlias", newValue).apply();

        surveyAlias = newValue;
    }

    QMBackgroundColor getBackgroundColor() {

        QMBackgroundColor backgroundColor;
        String color;

        color = getSharedPreferences().getString("DemoBackgroundColor", "");

        if (color.equals("")) {
            backgroundColor = GREY;
        } else {
            backgroundColor = QMBackgroundColor.valueOf(color.toUpperCase());
        }
        return backgroundColor;
    }

    void setBackgroundColor(QMBackgroundColor newValue) {
        sharedPreferences.edit().putString(
                "DemoBackgroundColor",
                QMBackgroundColor.toString(newValue)
        ).apply();
    }

    QMPosition getPosition() {

        QMPosition attachmentPosition;
        String position;

        position = getSharedPreferences().getString("DemoAttachmentPosition", "");

        if (position.equals("")) {
            attachmentPosition = BOTTOM;
        } else {
            attachmentPosition = QMPosition.valueOf(position);
        }

        return attachmentPosition;
    }

    void setPosition(QMPosition newValue) {
        getSharedPreferences().edit().putString(
                "DemoAttachmentPosition",
                newValue.toString()
        ).apply();

        position = newValue;
    }

    boolean isTablet() {
        return (this.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        apiKeyEditText = (EditText) findViewById(R.id.api_key_edit_text);
        apiSecretKeyEditText = (EditText) findViewById(R.id.secret_key_edit_text);
        surveyAliasEditText = (EditText) findViewById(R.id.survey_edit_text);

        positionSpinner = (Spinner) findViewById(R.id.position_spinner);
        backgroundColorSpinner = (Spinner) findViewById(R.id.background_color_spinner);

        backgroundAlphaSeekBar = (SeekBar) findViewById(R.id.background_alpha_seek_bar);

        showSurveyButton = (Button) findViewById(R.id.show_survey_button);
        attachSurveyButton = (Button) findViewById(R.id.attach_survey_button);

        removeQualarooMobileButton = (Button) findViewById(R.id.remove_qm_button);
        getStateButton = (Button) findViewById(R.id.get_state_button);


        setupViews();
        setupActions();

        apiKeyEditText.setText(getApiKey());
        apiSecretKeyEditText.setText(getApiSecretKey());
        surveyAliasEditText.setText(getSurveyAlias());
    }

    private void setupViews() {

        showSurveyButton.setEnabled(false);
        removeQualarooMobileButton.setEnabled(false);
        getStateButton.setEnabled(false);

        // Setup position spinner
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getStringPosition()
        );
        positionAdapter.setDropDownViewResource(R.layout.spinner_item);

        positionSpinner.setAdapter(positionAdapter);
        positionSpinner.setPrompt("Survey position");
        positionSpinner.setSelection(0);

        // Setup background color spinner
        ArrayAdapter<String> backgroundColorAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getStringBackground()
        );
        backgroundColorAdapter.setDropDownViewResource(R.layout.spinner_item);

        backgroundColorSpinner.setAdapter(backgroundColorAdapter);
        backgroundColorSpinner.setPrompt("Survey background color");

        // Setup background alpha
        backgroundAlphaSeekBar.setMax(255);
        backgroundAlphaSeekBar.setProgress(128);
    }

    void setupActions() {

        OnFocusListener onFocusListener = new OnFocusListener();
        OnEditorListener onEditorListener = new OnEditorListener();

        apiKeyEditText.setOnFocusChangeListener(onFocusListener);
        apiKeyEditText.setOnEditorActionListener(onEditorListener);

        apiSecretKeyEditText.setOnFocusChangeListener(onFocusListener);
        apiSecretKeyEditText.setOnEditorActionListener(onEditorListener);

        surveyAliasEditText.setOnFocusChangeListener(onFocusListener);
        surveyAliasEditText.setOnEditorActionListener(onEditorListener);

        positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (isTablet()) {
                    switch (i) {
                        case 0:
                            setPosition(TOP);
                            break;
                        case 1:
                            setPosition(BOTTOM);
                            break;
                        case 2:
                            setPosition(LEFT);
                            break;
                        case 3:
                            setPosition(RIGHT);
                            break;
                        case 4:
                            setPosition(TOP_LEFT);
                            break;
                        case 5:
                            setPosition(TOP_RIGHT);
                            break;
                        case 6:
                            setPosition(BOTTOM_LEFT);
                            break;
                        case 7:
                            setPosition(BOTTOM_RIGHT);
                            break;
                    }
                } else {
                    switch (i) {
                        case 0:
                            setPosition(TOP);
                            break;
                        case 1:
                            setPosition(BOTTOM);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        backgroundColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        setBackgroundColor(DARK);
                        break;
                    case 1:
                        setBackgroundColor(GREY);
                        break;
                    case 2:
                        setBackgroundColor(LIGHT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        backgroundAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                alpha = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        showSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qualaroo == null) {
                    presentErrorMessage("Qualaroo Survey is not properly configured. Please enter a valid API Key.");
                } else {
                    qualaroo.showSurvey(getSurveyAlias(), true, new QMCallback() {
                        @Override
                        public void callback(QMState state, QMReport report) {
                                presentMessage();
                        }
                    });
                }
            }
        });

        attachSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instantiateQualarooWithAPIKey()) {
                    showSurveyButton.setEnabled(true);
                    removeQualarooMobileButton.setEnabled(true);
                    getStateButton.setEnabled(true);
                }
            }
        });

        removeQualarooMobileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeQualarooMobile();
            }
        });

        getStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presentMessage();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //region Private Methods

    private void removeQualarooMobile() {
        if (qualaroo != null) {
            qualaroo.close();
            qualaroo = null;
        }
        showSurveyButton.setEnabled(false);
        removeQualarooMobileButton.setEnabled(false);
        getStateButton.setEnabled(false);
    }

    private boolean instantiateQualarooWithAPIKey() {

        if (qualaroo != null) {
            qualaroo.removeFromActivity();
            qualaroo.setBackgroundStyle(getBackgroundColor(), alpha);
            qualaroo.attachToActivity(this, getPosition(), new QMCallback() {
                @Override
                public void callback(QMState state, QMReport report) {
                    if (state == QMState.WARNING || state == QMState.CANCELLED) {
                        presentMessage();
                    }
                }
            });
        } else {
            try {
                qualaroo = new QualarooMobile(this.getBaseContext()).init(getApiKey(), getApiSecretKey());
                qualaroo.setBackgroundStyle(getBackgroundColor(), alpha);
                qualaroo.setIdentityCode("Test new logic #1");
                qualaroo.attachToActivity(this, getPosition(), new QMCallback() {
                    @Override
                    public void callback(QMState state, QMReport report) {
                        if (state == QMState.WARNING || state == QMState.CANCELLED) {
                            presentMessage();
                        }                    }
                });
            } catch (Exception e) {
                presentMessage();
            }
        }
        return true;
    }

    private String[] getStringPosition() {
        if (isTablet()) {
            return new String[]{
                    QMPosition.toString(TOP),
                    QMPosition.toString(BOTTOM),
                    QMPosition.toString(LEFT),
                    QMPosition.toString(RIGHT),
                    QMPosition.toString(TOP_LEFT),
                    QMPosition.toString(TOP_RIGHT),
                    QMPosition.toString(BOTTOM_LEFT),
                    QMPosition.toString(BOTTOM_RIGHT)
            };
        } else {
            return new String[]{
                    QMPosition.toString(TOP),
                    QMPosition.toString(BOTTOM)
            };
        }
    }

    private String[] getStringBackground() {
        return new String[]{
                QMBackgroundColor.toString(DARK),
                QMBackgroundColor.toString(GREY),
                QMBackgroundColor.toString(LIGHT)
        };
    }

    private void presentMessage() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setTitle(QMState.toString(qualaroo.getState()))
                        .setMessage(QMReport.toString(qualaroo.getStateReport()))
                        .setCancelable(false)
                        .setNegativeButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
    }

    private void presentErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QualarooAndroidSDK")
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //endregion

    // region Actions

    class OnFocusListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b) return;

            EditText editText = (EditText) view;
            String newValue = editText.getText().toString();

            if (editText == apiKeyEditText) {
                setApiKey(newValue);
            }
            if (editText == apiSecretKeyEditText) {
                setApiSecretKey(newValue);
            }
            if (editText == surveyAliasEditText) {
                setSurveyAlias(newValue);
            }

        }
    }

    class OnEditorListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

            if (keyEvent == null) return false;
            if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_ENTER)
                return false;

            EditText editText = (EditText) textView;

            if (editText == apiKeyEditText) {
                apiSecretKeyEditText.requestFocus();
                removeQualarooMobile();
                return true;
            }
            if (editText == apiSecretKeyEditText ) {
                surveyAliasEditText.requestFocus();
                removeQualarooMobile();
                return true;
            }
            if (editText == surveyAliasEditText) {
                surveyAliasEditText.clearFocus();
                InputMethodManager inputMethodManager;

                inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                inputMethodManager.hideSoftInputFromWindow(surveyAliasEditText.getWindowToken(), 0);

                if (showSurveyButton.isEnabled()) {
                    showSurveyButton.requestFocus();
                    removeQualarooMobile();
                    return true;
                } else {
                    positionSpinner.requestFocus();
                    removeQualarooMobile();
                    return true;
                }
            }

            removeQualarooMobile();

            return false;
        }
    }

    // endregion

}
