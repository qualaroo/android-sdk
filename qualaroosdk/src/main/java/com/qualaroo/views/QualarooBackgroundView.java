/*
 * Copyright © 2017 Qualaroo. All rights reserved.
 */

package com.qualaroo.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.qualaroo.helpers.Utils;

/*
 * Created by Artem Orynko on 4/13/17.
 */

public class QualarooBackgroundView extends LinearLayout {

    public QualarooBackgroundViewDelegate delegate;

    public QualarooBackgroundView(Context context) {
        super(context);
        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        setOrientation(VERTICAL);
        setBackgroundColor(Color.argb(128, 128, 128, 128));
        setVisibility(INVISIBLE);
        setLayoutParams(layoutParams);

        if (!Utils.isTablet(context)) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    delegate.handleTap();
                }
            });
        }
    }
    public interface QualarooBackgroundViewDelegate {
        void handleTap();
    }
}
