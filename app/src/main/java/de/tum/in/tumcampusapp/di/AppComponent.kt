package de.tum.`in`.tumcampusapp.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import de.tum.`in`.tumcampusapp.component.other.settings.SettingsFragment
import de.tum.`in`.tumcampusapp.component.tumui.feedback.di.FeedbackComponent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.di.CafeteriaComponent
import de.tum.`in`.tumcampusapp.component.ui.news.di.NewsComponent
import de.tum.`in`.tumcampusapp.component.ui.overview.MainActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainFragment
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.EventsComponent
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.TicketsComponent
import de.tum.`in`.tumcampusapp.component.ui.tufilm.di.KinoComponent
import de.tum.`in`.tumcampusapp.service.di.DownloadComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun cafeteriaComponent(): CafeteriaComponent
    fun downloadComponent(): DownloadComponent
    fun eventsComponent(): EventsComponent.Builder
    fun feedbackComponent(): FeedbackComponent.Builder
    fun kinoComponent(): KinoComponent
    fun newsComponent(): NewsComponent
    fun ticketsComponent(): TicketsComponent.Builder

    fun inject(mainActivity: MainActivity)
    fun inject(mainFragment: MainFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(settingsFragment: SettingsFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent

    }

}
