package de.tum.`in`.tumcampusapp.api.tumonline

import android.content.Context
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import de.tum.`in`.tumcampusapp.api.app.ApiHelper
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.AddTokenInterceptor
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.CacheResponseInterceptor
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.CheckErrorInterceptor
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.CheckTokenInterceptor
import de.tum.`in`.tumcampusapp.api.tumonline.model.AccessToken
import de.tum.`in`.tumcampusapp.api.tumonline.model.TokenConfirmation
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CreateEventResponse
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.DeleteEventResponse
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsResponse
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureDetailsResponse
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Employee
import de.tum.`in`.tumcampusapp.component.tumui.person.model.IdentitySet
import de.tum.`in`.tumcampusapp.component.tumui.person.model.PersonList
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.TuitionList
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

class TUMOnlineClient(private val apiService: TUMOnlineAPIService) {

    fun getCalendar(cacheControl: CacheControl): Call<EventsResponse> {
        return apiService.getCalendar(
            Const.CALENDAR_MONTHS_BEFORE, Const.CALENDAR_MONTHS_AFTER, cacheControl.header)
    }

    fun createEvent(calendarItem: CalendarItem, eventId: String?): Call<CreateEventResponse> {
        val start = DateTimeUtils.getDateTimeString(calendarItem.eventStart)
        val end = DateTimeUtils.getDateTimeString(calendarItem.eventEnd)
        return apiService.createCalendarEvent(
            calendarItem.title, calendarItem.description, start, end, eventId)
    }

    fun deleteEvent(eventId: String): Call<DeleteEventResponse> {
        return apiService.deleteCalendarEvent(eventId)
    }

    fun getTuitionFeesStatus(cacheControl: CacheControl): Call<TuitionList> {
        return apiService.getTuitionFeesStatus(cacheControl.header)
    }

    fun getPersonalLectures(cacheControl: CacheControl): Call<LecturesResponse> {
        return apiService.getPersonalLectures(cacheControl.header)
    }

    fun getLectureDetails(id: String, cacheControl: CacheControl): Call<LectureDetailsResponse> {
        return apiService.getLectureDetails(id, cacheControl.header)
    }

    fun getLectureAppointments(id: String, cacheControl: CacheControl): Call<LectureAppointmentsResponse> {
        return apiService.getLectureAppointments(id, cacheControl.header)
    }

    fun searchLectures(query: String): Call<LecturesResponse> {
        return apiService.searchLectures(query)
    }

    fun searchLecturesRx(query: String): Single<LecturesResponse> {
        return apiService.searchLecturesRx(query)
    }

    fun getPersonDetails(id: String, cacheControl: CacheControl): Call<Employee> {
        return apiService.getPersonDetails(id, cacheControl.header)
    }

    fun searchPerson(query: String): Single<PersonList> {
        return apiService.searchPerson(query)
    }

    fun getGrades(cacheControl: CacheControl): Call<ExamList> {
        return apiService.getGrades(cacheControl.header)
    }

    fun requestToken(username: String, tokenName: String): Call<AccessToken> {
        return apiService.requestToken(username, tokenName)
    }

    fun getIdentity(): Call<IdentitySet> = apiService.getIdentity()

    fun uploadSecret(token: String, secret: String): Call<TokenConfirmation> {
        return apiService.uploadSecret(token, secret)
    }

    companion object {

        private const val BASE_URL = "https://campus.tum.de/tumonline/"
        // For testing
        // private const val BASE_URL = "https://campusquality.tum.de/QSYSTEM_TUM/"

        private var client: TUMOnlineClient? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): TUMOnlineClient {
            if (client == null) {
                client = buildAPIClient(context)
            }

            return client!!
        }

        private fun buildAPIClient(context: Context): TUMOnlineClient {
            val cacheManager = CacheManager(context)

            val client = ApiHelper.getOkHttpClient(context)
                .newBuilder()
                .cache(cacheManager.cache)
                .addInterceptor(AddTokenInterceptor(context))
                .addInterceptor(CheckTokenInterceptor(context))
                .addNetworkInterceptor(CacheResponseInterceptor())
                .addNetworkInterceptor(CheckErrorInterceptor(context))
                .build()

            val tikXml = TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()
            val xmlConverterFactory = TikXmlConverterFactory.create(tikXml)

            val apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(xmlConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(TUMOnlineAPIService::class.java)
            return TUMOnlineClient(apiService)
        }

    }

}