package com.grootcode.android.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.grootcode.android.util.LogUtils;

public class InternalCache<K, T extends Cacheable<K, T>> {
    private static final String TAG = LogUtils.makeLogTag(InternalCache.class);
    private static final boolean V = false;

    private final Set<UpdateListener<T>> mListeners = Collections
            .newSetFromMap(new WeakHashMap<UpdateListener<T>, Boolean>());

    public interface UpdateListener<T extends Cacheable<?, T>> {
        public void onUpdate(T updated);
    }

    public interface CacheData<K, T extends Cacheable<K, T>> {
        String[] getProjection();

        Uri getUri(K key);

        T getEmptyInstance(K key);
    }

    private final TaskStack mTaskQueue = new TaskStack();
    private final Context mContext;
    private final CacheData<K, T> mCacheData;

    /**
     * TODO: change to {@link LruCache}
     */
    private final HashMap<K, T> mCacheHash = new HashMap<K, T>();

    public InternalCache(Context context, CacheData<K, T> cacheData) {
        mContext = context;
        mCacheData = cacheData;
    }

    public Context getContext() {
        return mContext;
    }

    public CacheData<K, T> getCacheData() {
        return mCacheData;
    }

    private static void logWithTrace(String msg, Object... format) {
        Thread current = Thread.currentThread();
        StackTraceElement[] stack = current.getStackTrace();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(current.getId());
        sb.append("] ");
        sb.append(String.format(msg, format));

        sb.append(" <- ");
        int stop = Math.min(stack.length, 7);
        for (int i = 3; i < stop; i++) {
            String className = stack[i].getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            String methodName = stack[i].getMethodName();
            int lineNumber = stack[i].getLineNumber();
            sb.append(className + "." + methodName + "(" + lineNumber + ")");
            if ((i + 1) != stop) {
                sb.append(" <- ");
            }
        }

        LogUtils.LOGD(TAG, sb.toString());
    }

    synchronized void dump() {
        LogUtils.LOGD(TAG, "**** Cacheable cache dump ****");
        for (K key : mCacheHash.keySet()) {
            T c = mCacheHash.get(key);
            LogUtils.LOGD(TAG, key + " ==> " + c.toString());
        }
    }

    private static class TaskStack {
        Thread mWorkerThread;
        private final ArrayList<Runnable> mThingsToLoad;

        public TaskStack() {
            mThingsToLoad = new ArrayList<Runnable>();
            mWorkerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Runnable r = null;
                        synchronized (mThingsToLoad) {
                            if (mThingsToLoad.size() == 0) {
                                try {
                                    mThingsToLoad.wait();
                                } catch (InterruptedException ex) {
                                    // nothing to do
                                }
                            }
                            if (mThingsToLoad.size() > 0) {
                                r = mThingsToLoad.remove(0);
                            }
                        }
                        if (r != null) {
                            r.run();
                        }
                    }
                }
            });
            mWorkerThread.start();
        }

        public void push(Runnable r) {
            synchronized (mThingsToLoad) {
                mThingsToLoad.add(r);
                mThingsToLoad.notify();
            }
        }
    }

    public void pushTask(Runnable r) {
        mTaskQueue.push(r);
    }

    public void addListener(UpdateListener<T> l) {
        synchronized (mListeners) {
            mListeners.add(l);
        }
    }

    public void removeListener(UpdateListener<T> l) {
        synchronized (mListeners) {
            mListeners.remove(l);
        }
    }

    public void dumpListeners() {
        synchronized (mListeners) {
            int i = 0;
            LogUtils.LOGI(TAG, "[Stock] dumpListeners; size=" + mListeners.size());
            for (UpdateListener<T> listener : mListeners) {
                LogUtils.LOGI(TAG, "[" + (i++) + "]" + listener);
            }
        }
    }

    public T get(K key, boolean canBlock) {

        if (key == null) {
            throw new IllegalArgumentException("Illegal value of key : " + key);
        }

        /*
         * Always returns a Cacheable object, even if we don't have an actual
         * Cacheable in the Cacheables db.
         */
        T cacheable = internalGet(key);

        if (V)
            logWithTrace("get(%s, %s, %s)", cacheable.getClass().getName(), key, canBlock);

        Runnable r = null;

        synchronized (cacheable) {
            /*
             * If there's a query pending and we're willing to block then wait
             * here until the query completes.
             */
            while (canBlock && cacheable.isQueryPending()) {
                try {
                    log("Executing wait on " + cacheable.getClass() + ":" + cacheable.getKey());
                    cacheable.wait();
                } catch (InterruptedException ex) {
                    /*
                     * try again by virtue of the loop unless mQueryPending is
                     * false
                     */
                }
                log("Wait over on " + cacheable.getClass() + ":" + cacheable.getKey());
            }
            /*
             * If we're stale and we haven't already kicked off a query then
             * kick it off here.
             */
            if (cacheable.isStaleState() && !cacheable.isQueryPending()) {
                log("async update for " + cacheable + " canBlock: " + canBlock + " isStale: "
                        + cacheable.isStaleState());

                cacheable.setStaleState(false);

                final T c = cacheable;
                r = new Runnable() {
                    @Override
                    public void run() {
                        updateCacheable(c);
                    }
                };

                /*
                 * set this to true while we have the lock on {@link Cacheable} since we
                 * will either run the query directly (canBlock case) or push
                 * the query onto the queue. In either case the mQueryPending
                 * will get set to false via updateCacheable.
                 */
                cacheable.setQueryPending(true);
            }
        }
        /*
         * do this outside of the synchronized so we don't hold up any
         * subsequent calls to "get" on other threads
         */
        if (r != null) {
            if (canBlock) {
                r.run();
            } else {
                pushTask(r);
            }
        }
        return cacheable;
    }

    private boolean cacheableChanged(T orig, T newCacheable) {
        return !orig.equals(newCacheable);
    }

    @SuppressWarnings("unchecked")
    private void updateCacheable(final T c) {
        if (c == null) {
            return;
        }

        T cacheable = getCacheableInfo(c.getKey());

        synchronized (c) {
            if (cacheableChanged(c, cacheable)) {
                log("updateCacheable: cacheable changed for " + cacheable.getClass().getSimpleName() + ":"
                        + cacheable.getKey());

                c.update(cacheable);
                /*
                 * clone the list of listeners in case the onUpdate call turns
                 * around and modifies the list of listeners access to
                 * mListeners is synchronized on CacheablesCache
                 */
                if (!mListeners.isEmpty()) {
                    final Object[] listeners;
                    synchronized (mListeners) {
                        listeners = mListeners.toArray();
                    }

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < listeners.length; ++i) {
                                UpdateListener<T> l = (UpdateListener<T>) listeners[i];
                                if (V) {
                                    LogUtils.LOGD(TAG, "updating UpdateListener " + l);
                                }
                                l.onUpdate(c);
                            }
                        }
                    };
                    pushTask(r);
                }
                log("cacheable Updated: cacheable " + cacheable.getClass().getSimpleName() + ":" + cacheable.getKey());
            }
            c.registerContentObserver();

            c.setQueryPending(false);
            c.notifyAll();
        }
    }

    /**
     * Returns the {@link Cacheable} for {@code key}
     * 
     * @param key
     * @return {@link Cacheable}
     */
    protected T getCacheableInfo(K key) {
        T c = mCacheData.getEmptyInstance(key);

        log("getCacheableInfo for " + c.getClass().getSimpleName() + ":" + key);

        Uri uri = mCacheData.getUri(key);
        Cursor cursor = mContext.getContentResolver().query(uri, mCacheData.getProjection(), null, null, null);

        if (cursor == null) {
            LogUtils.LOGW(TAG, "queryCacheableByKey(" + key + ") returned NULL cursor! cacheable uri used " + uri);
            return c;
        }

        try {
            if (cursor.moveToFirst()) {
                fillCacheable(c, cursor);
            }
        } finally {
            cursor.close();
        }

        return c;
    }

    private void fillCacheable(final T cacheable, final Cursor cursor) {
        synchronized (cacheable) {
            cacheable.fill(cursor);
        }
    }

    protected synchronized T internalGet(K key) {
        T cacheable = mCacheHash.get(key);
        if (cacheable == null) {
            cacheable = mCacheData.getEmptyInstance(key);
            mCacheHash.put(key, cacheable);
        }
        return cacheable;
    }

    synchronized void invalidate() {
        /*
         * Don't remove the Cacheables. Just mark them stale so we'll update their info,
         * particularly their presence.
         */
        for (T c : mCacheHash.values()) {
            synchronized (c) {
                c.setStaleState(true);
            }
        }
    }

    private static void log(String msg) {
        if (V)
            LogUtils.LOGD(TAG, msg);
    }
}
