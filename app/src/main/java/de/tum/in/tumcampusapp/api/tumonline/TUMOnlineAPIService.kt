package de.tum.`in`.tumcampusapp.api.tumonline

import de.tum.`in`.tumcampusapp.api.tumonline.model.AccessToken
import de.tum.`in`.tumcampusapp.api.tumonline.model.TokenConfirmation
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
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TUMOnlineAPIService {

    @GET("wbservicesbasic.kalender")
    fun getCalendar(
        @Query("pMonateVor") start: Int,
        @Query("pMonateNach") end: Int,
        @Header("Cache-Control") cacheControl: String
    ): Call<EventsResponse>

    @GET("wbservicesbasic.terminCreate")
    fun createCalendarEvent(
        @Query("pTitel") title: String,
        @Query("pAnmerkung") description: String,
        @Query("pVon") start: String,
        @Query("pBis") end: String,
        @Query("pTerminNr") eventId: String? = null
    ): Call<CreateEventResponse>

    @GET("wbservicesbasic.terminDelete")
    fun deleteCalendarEvent(
        @Query("pTerminNr") eventId: String
    ): Call<DeleteEventResponse>

    @GET("wbservicesbasic.studienbeitragsstatus")
    fun getTuitionFeesStatus(
        @Header("Cache-Control") cacheControl: String
    ): Call<TuitionList>

    @GET("wbservicesbasic.veranstaltungenEigene")
    fun getPersonalLectures(
        @Header("Cache-Control") cacheControl: String
    ): Call<LecturesResponse>

    @GET("wbservicesbasic.veranstaltungenDetails")
    fun getLectureDetails(
        @Query("pLVNr") id: String,
        @Header("Cache-Control") cacheControl: String
    ): Call<LectureDetailsResponse>

    @GET("wbservicesbasic.veranstaltungenTermine")
    fun getLectureAppointments(
        @Query("pLVNr") id: String,
        @Header("Cache-Control") cacheControl: String
    ): Call<LectureAppointmentsResponse>

    @GET("wbservicesbasic.veranstaltungenSuche")
    fun searchLectures(
        @Query("pSuche") query: String
    ): Call<LecturesResponse>

    @GET("wbservicesbasic.veranstaltungenSuche")
    fun searchLecturesRx(
        @Query("pSuche") query: String
    ): Single<LecturesResponse>

    @GET("wbservicesbasic.personenDetails")
    fun getPersonDetails(
        @Query("pIdentNr") id: String,
        @Header("Cache-Control") cacheControl: String
    ): Call<Employee>

    @GET("wbservicesbasic.personenSuche")
    fun searchPerson(
        @Query("pSuche") query: String
    ): Single<PersonList>

    @GET("wbservicesbasic.noten")
    fun getGrades(
        @Header("Cache-Control") cacheControl: String
    ): Call<ExamList>

    @GET("wbservicesbasic.requestToken")
    fun requestToken(
        @Query("pUsername") username: String,
        @Query("pTokenName") tokenName: String
    ): Call<AccessToken>

    @GET("wbservicesbasic.id")
    fun getIdentity(): Call<IdentitySet>

    @GET("wbservicesbasic.secretUpload")
    fun uploadSecret(
        @Query("pToken") token: String,
        @Query("pSecret") secret: String
    ): Call<TokenConfirmation>

}