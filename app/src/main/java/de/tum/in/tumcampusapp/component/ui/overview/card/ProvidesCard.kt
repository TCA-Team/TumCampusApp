package de.tum.`in`.tumcampusapp.component.ui.overview.card

/**
 * Interface which has to be implemented by a manager class to add cards to the stream
 */
interface ProvidesCard {

    /**
     * Returns the list of [Card]s that should be displayed in the overview screen.
     */
    fun getCards(force: Boolean): List<Card>

}