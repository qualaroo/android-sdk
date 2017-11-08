package com.qualaroo.internal.network;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.qualaroo.util.TimeProvider;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Cache<T> {

    private final TimeProvider timeProvider;
    private final long timeLimit;
    private CacheEntry<T> cacheEntry = new CacheEntry<>(null, 0);

    public Cache(TimeProvider timeProvider, long timeLimit) {
        this.timeProvider = timeProvider;
        this.timeLimit = timeLimit;
    }

    public boolean isStale() {
        return timeProvider.currentTimeMillis() - cacheEntry.timestamp() > timeLimit;
    }

    public boolean isInvalid() {
        return cacheEntry.data == null;
    }

    void put(T data) {
        cacheEntry = new CacheEntry<>(data, timeProvider.currentTimeMillis());
    }

    @Nullable T get() {
        return cacheEntry.data();
    }

    private static class CacheEntry<T> {
        private T data;
        private long timestamp;


        CacheEntry(T data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        T data() {
            return data;
        }

        long timestamp() {
            return timestamp;
        }
    }
}
