package com.qualaroo.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class UriOpener {

    private final Context context;

    public UriOpener(Context context) {
        this.context = context.getApplicationContext();
    }

    public void openUri(@Nullable String stringUri) {
        if (stringUri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(stringUri));
        if (canIntentBeHandled(intent)) {
            context.startActivity(intent);
        }
    }

    private boolean canIntentBeHandled(Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        return intent.resolveActivity(packageManager) != null;
    }
}
