package com.gameye98.osint;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import java.io.InputStream;

public class IconManager {
    private static IconManager instance;
    private final LruCache<String, Drawable> memoryCache;

    private IconManager() {
        // Cache up to 150 icons in memory (~2-3MB RAM)
        final int cacheSize = 150;
        memoryCache = new LruCache<String, Drawable>(cacheSize);
    }

    public static synchronized IconManager getInstance() {
        if (instance == null) {
            instance = new IconManager();
        }
        return instance;
    }

    public void loadIcon(Context context, String iconName, ImageView targetView) {
        if (targetView == null) return;

        if (iconName == null || iconName.isEmpty() || iconName.equals("default")) {
            targetView.setImageResource(R.drawable.ic_search_web);
            return;
        }

        Drawable cached = memoryCache.get(iconName);
        if (cached != null) {
            targetView.setImageDrawable(cached);
            return;
        }

        try {
            AssetManager assets = context.getAssets();
            InputStream is = assets.open("icons/" + iconName);
            Drawable drawable = Drawable.createFromStream(is, null);
            is.close();

            if (drawable != null) {
                memoryCache.put(iconName, drawable);
                targetView.setImageDrawable(drawable);
            } else {
                targetView.setImageResource(R.drawable.ic_search_web);
            }
        } catch (Exception e) {
            targetView.setImageResource(R.drawable.ic_search_web);
        }
    }

    public Drawable getIconDrawable(Context context, String iconName) {
        if (iconName == null || iconName.isEmpty() || iconName.equals("default")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_search_web);
        }

        Drawable cached = memoryCache.get(iconName);
        if (cached != null) {
            return cached;
        }

        try {
            AssetManager assets = context.getAssets();
            InputStream is = assets.open("icons/" + iconName);
            Drawable drawable = Drawable.createFromStream(is, null);
            is.close();
            if (drawable != null) {
                memoryCache.put(iconName, drawable);
                return drawable;
            }
        } catch (Exception ignored) {
        }
        return ContextCompat.getDrawable(context, R.drawable.ic_search_web);
    }
}
