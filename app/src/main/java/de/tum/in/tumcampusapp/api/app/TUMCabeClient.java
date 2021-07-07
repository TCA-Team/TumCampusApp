package de.tum.in.tumcampusapp.api.app;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.model.DeviceRegister;
import de.tum.in.tumcampusapp.api.app.model.DeviceUploadFcmToken;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.component.other.locations.model.BuildingToGps;
import de.tum.in.tumcampusapp.component.ui.bibreservation.model.BibAppointment;
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
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsAlert;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.EphimeralKey;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketPurchaseStripe;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.component.ui.updatenote.model.UpdateNote;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

/**
 * Proxy class for Retrofit client to our API hosted @app.tum.de
 */
public final class TUMCabeClient {

    static final String API_MEMBERS = "members/";
    static final String API_NOTIFICATIONS = "notifications/";
    static final String API_LOCATIONS = "locations/";
    static final String API_DEVICE = "device/";
    static final String API_WIFI_HEATMAP = "wifimap/";
    static final String API_BARRIER_FREE = "barrierfree/";
    static final String API_BARRIER_FREE_CONTACT = "contacts/";
    static final String API_BARRIER_FREE_BUILDINGS_TO_GPS = "getBuilding2Gps/";
    static final String API_BARRIER_FREE_NERBY_FACILITIES = "nerby/";
    static final String API_BARRIER_FREE_LIST_OF_TOILETS = "listOfToilets/";
    static final String API_BARRIER_FREE_LIST_OF_ELEVATORS = "listOfElevators/";
    static final String API_BARRIER_FREE_MORE_INFO = "moreInformation/";
    static final String API_ROOM_FINDER = "roomfinder/room/";
    static final String API_ROOM_FINDER_SEARCH = "search/";
    static final String API_ROOM_FINDER_COORDINATES = "coordinates/";
    static final String API_ROOM_FINDER_AVAILABLE_MAPS = "availableMaps/";
    static final String API_ROOM_FINDER_SCHEDULE = "scheduleById/";
    static final String API_FEEDBACK = "feedback/";
    static final String API_CAFETERIAS = "mensen/";
    static final String API_KINOS = "kino/";
    static final String API_NEWS = "news/";
    static final String API_UPDATE_NOTE = "updatenote/";
    static final String API_EVENTS = "event/";
    static final String API_TICKET = "ticket/";
    static final String API_STUDY_ROOMS = "studyroom/list";
    private static final String API_HOSTNAME = Const.API_HOSTNAME;
    private static final String API_BASEURL = "/Api/";
    private static final String API_CHAT = "chat/";
    static final String API_CHAT_ROOMS = API_CHAT + "rooms/";
    static final String API_CHAT_MEMBERS = API_CHAT + "members/";
    static final String API_OPENING_HOURS = "openingtimes/";
    static final String API_BIB_RESERVATIONS = "bibreservation/";

    private static TUMCabeClient instance;
    private final TUMCabeAPIService service;

    private TUMCabeClient(final Context c) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateSerializer())
                .create();

        service = new Retrofit.Builder()
                .baseUrl("https://" + API_HOSTNAME + API_BASEURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ApiHelper.getOkHttpClient(c))
                .build()
                .create(TUMCabeAPIService.class);

    }

    public static synchronized TUMCabeClient getInstance(Context c) {
        if (instance == null) {
            instance = new TUMCabeClient(c.getApplicationContext());
        }
        return instance;
    }

    private static TUMCabeVerification getVerification(Context context, @Nullable Object data) throws NoPrivateKey {
        TUMCabeVerification verification =
                TUMCabeVerification.create(context, data);
        if (verification == null) {
            throw new NoPrivateKey();
        }

        return verification;
    }

    public void createRoom(ChatRoom chatRoom, TUMCabeVerification verification, Callback<ChatRoom> cb) {
        verification.setData(chatRoom);
        service.createRoom(verification)
                .enqueue(cb);
    }

    public ChatRoom createRoom(ChatRoom chatRoom, TUMCabeVerification verification) throws IOException {
        verification.setData(chatRoom);
        return service.createRoom(verification)
                .execute()
                .body();
    }

    public ChatRoom getChatRoom(int id) throws IOException {
        return service.getChatRoom(id)
                .execute()
                .body();
    }

    public ChatMember createMember(ChatMember chatMember) throws IOException {
        return service.createMember(chatMember)
                .execute()
                .body();
    }

    public void leaveChatRoom(ChatRoom chatRoom, TUMCabeVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId(), verification)
                .enqueue(cb);
    }

    public void addUserToChat(ChatRoom chatRoom, ChatMember member, TUMCabeVerification verification, Callback<ChatRoom> cb) {
        service.addUserToChat(chatRoom.getId(), member.getId(), verification)
                .enqueue(cb);
    }

    public Observable<ChatMessage> sendMessage(int roomId, TUMCabeVerification verification) {
        ChatMessage message = (ChatMessage) verification.getData();
        if (message == null) {
            throw new IllegalStateException("TUMCabeVerification data is not a ChatMessage");
        }

        if (message.isNewMessage()) {
            return service.sendMessage(roomId, verification);
        }

        return service.updateMessage(roomId, message.getId(), verification);
    }

    public Observable<List<ChatMessage>> getMessages(int roomId, long messageId, @Body TUMCabeVerification verification) {
        return service.getMessages(roomId, messageId, verification);
    }

    public Observable<List<ChatMessage>> getNewMessages(int roomId, @Body TUMCabeVerification verification) {
        return service.getNewMessages(roomId, verification);
    }

    public List<ChatRoom> getMemberRooms(int memberId, TUMCabeVerification verification) throws IOException {
        return service.getMemberRooms(memberId, verification)
                .execute()
                .body();
    }

    Observable<TUMCabeStatus> uploadObfuscatedIds(String lrzId, ObfuscatedIdsUpload ids) {
        return service.uploadObfuscatedIds(lrzId, ids);
    }

    public FcmNotification getNotification(int notification) throws IOException {
        return service.getNotification(notification)
                .execute()
                .body();
    }

    public void confirm(int notification) throws IOException {
        service.confirm(notification)
                .execute();
    }

    public FcmNotificationLocation getLocation(int locationId) throws IOException {
        return service.getLocation(locationId)
                .execute()
                .body();
    }

    void deviceRegister(DeviceRegister verification, Callback<TUMCabeStatus> cb) {
        service.deviceRegister(verification)
                .enqueue(cb);
    }

    @Nullable
    public TUMCabeStatus verifyKey() {
        try {
            return service.verifyKey().execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public void deviceUploadGcmToken(DeviceUploadFcmToken verification, Callback<TUMCabeStatus> cb) {
        service.deviceUploadGcmToken(verification)
                .enqueue(cb);
    }

    @Nullable
    public UploadStatus getUploadStatus(String lrzId) {
        try {
            return service.getUploadStatus(lrzId).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public List<BarrierFreeContact> getBarrierfreeContactList() throws IOException {
        return service.getBarrierfreeContactList()
                .execute()
                .body();
    }

    public List<BarrierFreeMoreInfo> getMoreInfoList() throws IOException {
        return service.getMoreInfoList()
                .execute()
                .body();
    }

    public Call<List<RoomFinderRoom>> getListOfToilets() {
        return service.getListOfToilets();
    }

    public Call<List<RoomFinderRoom>> getListOfElevators() {
        return service.getListOfElevators();
    }

    public Call<List<RoomFinderRoom>> getListOfNearbyFacilities(String buildingId) {
        return service.getListOfNearbyFacilities(buildingId);
    }

    public List<BuildingToGps> getBuilding2Gps() throws IOException {
        return service.getBuilding2Gps()
                .execute()
                .body();
    }

    public Call<List<RoomFinderMap>> fetchAvailableMaps(final String archId) {
        return service.fetchAvailableMaps(ApiHelper.encodeUrl(archId));
    }

    public List<RoomFinderRoom> fetchRooms(String searchStrings) throws IOException {
        return service.fetchRooms(ApiHelper.encodeUrl(searchStrings))
                .execute()
                .body();
    }

    public RoomFinderCoordinate fetchCoordinates(String archId) throws IOException {
        return fetchRoomFinderCoordinates(archId).execute().body();
    }

    public Call<RoomFinderCoordinate> fetchRoomFinderCoordinates(String archId) {
        return service.fetchCoordinates(ApiHelper.encodeUrl(archId));
    }

    @Nullable
    public List<RoomFinderSchedule> fetchSchedule(String roomId, String start, String end) throws IOException {
        return service.fetchSchedule(ApiHelper.encodeUrl(roomId),
                ApiHelper.encodeUrl(start), ApiHelper.encodeUrl(end))
                .execute()
                .body();
    }

    public Call<FeedbackResult> sendFeedback(Feedback feedback) {
        return service.sendFeedback(feedback);
    }

    public List<Call<FeedbackResult>> sendFeedbackImages(Feedback feedback, String[] imagePaths) {
        List<Call<FeedbackResult>> calls = new ArrayList<>();
        for (int i = 0; i < imagePaths.length; i++) {
            File file = new File(imagePaths[i]);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("feedback_image", i + ".png", reqFile);

            Call<FeedbackResult> call = service.sendFeedbackImage(body, i + 1, feedback.getId());
            calls.add(call);
        }
        return calls;
    }

    public void searchChatMember(String query, Callback<List<ChatMember>> callback) {
        service.searchMemberByName(query)
                .enqueue(callback);
    }

    public void getChatMemberByLrzId(String lrzId, Callback<ChatMember> callback) {
        service.getMember(lrzId)
                .enqueue(callback);
    }

    public Observable<List<Cafeteria>> getCafeterias() {
        return service.getCafeterias();
    }

    public Flowable<List<Kino>> getKinos(String lastId) {
        return service.getKinos(lastId);
    }

    public List<News> getNews(String lastNewsId) throws IOException {
        return service.getNews(lastNewsId)
                .execute()
                .body();
    }

    public List<NewsSources> getNewsSources() throws IOException {
        return service.getNewsSources()
                .execute()
                .body();
    }

    public Observable<NewsAlert> getNewsAlert() {
        return service.getNewsAlert();
    }

    public Call<List<StudyRoomGroup>> getStudyRoomGroups() {
        return service.getStudyRoomGroups();
    }

    // TICKET SALE

    // Getting event information
    public Observable<List<Event>> fetchEvents() {
        return service.getEvents();
    }

    // Getting ticket information

    public Observable<List<Ticket>> fetchTickets(Context context) throws NoPrivateKey {
        TUMCabeVerification verification = getVerification(context, null);
        return service.getTickets(verification);
    }

    public Call<Ticket> fetchTicket(Context context, int ticketID) throws NoPrivateKey {
        TUMCabeVerification verification = getVerification(context, null);
        return service.getTicket(ticketID, verification);
    }

    public Observable<List<TicketType>> fetchTicketTypes(int eventID) {
        return service.getTicketTypes(eventID);
    }

    // Ticket reservation

    public void reserveTicket(TUMCabeVerification verification,
                              Callback<TicketReservationResponse> cb) {
        service.reserveTicket(verification).enqueue(cb);
    }

    // Ticket purchase

    public void purchaseTicketStripe(
            Context context, List<Integer> ticketIds, @NonNull String token,
            @NonNull String customerName, Callback<List<Ticket>> cb) throws NoPrivateKey {
        TicketPurchaseStripe purchase = new TicketPurchaseStripe(ticketIds, token, customerName);
        TUMCabeVerification verification = getVerification(context, purchase);
        service.purchaseTicketStripe(verification).enqueue(cb);
    }

    public void retrieveEphemeralKey(Context context, String apiVersion,
                                     Callback<HashMap<String, Object>> cb) throws NoPrivateKey {
        EphimeralKey key = new EphimeralKey(apiVersion);
        TUMCabeVerification verification = getVerification(context, key);
        service.retrieveEphemeralKey(verification).enqueue(cb);
    }

    public Single<List<TicketStatus>> fetchTicketStats(int event) {
        return service.getTicketStats(event);
    }

    public UpdateNote getUpdateNote(int version) throws IOException {
        return service.getUpdateNote(version).execute().body();
    }

    public List<Location> fetchOpeningHours(String language) throws IOException {
        return service.getOpeningHours(language)
                      .execute()
                      .body();
    }

    public void getReservableBibAppointments(Callback<List<BibAppointment>> callback) {
        service.getReservableBibAppointments().enqueue(callback);
    }

}
