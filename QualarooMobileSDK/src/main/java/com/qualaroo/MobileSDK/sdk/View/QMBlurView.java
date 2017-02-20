package com.qualaroo.MobileSDK.sdk.View;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

public class QMBlurView extends LinearLayout {

    public QMBlurViewInterface delegate;

    public QMBlurView(Context context) {
        super(context);
    }

    public QMBlurView init(boolean isTablet) {

        setOrientation(VERTICAL);
        setBackgroundColor(Color.argb(128, 128, 128, 128));
        setVisibility(INVISIBLE);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );

        setLayoutParams(params);
        if (!isTablet)
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    delegate.handleTap();
                }
            });
        return this;
    }
}
