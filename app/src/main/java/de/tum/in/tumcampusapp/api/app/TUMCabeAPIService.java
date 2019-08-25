package de.tum.in.tumcampusapp.api.app;

import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.model.DeviceRegister;
import de.tum.in.tumcampusapp.api.app.model.DeviceUploadFcmToken;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.component.other.locations.model.BuildingToGps;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.FeedbackResult;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderMap;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotification;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotificationLocation;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierFreeContact;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierFreeMoreInfo;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsAlert;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.component.ui.updatenote.model.UpdateNote;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_BUILDINGS_TO_GPS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_CONTACT;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_LIST_OF_ELEVATORS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_LIST_OF_TOILETS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_MORE_INFO;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_NERBY_FACILITIES;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_CAFETERIAS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_CHAT_MEMBERS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_CHAT_ROOMS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_DEVICE;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_EVENTS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_FEEDBACK;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_KINOS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_LOCATIONS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_MEMBERS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_NEWS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_NOTIFICATIONS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_OPENING_HOURS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_ROOM_FINDER;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_ROOM_FINDER_AVAILABLE_MAPS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_ROOM_FINDER_COORDINATES;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_ROOM_FINDER_SCHEDULE;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_ROOM_FINDER_SEARCH;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_STUDY_ROOMS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_TICKET;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_UPDATE_NOTE;

public interface TUMCabeAPIService {

    //Group chat
    @POST(API_CHAT_ROOMS)
    Call<ChatRoom> createRoom(@Body TUMCabeVerification verification);

    @GET(API_CHAT_ROOMS + "{room}")
    Call<ChatRoom> getChatRoom(@Path("room") int id);

    @POST(API_CHAT_ROOMS + "{room}/leave/")
    Call<ChatRoom> leaveChatRoom(@Path("room") int roomId, @Body TUMCabeVerification verification);

    @POST(API_CHAT_ROOMS + "{room}/add/{member}")
    Call<ChatRoom> addUserToChat(@Path("room") int roomId, @Path("member") int userId, @Body TUMCabeVerification verification);

    //Get/Update single message
    @PUT(API_CHAT_ROOMS + "{room}/message/")
    Observable<ChatMessage> sendMessage(@Path("room") int roomId, @Body TUMCabeVerification message);

    @PUT(API_CHAT_ROOMS + "{room}/message/{message}/")
    Observable<ChatMessage> updateMessage(@Path("room") int roomId, @Path("message") int messageId, @Body TUMCabeVerification message);

    //Get all recent messages or older ones
    @POST(API_CHAT_ROOMS + "{room}/messages/{page}/")
    Observable<List<ChatMessage>> getMessages(@Path("room") int roomId, @Path("page") long messageId, @Body TUMCabeVerification verification);

    @POST(API_CHAT_ROOMS + "{room}/messages/")
    Observable<List<ChatMessage>> getNewMessages(@Path("room") int roomId, @Body TUMCabeVerification verification);

    @POST(API_CHAT_MEMBERS)
    Call<ChatMember> createMember(@Body ChatMember chatMember);

    @GET(API_CHAT_MEMBERS + "{lrz_id}/")
    Call<ChatMember> getMember(@Path("lrz_id") String lrzId);

    @GET(API_CHAT_MEMBERS + "search/{query}/")
    Call<List<ChatMember>> searchMemberByName(@Path("query") String nameQuery);

    @POST(API_CHAT_MEMBERS + "{memberId}/rooms/")
    Call<List<ChatRoom>> getMemberRooms(@Path("memberId") int memberId, @Body TUMCabeVerification verification);

    @POST(API_MEMBERS + "uploadIds/{lrzId}/")
    Observable<TUMCabeStatus> uploadObfuscatedIds(@Path("lrzId") String lrzId, @Body ObfuscatedIdsUpload ids);

    @GET(API_NOTIFICATIONS + "{notification}/")
    Call<FcmNotification> getNotification(@Path("notification") int notification);

    @GET(API_NOTIFICATIONS + "confirm/{notification}/")
    Call<String> confirm(@Path("notification") int notification);

    @GET(API_LOCATIONS + "{locationId}/")
    Call<FcmNotificationLocation> getLocation(@Path("locationId") int locationId);

    //Device
    @POST(API_DEVICE + "register/")
    Call<TUMCabeStatus> deviceRegister(@Body DeviceRegister verification);

    @GET(API_DEVICE + "verifyKey/")
    Call<TUMCabeStatus> verifyKey();

    @POST(API_DEVICE + "addGcmToken/")
    Call<TUMCabeStatus> deviceUploadGcmToken(@Body DeviceUploadFcmToken verification);

    @GET(API_DEVICE + "uploaded/{lrzId}")
    Call<UploadStatus> getUploadStatus(@Path("lrzId") String lrzId);

    // Barrier free contacts
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_CONTACT)
    Call<List<BarrierFreeContact>> getBarrierfreeContactList();

    // Barrier free More Info
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_MORE_INFO)
    Call<List<BarrierFreeMoreInfo>> getMoreInfoList();

    // Barrier free toilets list
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_LIST_OF_TOILETS)
    Call<List<RoomFinderRoom>> getListOfToilets();

    // Barrier free elevator list
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_LIST_OF_ELEVATORS)
    Call<List<RoomFinderRoom>> getListOfElevators();

    // Barrier free nearby list
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_NERBY_FACILITIES + "{buildingId}/")
    Call<List<RoomFinderRoom>> getListOfNearbyFacilities(@Path("buildingId") String buildingId);

    // building to gps information
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_BUILDINGS_TO_GPS)
    Call<List<BuildingToGps>> getBuilding2Gps();

    //RoomFinder maps
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_AVAILABLE_MAPS + "{archId}")
    Call<List<RoomFinderMap>> fetchAvailableMaps(@Path("archId") String archId);

    //RoomFinder maps
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_SEARCH + "{searchStrings}")
    Single<List<RoomFinderRoom>> fetchRooms(@Path("searchStrings") String query);

    //RoomFinder cordinates
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_COORDINATES + "{archId}")
    Call<RoomFinderCoordinate> fetchCoordinates(@Path("archId") String archId);

    //RoomFinder schedule
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_SCHEDULE + "{roomId}" + "/" + "{start}" + "/" + "{end}")
    Call<List<RoomFinderSchedule>> fetchSchedule(@Path("roomId") String archId,
                                                 @Path("start") String start, @Path("end") String end);

    @POST(API_FEEDBACK)
    Call<FeedbackResult> sendFeedback(@Body Feedback feedback);

    @Multipart
    @POST(API_FEEDBACK + "{id}/{image}/")
    Call<FeedbackResult> sendFeedbackImage(@Part MultipartBody.Part image, @Path("image") int imageNr, @Path("id") String feedbackId);

    @GET(API_CAFETERIAS)
    Observable<List<Cafeteria>> getCafeterias();

    @GET(API_KINOS + "{lastId}")
    Flowable<List<Kino>> getKinos(@Path("lastId") String lastId);

    @GET(API_NEWS + "{lastNewsId}")
    Call<List<News>> getNews(@Path("lastNewsId") String lastNewsId);

    @GET(API_NEWS + "sources")
    Call<List<NewsSources>> getNewsSources();

    @GET(API_NEWS + "alert")
    Observable<NewsAlert> getNewsAlert();

    @GET(API_STUDY_ROOMS)
    Call<List<StudyRoomGroup>> getStudyRoomGroups();

    // TICKET SALE

    // Getting Event information

    @GET(API_EVENTS + "list")
    Observable<List<Event>> getEvents();

    // Getting Ticket information
    @POST(API_EVENTS + API_TICKET + "my")
    Observable<List<Ticket>> getTickets(@Body TUMCabeVerification verification);

    @POST(API_EVENTS + API_TICKET + "{ticketID}")
    Call<Ticket> getTicket(@Path("ticketID") int ticketID, @Body TUMCabeVerification verification);

    @GET(API_EVENTS + API_TICKET + "type/{eventID}")
    Observable<List<TicketType>> getTicketTypes(@Path("eventID") int eventID);

    // Ticket reservation
    @POST(API_EVENTS + API_TICKET + "reserve/multiple")
    Call<TicketReservationResponse> reserveTicket(@Body TUMCabeVerification verification);

    // Ticket purchase
    @POST(API_EVENTS + API_TICKET + "payment/stripe/purchase/multiple")
    Call<List<Ticket>> purchaseTicketStripe(@Body TUMCabeVerification verification);

    @POST(API_EVENTS + API_TICKET + "payment/stripe/ephemeralkey")
    Call<HashMap<String, Object>> retrieveEphemeralKey(@Body TUMCabeVerification verification);

    @GET(API_EVENTS + API_TICKET + "status/{event}")
    Single<List<TicketStatus>> getTicketStats(@Path("event") int event);

    // Update note
    @GET(API_UPDATE_NOTE + "{version}")
    Call<UpdateNote> getUpdateNote(@Path("version") int version);

    // Opening Hours
    @GET(API_OPENING_HOURS + "{language}")
    Call<List<Location>> getOpeningHours(@Path("language") String language);

}