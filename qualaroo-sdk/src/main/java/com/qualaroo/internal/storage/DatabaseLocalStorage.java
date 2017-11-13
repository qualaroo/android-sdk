package com.qualaroo.internal.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class DatabaseLocalStorage implements LocalStorage {

    static final String DB_NAME = "qualaroo.db";

    private static final String FAILED_REPORTS_TABLE = "failedReports";
    private static final String FAILED_REPORTS_ID = "reportId";
    private static final String FAILED_REPORTS_URL = "reportUrl";

    private static final String USER_PROPERTIES_TABLE = "userProperties";
    private static final String USER_PROPERTIES_KEY = "propertyKey";
    private static final String USER_PROPERTIES_VALUE = "propertyValue";

    private static final String SURVEY_STATUS_TABLE = "surveyStatus";
    private static final String SURVEY_STATUS_ID = "surveyId";
    private static final String SURVEY_STATUS_HAS_SEEN = "hasBeenSeen";
    private static final String SURVEY_STATUS_HAS_FINISHED = "hasBeenFinished";
    private static final String SURVEY_STATUS_TIMESTAMP = "seenAt";

    private static final String USER_GROUP_PERCENT_SURVEY_TABLE = "userGroupPercentTable";
    private static final String USER_GROUP_PERCENT_SURVEY_ID = "surveyId";
    private static final String USER_GROUP_PERCENT_SURVEY_VALUE = "percent";
    private final SQLiteOpenHelper dbHelper;

    public DatabaseLocalStorage(Context context) {
        this.dbHelper = new QualarooSQLiteOpenHelper(context, DB_NAME);
    }

    @Override public void storeFailedReportRequest(String reportRequestUrl) {
        ContentValues values = new ContentValues();
        values.put(FAILED_REPORTS_URL, reportRequestUrl);
        writeableDb().insert(FAILED_REPORTS_TABLE, null, values);
    }

    @Override public void removeReportRequest(String reportRequestUrl) {
        delete(FAILED_REPORTS_TABLE, FAILED_REPORTS_URL + "=?", new String[]{reportRequestUrl});
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
            //ignore
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return requests;
    }

    @Override public int getFailedRequestsCount() {
        try {
            return (int) DatabaseUtils.queryNumEntries(writeableDb(), FAILED_REPORTS_TABLE);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override public void markSurveyAsSeen(Survey survey) {
        ContentValues values = new ContentValues();
        values.put(SURVEY_STATUS_ID, survey.id());
        values.put(SURVEY_STATUS_HAS_SEEN, true);
        values.put(SURVEY_STATUS_TIMESTAMP, System.currentTimeMillis());
        insertOrUpdate(SURVEY_STATUS_TABLE, values, SURVEY_STATUS_ID + "=?", new String[]{String.valueOf(survey.id())});
    }

    @Override public void markSurveyFinished(Survey survey) {
        ContentValues values = new ContentValues();
        values.put(SURVEY_STATUS_ID, survey.id());
        values.put(SURVEY_STATUS_HAS_FINISHED, true);
        values.put(SURVEY_STATUS_TIMESTAMP, System.currentTimeMillis());
        insertOrUpdate(SURVEY_STATUS_TABLE, values, SURVEY_STATUS_ID + "=?", new String[]{String.valueOf(survey.id())});
    }

    @Override public SurveyStatus getSurveyStatus(Survey survey) {
        SurveyStatus.Builder builder = SurveyStatus.builder();
        builder.setSurveyId(survey.id());
        Cursor cursor = null;
        try {
            cursor = writeableDb().query(
                    SURVEY_STATUS_TABLE,
                    new String[]{SURVEY_STATUS_HAS_SEEN, SURVEY_STATUS_HAS_FINISHED, SURVEY_STATUS_TIMESTAMP},
                    SURVEY_STATUS_ID + "=?", new String[]{String.valueOf(survey.id())},
                    null, null, null, String.valueOf(1));
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                builder.setHasBeenSeen(cursor.getInt(0) > 0);
                builder.setHasBeenFinished(cursor.getInt(1) > 0);
                builder.setSeenAtInMillis(cursor.getInt(2));
            }
        } catch (Exception ex) {
            QualarooLogger.debug("Could not acquire survey status for survey: " + survey.canonicalName());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return builder.build();
    }

    @Override public void updateUserProperty(@NonNull String key, @Nullable String value) {
        if (value == null) {
            delete(USER_PROPERTIES_TABLE, USER_PROPERTIES_KEY + "=?", new String[]{key});
        } else {
            ContentValues values = new ContentValues();
            values.put(USER_PROPERTIES_KEY, key);
            values.put(USER_PROPERTIES_VALUE, value);
            insertOrUpdate(USER_PROPERTIES_TABLE, values, USER_PROPERTIES_KEY + "=?", new String[]{key});
        }
    }

    @Override public Map<String, String> getUserProperties() {
        Map<String, String> properties = new LinkedHashMap<>();
        Cursor c = null;
        try {
            c = writeableDb().query(
                    USER_PROPERTIES_TABLE,
                    new String[]{USER_PROPERTIES_KEY, USER_PROPERTIES_VALUE},
                    null, null, null, null, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                properties.put(c.getString(0), c.getString(1));
                c.moveToNext();
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return properties;
    }

    @Override public void storeUserGroupPercent(Survey survey, int percent) {
        ContentValues values = new ContentValues();
        values.put(USER_GROUP_PERCENT_SURVEY_ID, survey.id());
        values.put(USER_GROUP_PERCENT_SURVEY_VALUE, percent);
        insertOrUpdate(USER_GROUP_PERCENT_SURVEY_TABLE, values, USER_GROUP_PERCENT_SURVEY_ID + "=?", new String[]{String.valueOf(survey.id())});
    }

    @Nullable @Override public Integer getUserGroupPercent(Survey survey) {
        Cursor cursor = null;
        Integer result = null;
        try {
            cursor = writeableDb().query(
                    USER_GROUP_PERCENT_SURVEY_TABLE,
                    new String[]{USER_GROUP_PERCENT_SURVEY_VALUE},
                    USER_GROUP_PERCENT_SURVEY_ID + "=?", new String[]{String.valueOf(survey.id())},
                    null, null, null, String.valueOf(1));
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                result = cursor.getInt(0);
            }
        } catch (Exception e) {
            result = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }


    private void insertOrUpdate(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        int affectedRows = writeableDb().update(tableName, values, whereClause, whereArgs);
        if (affectedRows == 0) {
            writeableDb().insert(tableName, null, values);
        }
    }

    private void delete(String tableName, String whereClause, String[] whereArgs) {
        writeableDb().delete(tableName, whereClause, whereArgs);
    }

    private SQLiteDatabase writeableDb() {
        return dbHelper.getWritableDatabase();
    }

    private static class QualarooSQLiteOpenHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 2;

        QualarooSQLiteOpenHelper(Context context, String databaseName) {
            super(context, databaseName, null, DB_VERSION);
        }

        @Override public void onCreate(SQLiteDatabase db) {
            createFailedReportsTable(db);
            createUserPropertiesTable(db);
            createSurveyStatusTable(db);
            createUserPercentGroupTable(db);
        }

        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                createUserPercentGroupTable(db);
            }
        }

        private void createUserPercentGroupTable(SQLiteDatabase db) {
            String userPercentGroupTable = format(
                    "CREATE TABLE %1$s (" +
                            "%2$s INTEGER PRIMARY KEY," +
                            "%3$s INTEGER DEFAULT 0);",
                    USER_GROUP_PERCENT_SURVEY_TABLE,
                    USER_GROUP_PERCENT_SURVEY_ID,
                    USER_GROUP_PERCENT_SURVEY_VALUE
            );
            db.execSQL(userPercentGroupTable);
        }

        private void createSurveyStatusTable(SQLiteDatabase db) {
            String surveyStatusTable = format(
                    "CREATE TABLE %1$s (" +
                            "%2$s INTEGER PRIMARY KEY," +
                            "%3$s INTEGER DEFAULT 0," +
                            "%4$s INTEGER DEFAULT 0," +
                            "%5$s TIMESTAMP DEFAULT 0 NOT NULL);",
                    SURVEY_STATUS_TABLE,
                    SURVEY_STATUS_ID,
                    SURVEY_STATUS_HAS_SEEN,
                    SURVEY_STATUS_HAS_FINISHED,
                    SURVEY_STATUS_TIMESTAMP
            );
            db.execSQL(surveyStatusTable);
        }

        private void createUserPropertiesTable(SQLiteDatabase db) {
            String userPropertiesTable = format(
                    "CREATE TABLE %1$s (" +
                            "%2$s TEXT PRIMARY KEY NOT NULL," +
                            "%3$s TEXT);",
                    USER_PROPERTIES_TABLE,
                    USER_PROPERTIES_KEY,
                    USER_PROPERTIES_VALUE
            );
            db.execSQL(userPropertiesTable);
        }

        private void createFailedReportsTable(SQLiteDatabase db) {
            String failedReportsTable = format(
                    "CREATE TABLE %1$s (" +
                            "%2$s INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "%3$s TEXT NOT NULL);",
                    FAILED_REPORTS_TABLE,
                    FAILED_REPORTS_ID,
                    FAILED_REPORTS_URL
            );
            db.execSQL(failedReportsTable);
        }

        private static String format(String string, Object... args) {
            return String.format(Locale.ROOT, string, args);
        }
    }

}
