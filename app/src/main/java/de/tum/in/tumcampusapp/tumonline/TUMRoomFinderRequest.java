package de.tum.in.tumcampusapp.tumonline;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.managers.CacheManager;
import de.tum.in.tumcampusapp.models.tumo.Geo;

/**
 * Base class for communication with TUMRoomFinder
 */
public class TUMRoomFinderRequest {

    // Json keys
    public static final String KEY_ARCH_ID = "arch_id";
    public static final String KEY_MAP_ID = "map_id";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ROOM_ID = "room_id";
    public static final String KEY_CAMPUS_ID = "campus";
    public static final String KEY_CAMPUS_TITLE = "name";
    public static final String KEY_BUILDING_TITLE = "address";
    public static final String KEY_ROOM_TITLE = "info";
    public static final String KEY_UTM_ZONE = "utm_zone";
    public static final String KEY_UTM_EASTING = "utm_easting";
    public static final String KEY_UTM_NORTHING = "utm_northing";

    // Api urls
    private static final String API_BASE_URL = "https://tumcabe.in.tum.de/Api/roomfinder/";

    private static final String API_URL_SEARCH = API_BASE_URL + "room/search/";
    private static final String API_URL_DEFAULT_MAP = API_BASE_URL + "room/defaultMap/";
    private static final String API_URL_MAP = API_BASE_URL + "room/map/";
    private static final String API_URL_COORDINATES = API_BASE_URL + "room/coordinates/";
    private static final String API_URL_AVAILABLE_MAPS = API_BASE_URL + "room/availableMaps/";
    private static final String API_URL_SCHEDULE = API_BASE_URL + "room/scheduleById/";
    private final NetUtils net;
    /**
     * asynchronous task for interactive fetch
     */
    private AsyncTask<String, Void, List<Map<String, String>>> backgroundTask;

    public TUMRoomFinderRequest(Context context) {
        net = new NetUtils(context);
    }

    /**
     * returns the url to get the default map
     *
     * @param archId architecture id
     * @return url of default map
     */
    public static String fetchDefaultMap(String archId) {
        return API_URL_DEFAULT_MAP + encodeUrl(archId);
    }

    /**
     * returns the url for any map
     *
     * @param archId architecture id
     * @param mapId  map id
     * @return url of map
     */
    public static String fetchMap(String archId, String mapId) {
        return API_URL_MAP + encodeUrl(archId) + '/' + encodeUrl(mapId);
    }

    /**
     * encodes an url
     *
     * @param pUrl input url
     * @return encoded url
     */
    private static String encodeUrl(String pUrl) {
        String url = pUrl.replace("/", ""); //remove slashes in queries as this breaks the url
        return UrlEscapers.urlPathSegmentEscaper().escape(url);
    }

    /**
     * Converts UTM based coordinates to latitude and longitude based format
     */
    private static Geo convertUTMtoLL(double north, double east, double zone) {
        double d = 0.99960000000000004;
        double d1 = 6378137;
        double d2 = 0.0066943799999999998;
        double d4 = (1 - Math.sqrt(1 - d2)) / (1 + Math.sqrt(1 - d2));
        double d15 = east - 500000;
        double d11 = (zone - 1) * 6 - 180 + 3;
        double d3 = d2 / (1 - d2);
        double d10 = north / d;
        double d12 = d10 / (d1 * (1 - d2 / 4 - (3 * d2 * d2) / 64 - (5 * Math.pow(d2, 3)) / 256));
        double d14 = d12 + ((3 * d4) / 2 - (27 * Math.pow(d4, 3)) / 32) * Math.sin(2 * d12) + ((21 * d4 * d4) / 16 - (55 * Math.pow(d4, 4)) / 32) * Math.sin(4 * d12) + ((151 * Math.pow(d4, 3)) / 96) * Math.sin(6 * d12);
        double d5 = d1 / Math.sqrt(1 - d2 * Math.sin(d14) * Math.sin(d14));
        double d6 = Math.tan(d14) * Math.tan(d14);
        double d7 = d3 * Math.cos(d14) * Math.cos(d14);
        double d8 = (d1 * (1 - d2)) / Math.pow(1 - d2 * Math.sin(d14) * Math.sin(d14), 1.5);
        double d9 = d15 / (d5 * d);
        double d17 = d14 - ((d5 * Math.tan(d14)) / d8) * ((d9 * d9) / 2 - ((5 + 3 * d6 + 10 * d7 - 4 * d7 * d7 - 9 * d3) * Math.pow(d9, 4)) / 24 + ((61 + 90 * d6 + 298 * d7 + 45 * d6 * d6 - 252 * d3 - 3 * d7 * d7) * Math.pow(d9, 6)) / 720);
        d17 *= 180 / Math.PI;
        double d18 = (d9 - ((1 + 2 * d6 + d7) * Math.pow(d9, 3)) / 6 + ((5 - 2 * d7 + 28 * d6 - 3 * d7 * d7 + 8 * d3 + 24 * d6 * d6) * Math.pow(d9, 5)) / 120) / Math.cos(d14);
        d18 = d11 + d18 * 180 / Math.PI;
        return new Geo(d17, d18);
    }

    public void cancelRequest(boolean mayInterruptIfRunning) {
        // Cancel background task just if one has been established
        if (backgroundTask != null) {
            backgroundTask.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * fetches the room coordinates
     *
     * @param archId architecture id
     * @return coordinates of the room
     */
    public Optional<Geo> fetchCoordinates(String archId) {

        String url = API_URL_COORDINATES + encodeUrl(archId);
        Geo result = null;

        try {
            Optional<JSONObject> jsonObject = net.downloadJson(url);
            if (jsonObject.isPresent()) {
                double zone = jsonObject.get().getDouble(KEY_UTM_ZONE);
                double easting = jsonObject.get().getDouble(KEY_UTM_EASTING);
                double northing = jsonObject.get().getDouble(KEY_UTM_NORTHING);
                result = convertUTMtoLL(northing, easting, zone);
            }
        } catch (JSONException e) {
            Utils.log(String.valueOf(e));
        }

        // if something went wrong
        return Optional.fromNullable(result);
    }

    /**
     * fetches all rooms that match the search string
     *
     * @param searchString string that was entered by the user
     * @return list of HashMaps representing rooms, Map: attributes -> values
     */
    public List<Map<String, String>> fetchRooms(String searchString) {

        String url = API_URL_SEARCH + encodeUrl(searchString);
        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

        List<Map<String, String>> roomsList = new ArrayList<>();
        try {
            if (jsonArray.isPresent()) {
                JSONArray arr = jsonArray.get();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Map<String, String> roomMap = new HashMap<>();
                    roomMap.put(KEY_CAMPUS_ID, obj.getString(KEY_CAMPUS_ID));
                    roomMap.put(KEY_CAMPUS_TITLE, obj.getString(KEY_CAMPUS_TITLE));
                    roomMap.put(KEY_BUILDING_TITLE, obj.getString(KEY_BUILDING_TITLE));
                    roomMap.put(KEY_ROOM_TITLE, obj.getString(KEY_ROOM_TITLE));
                    roomMap.put(KEY_ARCH_ID, obj.getString(KEY_ARCH_ID));
                    roomMap.put(KEY_ROOM_ID, obj.getString(KEY_ROOM_ID));

                    // adding HashList to ArrayList
                    roomsList.add(roomMap);
                }
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return roomsList;
    }

    /**
     * fetches all available maps of the room or building
     *
     * @param archId architecture id
     * @return list of HashMap representing available maps
     */
    public List<Map<String, String>> fetchAvailableMaps(String archId) {

        String url = API_URL_AVAILABLE_MAPS + encodeUrl(archId);

        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);
        List<Map<String, String>> mapsList = new ArrayList<>();

        try {
            if (jsonArray.isPresent()) {
                JSONArray arr = jsonArray.get();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Map<String, String> mapMap = new HashMap<>();
                    mapMap.put(KEY_MAP_ID, obj.getString(KEY_MAP_ID));
                    mapMap.put(KEY_DESCRIPTION, obj.getString(KEY_DESCRIPTION));

                    // adding HashList to ArrayList
                    mapsList.add(mapMap);
                }
            }
        } catch (JSONException e) {
            Utils.log(String.valueOf(e));
        }

        return mapsList;
    }

    /**
     * fetches the room schedule for a given room
     *
     * @param roomId roomId
     * @return List of Events
     */
    public List<IntegratedCalendarEvent> fetchRoomSchedule(String roomId, String startDate, String endDate, List<IntegratedCalendarEvent> scheduleList) {

        String url = API_URL_SCHEDULE + encodeUrl(roomId) + '/' + encodeUrl(startDate) + '/' + encodeUrl(endDate);

        Optional<JSONArray> jsonArray = net.downloadJsonArray(url, CacheManager.VALIDITY_DO_NOT_CACHE, true);

        if (!jsonArray.isPresent()) {
            return scheduleList;
        }

        JSONArray arr = jsonArray.get();

        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String start = obj.getString("start");
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(Utils.getISODateTime(start));

                String end = obj.getString("end");
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(Utils.getISODateTime(end));
                IntegratedCalendarEvent event = new IntegratedCalendarEvent(
                        obj.getLong("event_id"),
                        obj.getString(Const.JSON_TITLE),
                        startCal,
                        endCal,
                        "",
                        IntegratedCalendarEvent.getDisplayColorFromColor(0xff28921f)
                );
                scheduleList.add(event);
            }
        } catch (JSONException e) {
            Utils.log(e);
        }

        return scheduleList;
    }

    /**
     * this fetch method will fetch the data from the TUMRoomFinder Request and
     * will address the listeners onFetch if the fetch succeeded, else the
     * onFetchError will be called
     *
     * @param context      the current context (may provide the current activity)
     * @param listener     the listener, which takes the result
     * @param searchString Text to search for
     */
    public void fetchSearchInteractive(final Context context,
                                       final TUMRoomFinderRequestFetchListener listener,
                                       String searchString) {

        // fetch information in a background task and show progress dialog in
        // meantime
        backgroundTask = new AsyncTask<String, Void, List<Map<String, String>>>() {

            /**
             * property to determine if there is an internet connection
             */
            boolean isOnline;

            @Override
            protected List<Map<String, String>> doInBackground(
                    String... searchString) {
                // set parameter on the TUMRoomFinder request an fetch the
                // results
                isOnline = NetUtils.isConnected(context);
                if (!isOnline) {
                    // not online, fetch does not make sense
                    return null;
                }
                // we are online, return fetch result

                return fetchRooms(searchString[0]);
            }

            @Override
            protected void onPostExecute(List<Map<String, String>> result) {
                // handle result
                if (!isOnline) {
                    listener.onNoInternetError();
                    return;
                }
                if (result == null) {
                    listener.onFetchError(context
                            .getString(R.string.empty_result));
                    return;
                }
                // If there could not be found any problems return usual on
                // Fetch method
                listener.onFetch(result);
            }

        };

        backgroundTask.execute(searchString);
    }
}
