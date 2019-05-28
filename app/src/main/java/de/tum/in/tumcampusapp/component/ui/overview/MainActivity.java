package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkManager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.di.ViewModelFactory;
import de.tum.in.tumcampusapp.service.DownloadWorker;
import de.tum.in.tumcampusapp.service.SilenceService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Main activity displaying the cards and providing navigation with navigation drawer
 */
public class MainActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, CardInteractionListener {

    private boolean mIsConnectivityChangeReceiverRegistered;

    private RecyclerView mCardsView;
    private CardAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final int FLEXIBLE_UPDATE_REQUEST_CODE = 1;
    private final int IMMEDIATE_UPDATE_REQUEST_CODE = 2;

    @Inject
    Provider<MainActivityViewModel> viewModelProvider;

    private MainActivityViewModel viewModel;

    ConnectivityManager connectivityManager;
    final NetworkCallback networkCallback = new NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            runOnUiThread(MainActivity.this::refreshCards);
        }
    };

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().inject(this);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Setup pull to refresh
        mSwipeRefreshLayout = findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200);

        // Setup card RecyclerView
        mCardsView = findViewById(R.id.cards_view);
        registerForContextMenu(mCardsView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mCardsView.setLayoutManager(layoutManager);
        mCardsView.setHasFixedSize(true);

        mAdapter = new CardAdapter();
        mCardsView.setAdapter(mAdapter);

        showToolbar();

        // Add equal spacing between CardViews in the RecyclerView
        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        mCardsView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        // Swipe gestures
        new ItemTouchHelper(new MainActivityTouchHelperCallback()).attachToRecyclerView(mCardsView);

        // Start silence Service (if already started it will just invoke a check)
        Intent service = new Intent(this, SilenceService.class);
        this.startService(service);

        ViewModelFactory<MainActivityViewModel> factory = new ViewModelFactory<>(viewModelProvider);
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);

        viewModel.getCards().observe(this, cards -> {
            if (cards != null) {
                onNewCardsAvailable(cards);
            }
        });
    }

    private void checkForUpdates() {
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                try {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo, AppUpdateType.IMMEDIATE, this, IMMEDIATE_UPDATE_REQUEST_CODE);
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener() {
                            @Override
                            public void onStateUpdate(InstallState installState) {
                                if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                                    showInstallUpdateSnackbar(appUpdateManager);
                                    appUpdateManager.unregisterListener(this);
                                }
                            }
                        };
                        appUpdateManager.registerListener(installStateUpdatedListener);
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE_UPDATE_REQUEST_CODE);
                    }
                } catch (IntentSender.SendIntentException e) {
                    Utils.log(e);
                }
            }
        });
    }

    private void showInstallUpdateSnackbar(AppUpdateManager appUpdateManager) {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        getApplicationContext().getString(R.string.update_downloaded),
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.update_restart), view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.color_primary));
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMMEDIATE_UPDATE_REQUEST_CODE || requestCode == FLEXIBLE_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Utils.log("Update flow failed! Result code: " + resultCode);
                // retry immediate updates
                if (requestCode == IMMEDIATE_UPDATE_REQUEST_CODE) {
                    checkForUpdates();
                }
            }
        }
    }

    private void onNewCardsAvailable(List<Card> cards) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.updateItems(cards);

        if (!NetUtils.isConnected(this) && !mIsConnectivityChangeReceiverRegistered) {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetUtils.getInternetCapability())
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
            mIsConnectivityChangeReceiverRegistered = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void downloadNewsAlert() {
        WorkManager.getInstance().enqueue(DownloadWorker.getWorkRequest());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsConnectivityChangeReceiverRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            mIsConnectivityChangeReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utils.getSettingBool(this, Const.REFRESH_CARDS, false)) {
            refreshCards();
            Utils.setSetting(this, Const.REFRESH_CARDS, false);
        }

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed, notify the user to complete the update.
                    // For flexible updates
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        showInstallUpdateSnackbar(appUpdateManager);
                    }
                    // For immediate updates
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    this,
                                    IMMEDIATE_UPDATE_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            Utils.log(e);
                        }
                    }
                });

    }

    /**
     * Show progress indicator and start updating cards in background
     */
    public void refreshCards() {
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();
        downloadNewsAlert();
    }

    /**
     * Starts updating cards in background
     * Called when {@link SwipeRefreshLayout} gets triggered.
     */
    @Override
    public void onRefresh() {
        viewModel.refreshCards();
    }

    /**
     * Executed when the RestoreCard is pressed
     */
    public void restoreCards(View view) {
        CardManager.restoreCards(this);
        refreshCards();
    }

    /**
     * Smoothly scrolls the RecyclerView to the top and dispatches nestedScrollingEvents to show
     * the Toolbar
     */
    private void showToolbar() {
        mCardsView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        mCardsView.dispatchNestedFling(0, Integer.MIN_VALUE, true);
        mCardsView.stopNestedScroll();

        RecyclerView.LayoutManager layoutManager = mCardsView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.smoothScrollToPosition(mCardsView, null, 0);
        }
    }

    @Override
    public void onAlwaysHideCard(int position) {
        mAdapter.remove(position);
    }

    /**
     * A touch helper class, Handles swipe to dismiss events
     */
    private class MainActivityTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        MainActivityTouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
            CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
            Card card = cardViewHolder.getCurrentCard();
            if (card == null || !card.isDismissible()) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
            final Card card = cardViewHolder.getCurrentCard();
            final int lastPos = cardViewHolder.getAdapterPosition();
            mAdapter.remove(lastPos);

            final View coordinatorLayoutView = findViewById(R.id.coordinator);
            final int color = ContextCompat.getColor(getBaseContext(), android.R.color.white);

            Snackbar.make(coordinatorLayoutView, R.string.card_dismissed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> {
                        mAdapter.insert(lastPos, card);

                        RecyclerView.LayoutManager layoutManager = mCardsView.getLayoutManager();
                        if (layoutManager != null) {
                            layoutManager.smoothScrollToPosition(mCardsView, null, lastPos);
                        }
                    })
                    .setActionTextColor(color)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                // DISMISS_EVENT_ACTION means, the snackbar was dismissed via the undo button
                                // and therefore, we didn't really dismiss the card

                                if (card != null) {
                                    card.discardCard();
                                }
                            }
                        }
                    })
                    .show();
        }
    }
}
