package com.qualaroo.internal;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.LruCache;

import com.qualaroo.internal.network.ImageRepository;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSource;
import okio.Okio;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class ImageProvider {

    public interface OnBitmapLoadedListener {
        void onBitmapReady(Bitmap bitmap);
    }

    private final LruCache<String, Bitmap> bitmapLruCache;
    private final ImageRepository imageRepository;
    private final Executor backgroundExecutor;
    private final Executor mainThreadExecutor;

    public ImageProvider(Context context, ImageRepository imageRepository, Executor backgroundExecutor, Executor mainThreadExecutor) {
        this.bitmapLruCache = new LruCache<>(calculateMemoryCacheSize(context));
        this.imageRepository = imageRepository;
        this.backgroundExecutor = backgroundExecutor;
        this.mainThreadExecutor = mainThreadExecutor;
    }

    public void getImage(@Nullable final String url, @Nullable OnBitmapLoadedListener onBitmapLoadedListener) {
        if (url == null) {
            return;
        }
        Bitmap bitmap = bitmapLruCache.get(url);
        if (bitmap != null) {
            callListenerSafely(bitmap, onBitmapLoadedListener);
        } else {
            fetchBitmapAsync(url, onBitmapLoadedListener);
        }
    }

    private void callListenerSafely(Bitmap bitmap, @Nullable OnBitmapLoadedListener onBitmapLoadedListener) {
        if (onBitmapLoadedListener != null) {
            onBitmapLoadedListener.onBitmapReady(bitmap);
        }
    }

    private void fetchBitmapAsync(final String url, final OnBitmapLoadedListener onBitmapLoadedListener) {
        final WeakReference<OnBitmapLoadedListener> listener = new WeakReference<>(onBitmapLoadedListener);
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                final Bitmap bitmap = downloadBitmap(url);
                if (bitmap != null) {
                    bitmapLruCache.put(url, bitmap);
                    mainThreadExecutor.execute(new Runnable() {
                        @Override public void run() {
                            OnBitmapLoadedListener onBitmapLoadedListener = listener.get();
                            if (onBitmapLoadedListener != null) {
                                onBitmapLoadedListener.onBitmapReady(bitmap);
                            }
                        }
                    });
                }
            }
        });
    }

    @Nullable private Bitmap downloadBitmap(String url) {
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = imageRepository.load(request);
            if (!response.isSuccessful()) {
                return null;
            }
            return decodeBitmapFromResponse(response);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable private static Bitmap decodeBitmapFromResponse(Response response) {
        BufferedSource source = Okio.buffer(response.body().source());
        try {
            InputStream inputStream = new BufferedInputStream(source.inputStream());
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        } finally {
            Util.closeQuietly(source);
        }
    }

    private static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = largeHeap ? am.getLargeMemoryClass() : am.getMemoryClass();
        // Target ~8% of the available heap.
        return (int) (1024L * 1024L * memoryClass / 12);
    }
}
