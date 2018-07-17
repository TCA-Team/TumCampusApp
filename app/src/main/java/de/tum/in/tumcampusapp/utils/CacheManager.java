package de.tum.in.tumcampusapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import org.joda.time.Duration;

import java.io.File;
import java.io.IOException;

import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.api.tumonline.xml.XMLConverter;
import okhttp3.ResponseBody;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
@Deprecated
public class CacheManager {
    public static final int CACHE_TYP_DATA = 0;
    public static final int CACHE_TYP_IMAGE = 1;

    /**
     * Validity's for entries in seconds
     */
    public static final int VALIDITY_DO_NOT_CACHE = 0;
    public static final int VALIDITY_ONE_DAY = 86400;
    public static final int VALIDITY_TWO_DAYS = 2 * 86400;
    public static final int VALIDITY_FIVE_DAYS = 5 * 86400;
    public static final int VALIDITY_TEN_DAYS = 10 * 86400;
    public static final int VALIDITY_ONE_MONTH = 30 * 86400;

    private static SQLiteDatabase cacheDb;
    private final Context mContext;

    private static synchronized void initCacheDb(Context c) {
        if (cacheDb == null) {
            File dbFile = new File(c.getCacheDir()
                                    .getAbsolutePath() + "/cache.db");
            dbFile.getParentFile().mkdirs();
            cacheDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
    }

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CacheManager(Context context) {
        initCacheDb(context);
        mContext = context;

        // create table if needed
        cacheDb.execSQL("CREATE TABLE IF NOT EXISTS cache (url VARCHAR UNIQUE, data BLOB, " +
                        "validity VARCHAR, max_age VARCHAR, typ INTEGER)");

        // Delete all entries that are too old and delete corresponding image files
        cacheDb.beginTransaction();
        try (Cursor cur = cacheDb.rawQuery("SELECT data FROM cache WHERE datetime()>max_age AND typ=1", null)) {
            if (cur.moveToFirst()) {
                do {
                    File f = new File(cur.getString(0));
                    f.delete();
                } while (cur.moveToNext());
            }
        }
        cacheDb.execSQL("DELETE FROM cache WHERE datetime()>max_age");
        cacheDb.setTransactionSuccessful();
        cacheDb.endTransaction();
    }

    /**
     * Download usual tumOnline requests
     */
    public void fillCache() {

        // acquire access token
        if (!new AccessTokenManager(mContext).hasValidAccessToken()) {
            return;
        }

        // ALL STUFF BELOW HERE NEEDS A VALID ACCESS TOKEN

        // Sync organisation tree
        /*
        TUMOnlineRequest<OrgItemList> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getORG_TREE(), mContext);
        if (shouldRefresh(requestHandler.getRequestURL())) {
            requestHandler.fetch();
        }

        // Sync fee status
        TUMOnlineRequest<TuitionList> requestHandler2 = new TUMOnlineRequest<>(TUMOnlineConst.TUITION_FEE_STATUS, mContext);
        if (shouldRefresh(requestHandler2.getRequestURL())) {
            requestHandler2.fetch();
        }
        */

        // TODO: Move this stuff over to DownloadService?

        // Sync lectures, details and appointments
        importLecturesFromTUMOnline();

        // Sync calendar
        syncCalendar();
    }

    public void syncCalendar() {
        /*
        TUMOnlineRequest<Events> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getCALENDER(), mContext);
        requestHandler.setParameter("pMonateVor", "2");
        requestHandler.setParameter("pMonateNach", "3");
        if (shouldRefresh(requestHandler.getRequestURL())) {
            Optional<Events> set = requestHandler.fetch();
            if (set.isPresent()) {
                CalendarController calendarController = new CalendarController(mContext);
                calendarController.importCalendar(set.get());
                CalendarController.QueryLocationsService.loadGeo(mContext);
            }
        }
        */
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date and returns the cache content
     * if data is up to date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public Optional<String> getFromCache(String url) {
        String result = null;

        try (Cursor c = cacheDb.rawQuery("SELECT data FROM cache WHERE url=? AND datetime()<max_age", new String[]{url})) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = c.getString(0);
            }
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return Optional.fromNullable(result);
    }

    /**
     * Returns the cached content as an object of the desired class.
     * @param url The URL of the API call
     * @return The content of the cache
     */
    @Nullable
    public ResponseBody getResponseBodyFromCache(String url) {
        String content = getCachedResponseFromCache(url);
        return (content != null) ? XMLConverter.responseBody(content) : null;
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date and returns the cache content
     * if data is up to date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public String getCachedResponseFromCache(String url) {
        String result = null;
        String query = "SELECT data FROM cache WHERE url LIKE ? AND datetime() < max_age";
        try (Cursor c = cacheDb.rawQuery(query, new String[]{url})) {
            if (c.getCount() == 1) {
                c.moveToFirst();
                result = c.getString(0);
            }
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return result;
    }

    /**
     * Checks if a new sync is needed or if data is up-to-date
     *
     * @param url Url from which data was cached
     * @return Data if valid version was found, null if no data is available
     */
    public boolean shouldRefresh(String url) {
        boolean result = true;

        try (Cursor c = cacheDb.rawQuery("SELECT url FROM cache WHERE url=? AND datetime() < validity", new String[]{url})) {
            if (c.getCount() == 1) {
                result = false;
            }
        } catch (SQLiteException e) {
            Utils.log(e);
        }
        return result;
    }

    /**
     * Add a result to cache
     *
     * @param url  url from where the data was fetched
     * @param data result
     */
    public void addToCache(String url, String data, int validity, int typ) {
        if (validity == VALIDITY_DO_NOT_CACHE) {
            return;
        }

        cacheDb.execSQL("REPLACE INTO cache (url, data, validity, max_age, typ) " +
                        "VALUES (?, ?, datetime('now','+" + (validity / 2) + " seconds'), " +
                        "datetime('now','+" + validity + " seconds'), ?)",
                        new String[]{url, data, String.valueOf(typ)});
    }

    private void addToCache(String url, String body, Duration cachingDuration) {
        int validity = (int) cachingDuration.getMillis();
        addToCache(url, body, validity, CacheManager.CACHE_TYP_DATA);
    }

    /**
     * this function allows us to import all lecture items from TUMOnline
     */
    private void importLecturesFromTUMOnline() {
        /*
        // get my lectures
        TUMOnlineRequest<LecturesResponse> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, mContext);
        if (!shouldRefresh(requestHandler.getRequestURL())) {
            return;
        }

        Optional<LecturesResponse> lecturesList = requestHandler.fetch();
        if (!lecturesList.isPresent()) {
            return;
        }
        List<Lecture> lectures = lecturesList.get().getLectures();

        ChatRoomController manager = new ChatRoomController(mContext);
        manager.createLectureRooms(lectures);
        */
    }

    public static synchronized void clearCache(Context context) {
        cacheDb = null;
        try {
            Process proc = Runtime.getRuntime()
                                  .exec("rm -r " + context.getCacheDir());
            proc.waitFor();
        } catch (InterruptedException | IOException e) {
            Utils.log("couldn't delete cache files " + e.toString());
        }
    }
}