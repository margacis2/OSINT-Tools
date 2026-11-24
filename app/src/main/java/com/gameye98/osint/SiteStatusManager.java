package com.gameye98.osint;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SiteStatusManager {

    private static final String PREF_NAME = "osint_site_status_pref";
    private static final String KEY_LAST_CHECK_VERSION = "last_check_version";
    private static final String KEY_LAST_CHECK_TIME = "last_check_time";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 4000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;
    private static final int THREAD_POOL_SIZE = 24;

    private static volatile SiteStatusManager instance;

    private final Context context;
    private final SiteStatusDbHelper dbHelper;
    private final ConcurrentHashMap<String, SiteCheckResult> statusCache = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExecutorService checkExecutor;
    private final AtomicBoolean isChecking = new AtomicBoolean(false);
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    public interface HealthCheckListener {
        void onProgress(int completed, int total, SiteCheckResult result);
        void onComplete(int workingCount, int deadCount);
    }

    public interface SingleCheckListener {
        void onResult(SiteCheckResult result);
    }

    private final List<HealthCheckListener> listeners = new ArrayList<>();

    private SiteStatusManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new SiteStatusDbHelper(this.context);
        loadCachedStatuses();
    }

    public static SiteStatusManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SiteStatusManager.class) {
                if (instance == null) {
                    instance = new SiteStatusManager(context);
                }
            }
        }
        return instance;
    }

    private void loadCachedStatuses() {
        Map<String, SiteCheckResult> all = dbHelper.getAllResults();
        if (all != null) {
            statusCache.putAll(all);
        }
    }

    public SiteStatus getStatus(String url) {
        if (url == null) return SiteStatus.PENDING;
        SiteCheckResult result = statusCache.get(url.trim());
        return result != null ? result.getStatus() : SiteStatus.PENDING;
    }

    public SiteCheckResult getResult(String url) {
        if (url == null) return null;
        return statusCache.get(url.trim());
    }

    public PricingType getPricing(String url) {
        if (url == null) return PricingType.UNKNOWN;
        SiteCheckResult result = statusCache.get(url.trim());
        return result != null ? result.getPricing() : PricingType.UNKNOWN;
    }

    public void addListener(HealthCheckListener listener) {
        synchronized (listeners) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeListener(HealthCheckListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public boolean isChecking() {
        return isChecking.get();
    }

    public void stopChecking() {
        cancelRequested.set(true);
        if (checkExecutor != null && !checkExecutor.isShutdown()) {
            checkExecutor.shutdownNow();
        }
        isChecking.set(false);
    }

    private int getPackageVersionCode() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Checks if the app was newly installed or updated, or if health check cache is empty.
     */
    public boolean shouldCheckOnLaunch() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int lastVersion = prefs.getInt(KEY_LAST_CHECK_VERSION, -1);
        int currentVersion = getPackageVersionCode();

        if (lastVersion < currentVersion) {
            return true;
        }

        // Also check if cache is empty
        return statusCache.isEmpty();
    }

    public void recordCheckCompleted() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_LAST_CHECK_VERSION, getPackageVersionCode())
                .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                .apply();
    }

    /**
     * Starts background health check across all tools with 24 parallel workers and batch writes.
     */
    public synchronized void startHealthCheck(final List<OsintTool> tools) {
        if (isChecking.get()) return;
        if (tools == null || tools.isEmpty()) return;

        isChecking.set(true);
        cancelRequested.set(false);

        if (checkExecutor != null && !checkExecutor.isShutdown()) {
            checkExecutor.shutdownNow();
        }
        checkExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        final int total = tools.size();
        final AtomicInteger completed = new AtomicInteger(0);
        final AtomicInteger workingCount = new AtomicInteger(0);
        final AtomicInteger deadCount = new AtomicInteger(0);
        final List<SiteCheckResult> batchBuffer = Collections.synchronizedList(new ArrayList<SiteCheckResult>());

        for (final OsintTool tool : tools) {
            if (cancelRequested.get()) break;

            checkExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (cancelRequested.get()) return;

                    SiteCheckResult result = SiteChecker.checkUrl(tool.getUrl(), DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
                    statusCache.put(tool.getUrl(), result);

                    batchBuffer.add(result);
                    if (batchBuffer.size() >= 25) {
                        List<SiteCheckResult> toFlush = new ArrayList<>();
                        synchronized (batchBuffer) {
                            toFlush.addAll(batchBuffer);
                            batchBuffer.clear();
                        }
                        dbHelper.saveBatch(toFlush);
                    }

                    if (result.isWorking()) {
                        workingCount.incrementAndGet();
                    } else if (result.isDead()) {
                        deadCount.incrementAndGet();
                    }

                    final int done = completed.incrementAndGet();
                    final SiteCheckResult finalRes = result;

                    if (done % 5 == 0 || done >= total) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyProgress(done, total, finalRes);
                                if (done >= total) {
                                    if (!batchBuffer.isEmpty()) {
                                        List<SiteCheckResult> remaining = new ArrayList<>();
                                        synchronized (batchBuffer) {
                                            remaining.addAll(batchBuffer);
                                            batchBuffer.clear();
                                        }
                                        dbHelper.saveBatch(remaining);
                                    }
                                    isChecking.set(false);
                                    recordCheckCompleted();
                                    notifyComplete(workingCount.get(), deadCount.get());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Recheck a single tool on demand (e.g. from Tool Details dialog).
     */
    public void recheckSite(final String url, final SingleCheckListener listener) {
        if (url == null || url.trim().isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final SiteCheckResult result = SiteChecker.checkUrl(url, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
                statusCache.put(url, result);
                dbHelper.saveResult(result);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onResult(result);
                        }
                    }
                });
            }
        });
    }

    private void notifyProgress(int done, int total, SiteCheckResult result) {
        synchronized (listeners) {
            for (HealthCheckListener l : listeners) {
                try {
                    l.onProgress(done, total, result);
                } catch (Exception ignored) {}
            }
        }
    }

    private void notifyComplete(int workingCount, int deadCount) {
        synchronized (listeners) {
            for (HealthCheckListener l : listeners) {
                try {
                    l.onComplete(workingCount, deadCount);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Sorts tools:
     * 1. Working sites on top, dead links on below.
     * 2. Within each status group, favorites come first.
     * 3. Secondary sort by title alphabetically.
     */
    public void sortTools(List<OsintTool> tools) {
        if (tools == null || tools.size() <= 1) return;

        Collections.sort(tools, new Comparator<OsintTool>() {
            @Override
            public int compare(OsintTool t1, OsintTool t2) {
                SiteStatus s1 = getStatus(t1.getUrl());
                SiteStatus s2 = getStatus(t2.getUrl());

                int p1 = s1.getSortPriority();
                int p2 = s2.getSortPriority();

                if (p1 != p2) {
                    return Integer.compare(p1, p2);
                }

                // Within same status, favorites first
                if (t1.isFavorite() != t2.isFavorite()) {
                    return t1.isFavorite() ? -1 : 1;
                }

                // Alphabetical
                return t1.getTitle().compareToIgnoreCase(t2.getTitle());
            }
        });
    }
}
