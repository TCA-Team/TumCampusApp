package de.tum.`in`.tumcampusapp.component.ui.overview

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomController
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamCard
import de.tum.`in`.tumcampusapp.component.ui.eduroam.EduroamFixCard
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.news.TopNewsCard
import de.tum.`in`.tumcampusapp.component.ui.onboarding.LoginPromptCard
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import org.jetbrains.anko.doAsync

class CardsRepository(private val context: Context) {

    private var cards = MutableLiveData<List<Card>>()

    /**
     * Starts refresh of [Card]s and returns the corresponding [LiveData]
     * through which the result can be received.
     *
     * @return The [LiveData] of [Card]s
     */
    fun getCards(): LiveData<List<Card>> {
        refreshCards(false)
        return cards
    }

    /**
     * Refreshes the [LiveData] of [Card]s and updates its value.
     */
    fun refreshCards(force: Boolean) {
        doAsync {
            val results = getCardsNow(force)
            cards.postValue(results)
        }
    }

    /**
     * Returns the list of [Card]s synchronously.
     *
     * @return The list of [Card]s
     */
    fun getCardsNow(force: Boolean): List<Card> {
        val results = ArrayList<Card?>().apply {
            add(NoInternetCard(context).getIfShowOnStart())
            add(TopNewsCard(context).getIfShowOnStart())
            add(LoginPromptCard(context).getIfShowOnStart())
            add(SupportCard(context).getIfShowOnStart())
            add(EduroamCard(context).getIfShowOnStart())
            add(EduroamFixCard(context).getIfShowOnStart())
        }

        val providers = ArrayList<ProvidesCard>().apply {
            if (AccessTokenManager.hasValidAccessToken(context)) {
                add(CalendarController(context))
                add(TuitionFeeManager(context))
                add(ChatRoomController(context))
            }

            add(CafeteriaManager(context))
            add(TransportController(context))
            add(NewsController(context))
        }

        providers.forEach { provider ->
            val cards = provider.getCards(force)
            results.addAll(cards)
        }

        results.add(RestoreCard(context).getIfShowOnStart())

        return results.filterNotNull()
    }

}