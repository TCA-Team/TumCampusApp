package de.tum.in.tumcampusapp.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.managers.CardManager;

@SuppressLint("Registered")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CardsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final int appID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new CardsRemoteViewsFactory(this.getApplicationContext(), appID);
    }
}

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class CardsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    final private Context mContext;
    final private int appWidgetId;
    final private SharedPreferences prefs;
    final private List<RemoteViews> views = new ArrayList<>();

    CardsRemoteViewsFactory(Context context, int appWidgetId) {
        this.mContext = context;
        this.appWidgetId = appWidgetId;
        prefs = context.getSharedPreferences(CardsWidgetConfigureActivity.PREFS_NAME, 0);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        updateContent();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        return views.get(i);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void updateContent() {
        final String prefix = CardsWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId;
        views.clear();
        CardManager.update(mContext);
        List<Card> cards = CardManager.getCards();
        for (Card card : cards) {
            final boolean getsShown = prefs.getBoolean(prefix + card.getType(), false);
            if (getsShown) {
                final RemoteViews remote = card.getRemoteViews(mContext);

                //So, here is what we do now:
                //Since it is not guaranteed, that anything is running when the user clicks on
                //any card, we need to make sure, we can start the targeted intent only with the
                //data filled in via the FillInIntent.
                //To do this, we try our best to fill in the targeted intent into the FillInIntent
                final Intent target = card.getIntent();

                if (remote != null && target != null) {
                    final Intent fillInIntent = new Intent();
                    if (target.getExtras() != null) {
                        fillInIntent.putExtras(target.getExtras());
                    }
                    fillInIntent.putExtra(CardsWidget.TARGET_INTENT, target.toUri(Intent.URI_INTENT_SCHEME));
                    remote.setOnClickFillInIntent(R.id.cards_widget_card, fillInIntent);
                }
                views.add(remote);
            }
        }
    }
}
