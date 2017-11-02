package com.qualaroo.ui.render;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.RestrictTo;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.MessageType;
import com.qualaroo.ui.OnMessageConfirmedListener;
import com.qualaroo.util.ContentUtils;
import com.qualaroo.util.DebouncingOnClickListener;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class MessageRenderer {

    private final Theme theme;

    MessageRenderer(Theme theme) {
        this.theme = theme;
    }

    public View render(final Context context, final Message message, final OnMessageConfirmedListener onMessageConfirmedListener) {
        final View view = LayoutInflater.from(context).inflate(R.layout.qualaroo__view_message, null);
        TextView text = view.findViewById(R.id.qualaroo__view_message_text);
        text.setText(ContentUtils.sanitazeText(message.description()));
        text.setTextColor(theme.textColor());
        text.setMovementMethod(new ScrollingMovementMethod());
        final Button callToAction = view.findViewById(R.id.qualaroo__view_message_cta);
        ThemeUtils.applyTheme(callToAction, theme);
        if (message.type() == MessageType.REGULAR || message.type() == MessageType.UNKNOWN) {
            callToAction.setText(android.R.string.ok);
            callToAction.setOnClickListener(new DebouncingOnClickListener() {
                @Override public void doClick(View v) {
                    onMessageConfirmedListener.onMessageConfirmed(message);
                }
            });
        } else if (message.type() == MessageType.CALL_TO_ACTION) {
            callToAction.setText(message.ctaMap().text());
            callToAction.setOnClickListener(new DebouncingOnClickListener() {
                @Override public void doClick(View v) {
                    if (message.ctaMap().uri() != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(message.ctaMap().uri()));
                        context.startActivity(intent);
                    }
                    onMessageConfirmedListener.onMessageConfirmed(message);
                }
            });
        }
        return view;
    }


}
