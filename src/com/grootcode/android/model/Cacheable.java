package com.grootcode.android.model;

import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import com.grootcode.android.model.InternalCache.CacheData;
import com.grootcode.android.util.LogUtils;
import com.grootcode.android.util.Maps;

public abstract class Cacheable<K, T extends Cacheable<K, T>> extends EntityBase<T> {
    private static final String TAG = LogUtils.makeLogTag(Cacheable.class);
    private static final boolean V = true;

    private static Map<Class<?>, InternalCache<?, ?>> sInternalCacheMap = Maps.newHashMap();
    private static Handler sHandler = new Handler();

    private static class CacheableContentObserver<K, T extends Cacheable<K, T>> extends ContentObserver {
        K mKey;
        Class<T> mTypeClass;

        public CacheableContentObserver(K key, Class<T> typeClass) {
            super(sHandler);
            mKey = key;
            mTypeClass = typeClass;
        }

        @Override
        public void onChange(boolean selfUpdate) {
            log("CacheableContentObserver notified for " + mTypeClass.getSimpleName() + ":" + mKey);
            T entity = Cacheable.getInternalCache(mTypeClass).get(mKey, false);
            if (entity != null) {
                entity.reload();
            }
        }
    }

    private transient boolean mIsStaleState = true;
    private transient boolean mQueryPendingState;
    private transient CacheableContentObserver<K, T> mCacheableContentObserver;

    private final K mKey;

    public Cacheable(final K key) {
        final InternalCache<K, T> internalCache = getInternalCache();

        /* If key is null, we are creating an empty instance, which is fine */
        if (internalCache == null && key != null) {
            throw new IllegalStateException("First Call init() on " + getClass().getName());
        }

        mKey = key;
    }

    protected static <K, T extends Cacheable<K, T>> void init(final Context context, CacheData<K, T> cacheData,
            Class<T> typeClass) {
        sInternalCacheMap.put(typeClass, new InternalCache<K, T>(context, cacheData));
    }

    @SuppressWarnings("unchecked")
    protected static <K, T extends Cacheable<K, T>> InternalCache<K, T> getInternalCache(Class<T> typeClass) {
        return (InternalCache<K, T>) sInternalCacheMap.get(typeClass);
    }

    @SuppressWarnings("unchecked")
    protected InternalCache<K, T> getInternalCache() {
        return getInternalCache(getClass());
    }

    public K getKey() {
        return mKey;
    }

    /**
     * Query Pending and Stale State parameters are only available for limited audience for
     * consumption. i.e. limited only for InternalCache
     */
    public boolean isQueryPending() {
        return mQueryPendingState;
    }

    void setQueryPending(boolean queryPending) {
        mQueryPendingState = queryPending;
    }

    public boolean isStaleState() {
        return mIsStaleState;
    }

    void setStaleState(boolean staleState) {
        mIsStaleState = staleState;
    }

    protected static <K, T extends Cacheable<K, T>> T get(K key, Class<T> typeClass) {
        return get(key, typeClass, true);
    }

    protected static <K, T extends Cacheable<K, T>> T get(K key, Class<T> typeClass, boolean canBlock) {
        return getInternalCache(typeClass).get(key, canBlock);
    }

    public synchronized void reload() {
        setStaleState(true);
        getInternalCache().get(getKey(), false);
    }

    protected static <K, T extends Cacheable<K, T>> void addListener(InternalCache.UpdateListener<T> l,
            Class<T> typeClass) {
        getInternalCache(typeClass).addListener(l);
    }

    protected static <K, T extends Cacheable<K, T>> void removeListener(InternalCache.UpdateListener<T> l,
            Class<T> typeClass) {
        getInternalCache(typeClass).removeListener(l);
    }

    protected static <K, T extends Cacheable<K, T>> void dumpListeners(Class<T> typeClass) {
        getInternalCache(typeClass).dumpListeners();
    }

    protected static <K, T extends Cacheable<K, T>> void dump(Class<T> typeClass) {
        getInternalCache(typeClass).dump();
    }

    public abstract void fill(Cursor cursor);

    @SuppressWarnings("unchecked")
    public void registerContentObserver() {
        if (mCacheableContentObserver == null) {
            /*
             * This entity is very important since it has been cached (as proved by the fact that it
             * is
             * being updated from another entity). Register for updates on this entity in database
             */

            log("Registering content observer for " + getClass().getName() + ":" + getKey());
            final InternalCache<K, T> internalCache = getInternalCache();

            mCacheableContentObserver = new CacheableContentObserver<K, T>(mKey, (Class<T>) getClass());
            ContentResolver contentResolver = internalCache.getContext().getContentResolver();
            contentResolver.registerContentObserver(internalCache.getCacheData().getUri(getKey()), false,
                    mCacheableContentObserver);
        }
    }

    @Override
    protected void finalize() {
        /* Since, this entity is being garbage collected. Unregister the observer */
        final InternalCache<K, T> internalCache = getInternalCache();
        log("Finalize() for " + getClass().getName() + ":" + getKey());
        if (internalCache != null && mCacheableContentObserver != null) {
            log("Unregistering content observer for " + getClass().getName() + ":" + getKey());
            internalCache.getContext().getContentResolver().unregisterContentObserver(mCacheableContentObserver);
        }
    }

    private static void log(String msg) {
        if (V) {
            LogUtils.LOGD(TAG, msg);
        }
    }

}
