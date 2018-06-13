package de.tum.in.tumcampusapp;

import android.app.Application;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import de.tum.in.tumcampusapp.component.other.notifications.model.AppNotificationsManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        this.initPicasso();
        AppNotificationsManager.INSTANCE.setupNotificationChannels(this);
    }

    protected void initPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));

        Picasso built = builder.build();
        built.setLoggingEnabled(true);

        if (BuildConfig.DEBUG) {
            built.setIndicatorsEnabled(true);
        }

        Picasso.setSingletonInstance(built);
    }
}
