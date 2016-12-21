package qualaroo.com.AndroidMobileSDK.View;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMLinearLayout extends LinearLayout {
    Context mContext;

    public QMLinearLayout(Context context) {
        super(context);
        mContext = context;
    }


    public void init(int position, View.OnClickListener listener) {
        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(Color.argb(128, 128, 128, 128));
        this.setVisibility(View.INVISIBLE);
        this.setGravity(position);

        LinearLayout.LayoutParams layoutParams;
        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        this.setLayoutParams(layoutParams);
        this.setOnClickListener(listener);
    }
}
