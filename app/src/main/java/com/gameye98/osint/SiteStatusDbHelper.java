package com.gameye98.osint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteStatusDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "site_status.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_STATUS = "site_status";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_HTTP_CODE = "http_code";
    public static final String COLUMN_FINAL_URL = "final_url";
    public static final String COLUMN_PRICING = "pricing";
    public static final String COLUMN_LAST_CHECKED = "last_checked";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_STATUS + " (" +
                    COLUMN_URL + " TEXT PRIMARY KEY, " +
                    COLUMN_STATUS + " TEXT, " +
                    COLUMN_HTTP_CODE + " INTEGER, " +
                    COLUMN_FINAL_URL + " TEXT, " +
                    COLUMN_PRICING + " TEXT, " +
                    COLUMN_LAST_CHECKED + " INTEGER);";

    public SiteStatusDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        onCreate(db);
    }

    public synchronized void saveResult(SiteCheckResult result) {
        if (result == null || result.getUrl().isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, result.getUrl());
        values.put(COLUMN_STATUS, result.getStatus().name());
        values.put(COLUMN_HTTP_CODE, result.getHttpCode());
        values.put(COLUMN_FINAL_URL, result.getFinalUrl());
        values.put(COLUMN_PRICING, result.getPricing().name());
        values.put(COLUMN_LAST_CHECKED, result.getLastCheckedTimestamp());
        db.insertWithOnConflict(TABLE_STATUS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized void saveBatch(List<SiteCheckResult> results) {
        if (results == null || results.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (SiteCheckResult result : results) {
                if (result == null || result.getUrl().isEmpty()) continue;
                ContentValues values = new ContentValues();
                values.put(COLUMN_URL, result.getUrl());
                values.put(COLUMN_STATUS, result.getStatus().name());
                values.put(COLUMN_HTTP_CODE, result.getHttpCode());
                values.put(COLUMN_FINAL_URL, result.getFinalUrl());
                values.put(COLUMN_PRICING, result.getPricing().name());
                values.put(COLUMN_LAST_CHECKED, result.getLastCheckedTimestamp());
                db.insertWithOnConflict(TABLE_STATUS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized Map<String, SiteCheckResult> getAllResults() {
        Map<String, SiteCheckResult> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_STATUS, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int urlIdx = cursor.getColumnIndex(COLUMN_URL);
                int statusIdx = cursor.getColumnIndex(COLUMN_STATUS);
                int codeIdx = cursor.getColumnIndex(COLUMN_HTTP_CODE);
                int finalIdx = cursor.getColumnIndex(COLUMN_FINAL_URL);
                int pricingIdx = cursor.getColumnIndex(COLUMN_PRICING);
                int lastIdx = cursor.getColumnIndex(COLUMN_LAST_CHECKED);

                do {
                    String url = cursor.getString(urlIdx);
                    String statusStr = cursor.getString(statusIdx);
                    int code = cursor.getInt(codeIdx);
                    String finalUrl = cursor.getString(finalIdx);
                    String pricingStr = cursor.getString(pricingIdx);
                    long lastChecked = cursor.getLong(lastIdx);

                    SiteStatus status = SiteStatus.fromString(statusStr);
                    PricingType pricing = PricingType.fromString(pricingStr);

                    map.put(url, new SiteCheckResult(url, status, code, finalUrl, pricing, lastChecked));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return map;
    }

    public synchronized SiteCheckResult getResult(String url) {
        if (url == null || url.isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_STATUS, null, COLUMN_URL + "=?", new String[]{url}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                int code = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HTTP_CODE));
                String finalUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FINAL_URL));
                String pricingStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICING));
                long lastChecked = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_CHECKED));

                SiteStatus status = SiteStatus.fromString(statusStr);
                PricingType pricing = PricingType.fromString(pricingStr);

                return new SiteCheckResult(url, status, code, finalUrl, pricing, lastChecked);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public synchronized void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_STATUS, null, null);
    }
}
