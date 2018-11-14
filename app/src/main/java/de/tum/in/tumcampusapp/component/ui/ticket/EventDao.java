package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import io.reactivex.Flowable;

@Dao
public interface EventDao {

    @Query("SELECT * FROM events ORDER BY start_time")
    LiveData<List<Event>> getAll();

    @Query("SELECT * FROM events WHERE start_time > date('now') ORDER BY start_time LIMIT 1")
    Event getNextEvent();

    @Query("SELECT * FROM events where id = :id")
    Event getEventById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Event> events);

    @Query("UPDATE events SET dismissed = 1 WHERE id = :eventId")
    void setDismissed(int eventId);

    @Query("DELETE FROM events WHERE start_time < date('now')")
    void removePastEvents();

    @Query("DELETE FROM events")
    void removeAll();

    // TODO(bronger) use events.tu_film instead
    @Query("SELECT events.* FROM events, kino " +
            "WHERE kino.id =:movieId " +
            "AND kino.link = events.link " +
            "LIMIT 1")
    Flowable<Event> getEventByMovie(String movieId);

}
