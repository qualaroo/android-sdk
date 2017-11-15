package com.qualaroo.internal.network;

import android.os.Build;
import android.os.StatFs;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageRepository {

    private static final int MIN_DISK_CACHE_SIZE = 1 * 1024 * 1024; // 1MB
    private static final int MAX_DISK_CACHE_SIZE = 2 * 1024 * 1024; // 2MB

    private final OkHttpClient okHttpClient;

    public ImageRepository(OkHttpClient okHttpClient, File cacheDir) {
        this(okHttpClient, cacheDir, calculateDiskCacheSize(cacheDir));
    }

    ImageRepository(OkHttpClient okHttpClient, File cacheDir, long cacheSize) {
        this.okHttpClient = okHttpClient.newBuilder()
                .addNetworkInterceptor(new ImmutableCacheControlInjector())
                .cache(new Cache(cacheDir, cacheSize))
                .build();
    }

    public Response load(Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }

    private static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            final long blockCount;
            final long blockSize;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockCount = statFs.getBlockCount();
                blockSize = statFs.getBlockSize();
            } else {
                blockCount = statFs.getBlockCountLong();
                blockSize = statFs.getBlockSizeLong();
            }
            long available = blockCount * blockSize;
            size = available / 50; // target 2% of the total space.

        } catch (IllegalArgumentException ignored) {
        }
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    private static class ImmutableCacheControlInjector implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            return response.newBuilder()
                    .addHeader("Cache-Control", "immutable")
                    .build();
        }
    }
}
