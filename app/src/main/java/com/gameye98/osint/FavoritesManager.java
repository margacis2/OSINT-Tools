package com.gameye98.osint;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String PREF_NAME = "osint_favorites";
    private static final String KEY_FAVORITES = "fav_urls";
    private static FavoritesManager instance;
    private final SharedPreferences prefs;
    private final Set<String> cachedFavorites;

    private FavoritesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet(KEY_FAVORITES, null);
        cachedFavorites = new HashSet<>(saved != null ? saved : new HashSet<String>());
    }

    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }

    public synchronized boolean isFavorite(String url) {
        return url != null && cachedFavorites.contains(url);
    }

    public synchronized boolean toggleFavorite(String url) {
        if (url == null) return false;
        boolean willBeFav = !cachedFavorites.contains(url);
        if (willBeFav) {
            cachedFavorites.add(url);
        } else {
            cachedFavorites.remove(url);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, new HashSet<>(cachedFavorites)).apply();
        return willBeFav;
    }

    public synchronized void setFavorite(String url, boolean isFav) {
        if (url == null) return;
        if (isFav) {
            cachedFavorites.add(url);
        } else {
            cachedFavorites.remove(url);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, new HashSet<>(cachedFavorites)).apply();
    }

    public synchronized int getCount() {
        return cachedFavorites.size();
    }
}
