package com.qualaroo.ui.render;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.model.Message;
import com.qualaroo.ui.OnMessageConfirmedListener;
import com.qualaroo.util.DebouncingOnClickListener;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class MessageRenderer {

    private final Theme theme;

    MessageRenderer(Theme theme) {
        this.theme = theme;
    }

    public View render(Context context, final Message message, final OnMessageConfirmedListener onMessageConfirmedListener) {
        final View view = LayoutInflater.from(context).inflate(R.layout.qualaroo__view_message, null);
        TextView text = view.findViewById(R.id.qualaroo__view_message_text);
        final Button callToAction = view.findViewById(R.id.qualaroo__view_message_cta);
        callToAction.setText(android.R.string.ok);
        callToAction.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                onMessageConfirmedListener.onMessageConfirmed(message);
            }
        });
        text.setText(sanitizeMessageDescription(message.description()));
        text.setTextColor(theme.textColor());
        ThemeUtils.applyTheme(callToAction, theme);
        return view;
    }

    private Spanned sanitizeMessageDescription(@Nullable String text) {
        if (text == null) {
            return null;
        } else {
            return Html.fromHtml(text);
        }
    }

}
