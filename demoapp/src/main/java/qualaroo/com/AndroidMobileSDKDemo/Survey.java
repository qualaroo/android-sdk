package qualaroo.com.AndroidMobileSDKDemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Objects;

import qualaroo.com.AndroidMobileSDK.QualarooBackgroundStyle;
import qualaroo.com.AndroidMobileSDK.QualarooSurvey;
import qualaroo.com.AndroidMobileSDK.QualarooSurveyPosition;

public class Survey extends AppCompatActivity {

    EditText apiKeyEditText;
    EditText apiSecretKeyEditText;
    EditText surveyAliasEditText;
    Button showSurveyButton;
    Spinner positionSpinner;
    Spinner backgroundStyleSpinner;
    SeekBar seekBar;

    String mSurveyAlias;
    String mAPIKey;
    String mAPISecretKey;
    int alpha = 128;

    boolean mIsTable;
    SharedPreferences sharedPreferences;
    QualarooSurveyPosition mAttachmentPosition;

    public String getSurveyAlias() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSurveyAlias = sharedPreferences.getString("DemoSurvey", "");
        return mSurveyAlias;
    }

    public void setSurveyAlias(String surveyAlias) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("DemoSurvey", surveyAlias).apply();

        if (mQualarooSurvey != null && !surveyAlias.isEmpty()) {
            showSurveyButton.setEnabled(true);
        } else {
            showSurveyButton.setEnabled(false);
        }

        mSurveyAlias = surveyAlias;
    }

    public String getAPIKey() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAPIKey = sharedPreferences.getString("DemoAPIKey", "");
        return mAPIKey;
    }

    public void setAPIKey(String APIKey) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("DemoAPIKey", APIKey).apply();

        showSurveyButton.setEnabled(false);

        mAPIKey = APIKey;

    }

    public String getAPISecretKey() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAPISecretKey = sharedPreferences.getString("DemoAPISecretKey", "");
        return mAPISecretKey;
    }

    public void setAPISecretKey(String APISecretKey) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("DemoAPISecretKey", APISecretKey).apply();

        mAPISecretKey = APISecretKey;

    }

    public QualarooBackgroundStyle getBackgroundStyle() {
        String style;
        QualarooBackgroundStyle backgroundStyle;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        style = sharedPreferences.getString("BackgroundStyle", "");

        if (Objects.equals(style, ""))
            backgroundStyle = QualarooBackgroundStyle.DARK;
        else
            backgroundStyle = QualarooBackgroundStyle.valueOf(style);

        return backgroundStyle;
    }

    public void setBackgroundStyle(QualarooBackgroundStyle style) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("BackgroundStyle", style.toString()).apply();
    }

    public QualarooSurveyPosition getAttachmentPosition() {
        String position;
        QualarooSurveyPosition attachmentPosition;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        position = sharedPreferences.getString("DemoAttachmentPosition", "");

        if (position == null) {
            if (this.getResources().getBoolean(qualaroo.com.AndroidMobileSDKDemo.R.bool.isTablet)) {
                attachmentPosition = QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH;
            } else {
                attachmentPosition = QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM;
            }
            mAttachmentPosition = attachmentPosition;
        } else {
            mAttachmentPosition = QualarooSurveyPosition.valueOf(position);
        }
        return mAttachmentPosition;
    }

    public void setAttachmentPosition(QualarooSurveyPosition attachmentPosition) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("DemoAttachmentPosition", attachmentPosition.toString()).apply();

        showSurveyButton.setEnabled(false);

    }

    QualarooSurvey mQualarooSurvey = new QualarooSurvey(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        mIsTable = getResources().getBoolean(R.bool.isTablet);

        apiKeyEditText = (EditText) findViewById(R.id.apikey_edittext);
        surveyAliasEditText = (EditText) findViewById(R.id.survey_edittext);
        showSurveyButton = (Button) findViewById(R.id.show_survey_button);
        apiSecretKeyEditText = (EditText) findViewById(R.id.secretkey_edittext);

        showSurveyButton.setEnabled(false);
        apiKeyEditText.setText(getAPIKey());
        apiSecretKeyEditText.setText(getAPISecretKey());
        surveyAliasEditText.setText(getSurveyAlias());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getStringPosition());
        adapter.setDropDownViewResource(R.layout.spinner_item);

        positionSpinner = (Spinner) findViewById(R.id.spinner);
        positionSpinner.setAdapter(adapter);
        positionSpinner.setPrompt("Survey position");
        positionSpinner.setSelection(0);

        //Spinner, set backgroundStyle
        ArrayAdapter<String> background_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getStringBackground());
        background_adapter.setDropDownViewResource(R.layout.spinner_item);
        backgroundStyleSpinner = (Spinner) findViewById(R.id.spinner_background_style);
        backgroundStyleSpinner.setAdapter(background_adapter);
        backgroundStyleSpinner.setPrompt("Background style");
        backgroundStyleSpinner.setSelection(getBackgroundStyle().ordinal());

        apiKeyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }

                String s = ((EditText) v).getText().toString();
                if (!s.isEmpty()) {
                    setAPIKey(s);
                }
            }
        });

        apiKeyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                                actionId == EditorInfo.IME_ACTION_DONE)) {
                    apiSecretKeyEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        apiSecretKeyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }

                String s = ((EditText) v).getText().toString();
                if (!s.isEmpty()) {
                    setAPISecretKey(s);
                }
            }
        });

        apiSecretKeyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                                actionId == EditorInfo.IME_ACTION_DONE)) {
                    surveyAliasEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        surveyAliasEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }

                String s = ((EditText) v).getText().toString();
                setSurveyAlias(s);
            }
        });

        surveyAliasEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                                actionId == EditorInfo.IME_ACTION_DONE)) {
                    InputMethodManager inputMethodManager;
                    inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(surveyAliasEditText.getWindowToken(), 0);
                    showSurveyButton.requestFocus();
                    return true;
                }
                return false;
            }
        });

        backgroundStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        setBackgroundStyle(QualarooBackgroundStyle.DARK);
                        break;
                    case 1:
                        setBackgroundStyle(QualarooBackgroundStyle.GREY);
                        break;
                    case 2:
                        setBackgroundStyle(QualarooBackgroundStyle.LIGHT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mIsTable) {
                    switch (position) {
                        case 0:
                            setAttachmentPosition(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH);
                            break;
                        case 1:
                            setAttachmentPosition(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM_LEFT);
                            break;
                        case 2:
                            setAttachmentPosition(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_TOP_RIGHT);
                            break;
                        case 3:
                            setAttachmentPosition(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM_LEFT);
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            setAttachmentPosition(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM);
                            break;
                        case 1:
                            setAttachmentPosition(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_TOP);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(255);
        seekBar.setProgress(128);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                alpha = value;
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
            public void onClick(View v) {
                instantiateQualarooWithAPIKey();
                if (mQualarooSurvey == null) {
                    presentErrorMessage("Qualaroo Survey is not properly configured. Please enter a valid API Key.");
                } else {
                    mQualarooSurvey.setBackgroundStyle(getBackgroundStyle(), alpha);
                    mQualarooSurvey.showSurvey(mSurveyAlias, true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQualarooSurvey.removeFromActivity();
    }

    //region Private Methods

    private boolean instantiateQualarooWithAPIKey() {

        mQualarooSurvey.removeFromActivity();
        mQualarooSurvey = null;

        try {
            if (getAPISecretKey() == "") {
                mQualarooSurvey = new QualarooSurvey(this).initWithAPIKey(mAPIKey);
            } else {
                mQualarooSurvey = new QualarooSurvey(this).initWithAPIKey(mAPIKey, mAPISecretKey);
            }
            mQualarooSurvey.attachToActivity(getAttachmentPosition());
            mQualarooSurvey.setIdentityCodeWithString("Android test #1");

            if (!mSurveyAlias.isEmpty()) {
                showSurveyButton.setEnabled(true);
            }
            return true;
        } catch (InflateException e) {
            presentErrorMessage(e.getMessage());
        }
        return true;
    }

    private String[] getStringPosition() {
        if (this.getResources().getBoolean(R.bool.isTablet)) {
            return new String[]{
                    "Bottom Right",
                    "Bottom Left",
                    "Top Right",
                    "Top Left"
            };
        } else {
            return new String[]{
                    "Bottom",
                    "Top"
            };
        }
    }

    private String[] getStringBackground() {
        return new String[]{
                "Dark",
                "Grey",
                "White"
        };
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

}
