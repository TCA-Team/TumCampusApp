package de.tum.in.tumcampusapp.auxiliary.calendar;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

/**
 * Helper class for exporting to Google Calendar.
 */
public class CalendarHelper {
	private static final String ACCOUNT_NAME = "TUM_Campus_APP";
	private static final String Calendar_Name = "TUM Campus";

    /**
     * Gets uri query to insert calendar TUM_Campus_APP to google calendar
     * @param c Context
     * @return Uri for insertion
     */
	public static Uri addCalendar(Context c) {
		final ContentValues cv = buildContentValues();
		Uri calUri = buildCalUri();
        return c.getContentResolver().insert(calUri, cv);
	}

    /**
     * Deletes the calendar TUM_Campus_APP from google calendar
     * @param c Context
     * @return Number of rows deleted
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static int deleteCalendar(Context c) {
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        return c.getContentResolver().delete(uri, " account_name = '" + ACCOUNT_NAME + "'", null);
    }

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static Uri buildCalUri() {
		return CalendarContract.Calendars.CONTENT_URI.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();
	}

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static ContentValues buildContentValues() {
		int colorCalendar = 0x0066CC;
		String intName = ACCOUNT_NAME + Calendar_Name;
		final ContentValues cv = new ContentValues();
		cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
		cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
		cv.put(Calendars.NAME, intName);
		cv.put(Calendars.CALENDAR_DISPLAY_NAME, Calendar_Name);
		cv.put(Calendars.CALENDAR_COLOR, colorCalendar);
		cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
		cv.put(Calendars.VISIBLE, 1);
		cv.put(Calendars.SYNC_EVENTS, 1);
		return cv;
	}

}
