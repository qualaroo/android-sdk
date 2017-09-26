package com.qualaroo.internal.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class DatabaseLocalStorage implements LocalStorage {

    static final String DB_NAME = "qualaroo.db";

    private static final String FAILED_REPORTS_TABLE = "failedReports";
    private static final String FAILED_REPORTS_ID = "reportId";
    private static final String FAILED_REPORTS_URL = "reportUrl";

    private final SQLiteOpenHelper dbHelper;

    DatabaseLocalStorage(Context context) {
        this.dbHelper = new QualarooSQLiteOpenHelper(context, DB_NAME);
    }

    @Override public void storeFailedReportRequest(String reportRequestUrl) {
        ContentValues values = new ContentValues();
        values.put(FAILED_REPORTS_URL, reportRequestUrl);
        writeableDb().insert(FAILED_REPORTS_TABLE, null, values);
    }

    @Override public void removeReportRequest(String reportRequestUrl) {
        writeableDb().delete(FAILED_REPORTS_TABLE, FAILED_REPORTS_URL + "=?", new String[]{reportRequestUrl});
    }

    @Override public List<String> getFailedReportRequests(int numOfRequests) {
        List<String> requests = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = writeableDb().query(FAILED_REPORTS_TABLE, new String[]{FAILED_REPORTS_URL}, null, null, null, null, null, String.valueOf(numOfRequests));
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                requests.add(cursor.getString(0));
                cursor.moveToNext();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return requests;
    }

    @Override public void markSurveyAsSeen(Survey survey) {

    }

    @Override public void markSurveyFinished(Survey survey) {

    }

    @Override public SurveyStatus getSurveyStatus(Survey survey) {
        return null;
    }

    @Override public void updateUserProperty(@NonNull String key, @Nullable String value) {

    }

    @Override public Map<String, String> getUserProperties() {
        return null;
    }

    private SQLiteDatabase writeableDb() {
        return dbHelper.getWritableDatabase();
    }

    private static class QualarooSQLiteOpenHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;

        QualarooSQLiteOpenHelper(Context context, String databaseName) {
            super(context, databaseName, null, DB_VERSION);
        }

        @Override public void onCreate(SQLiteDatabase db) {
            String createFailedReportsTable = format(
                    "CREATE TABLE %1$s (" +
                            "%2$s INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "%3$s TEXT NOT NULL" +
                            ");",
                    FAILED_REPORTS_TABLE,
                    FAILED_REPORTS_ID,
                    FAILED_REPORTS_URL
            );
            db.execSQL(createFailedReportsTable);
        }

        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //no upgrade yet
        }


    }
    private static String format(String string, Object... args) {
        return String.format(Locale.ROOT, string, args);
    }

}
