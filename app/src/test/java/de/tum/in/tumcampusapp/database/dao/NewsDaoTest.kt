package de.tum.`in`.tumcampusapp.database.dao

import net.danlew.android.joda.JodaTimeAndroid

import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

import java.util.Date

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.ui.news.NewsDao
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.database.TcaDb

import org.assertj.core.api.Assertions.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class NewsDaoTest {
    private var dao: NewsDao? = null
    private var newsIdx: Int = 0

    @Before
    fun setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).newsDao()
        newsIdx = 0
        JodaTimeAndroid.init(RuntimeEnvironment.application)
    }

    @After
    fun tearDown() {
        dao!!.flush()
        TcaDb.getInstance(RuntimeEnvironment.application).close()
    }

    private fun createNewsItem(source: String, date: Date): News {
        val news = News(Integer.toString(newsIdx),
                Integer.toString(newsIdx),
                "dummy link",
                source,
                "dummy image",
                date,
                date,
                0)
        newsIdx++
        return news
    }

    /**
     * Test clean up for 3 month old news items (which are all)
     * Expected output: all items are cleared - empty database
     */
    @Test
    fun cleanUpOldTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusMonths(3).minusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusMonths(10).toDate()))
        dao!!.insert(createNewsItem("123", now.minusYears(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusYears(3).toDate()))

        // before testing, make sure all items are there
        assertThat(dao!!.getAll(arrayOf(123), 123)).hasSize(4)
        dao!!.cleanUp()
        assertThat(dao!!.getAll(arrayOf(123), 123)).hasSize(0)
    }

    /**
     * Test clean up for items that are still new
     * Expected output: all items remain
     */
    @Test
    fun cleanUpNothingTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusMonths(2).minusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.toDate()))
        dao!!.insert(createNewsItem("123", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.plusYears(1).toDate()))

        // before testing, make sure all items are there
        assertThat(dao!!.getAll(arrayOf(123), 123)).hasSize(4)
        dao!!.cleanUp()
        assertThat(dao!!.getAll(arrayOf(123), 123)).hasSize(4)
    }

    /**
     * Test clean up for various date items
     * Expected output: some items are cleared, some remain
     */
    @Test
    fun cleanUpMixedTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusMonths(5).toDate()))
        dao!!.insert(createNewsItem("123", now.minusDays(100).toDate()))
        dao!!.insert(createNewsItem("123", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.toDate()))

        // before testing, make sure all items are there
        assertThat(dao!!.getAll(arrayOf(123), 123)).hasSize(4)
        dao!!.cleanUp()
        assertThat(dao!!.getAll(arrayOf(123), 123)).hasSize(2)
    }

    /**
     * Several items with different sources - get for single source
     * Expected output: several items are retrieved
     */
    @Test
    fun getAllSingleSourceTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusMonths(5).toDate()))
        dao!!.insert(createNewsItem("124", now.minusDays(100).toDate()))
        dao!!.insert(createNewsItem("125", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.toDate()))

        assertThat(dao!!.getAll(arrayOf(123), 999)).hasSize(2)
    }

    /**
     * Several items with different sources, including selected newspread
     * Expected output: several items are retrieved
     */
    @Test
    fun getAllSelectedSourceTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("999", now.minusMonths(5).toDate()))
        dao!!.insert(createNewsItem("999", now.minusDays(100).toDate()))
        dao!!.insert(createNewsItem("125", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("999", now.toDate()))

        assertThat(dao!!.getAll(arrayOf(123, 999), 999)).hasSize(3)
    }

    /**
     * Several items with multiple sources - get for single source
     * Expected output: several items are retrieved from different sources
     */
    @Test
    fun getAllMultiSourceTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusMonths(5).toDate()))
        dao!!.insert(createNewsItem("124", now.minusDays(100).toDate()))
        dao!!.insert(createNewsItem("125", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.toDate()))

        assertThat(dao!!.getAll(arrayOf(123, 124), 999)).hasSize(3)
    }

    /**
     * News items with dates in future
     * Expected output: All items are retrieved
     */
    @Test
    fun getNewerAllTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("123", now.plusHours(100).toDate()))

        assertThat(dao!!.getNewer(123)).hasSize(4)
    }

    /**
     * Some of news items have dates in future
     * Expected output: Some items are retrieved
     */
    @Test
    fun getNewerSomeTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusHours(1).toDate()))

        assertThat(dao!!.getNewer(123)).hasSize(2)
    }

    /**
     * All dates are in the past for news items
     * Expected output: No items retrieved
     */
    @Test
    fun getNewerNoneTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusYears(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusHours(1).toDate()))

        assertThat(dao!!.getNewer(123)).hasSize(0)
    }

    /**
     * Several news items and "biggest" id one is retrieved
     * Expected output: item with biggest id is retrieved
     */
    @Test
    fun getLastTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusYears(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusHours(1).toDate()))

        val last = dao!!.last
        assertThat(last!!.id).isEqualTo("3")
    }

    /**
     * News items with different sources and all match
     * Expected output: All items are retrieved
     */
    @Test
    fun getBySourcesAllTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("124", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("126", now.plusHours(1).toDate()))

        assertThat(dao!!.getBySources(arrayOf(123, 124, 125, 126))).hasSize(4)
    }

    /**
     * News items with different sources and some match
     * Expected output: Some items are retrieved
     */
    @Test
    fun getBySourcesSomeTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("124", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("126", now.plusHours(1).toDate()))

        assertThat(dao!!.getBySources(arrayOf(123, 124))).hasSize(2)
    }

    /**
     * News items with different sources and some match
     * Expected output: No items retrieved
     */
    @Test
    fun getBySourcesNoneTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("124", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("126", now.plusHours(1).toDate()))

        assertThat(dao!!.getBySources(arrayOf(127, 128))).hasSize(0)
    }

    /**
     * Closest to today items retrieved single per source.
     * Expected output: All items are retrieved
     */
    @Test
    fun getBySourcesLatestTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusDays(1).toDate()))
        dao!!.insert(createNewsItem("124", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.minusYears(1).toDate()))
        dao!!.insert(createNewsItem("126", now.minusHours(1).toDate()))

        val news = dao!!.getBySources(arrayOf(123, 124, 125, 126))
        assertThat(news).hasSize(4)
    }

    /**
     * There are several items per source
     * Expected output: Some items are retrieved
     */
    @Test
    fun getBySourcesLatestSomeTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.minusDays(1).toDate()))
        dao!!.insert(createNewsItem("123", now.minusMonths(1).toDate()))
        dao!!.insert(createNewsItem("124", now.minusYears(1).toDate()))
        dao!!.insert(createNewsItem("124", now.minusHours(100).toDate()))

        val news = dao!!.getBySourcesLatest(arrayOf(123, 124, 125, 126))
        assertThat(news).hasSize(2)
        assertThat(news[0].id).isEqualTo("3")
        assertThat(news[1].id).isEqualTo("0")
    }

    /**
     * All items are in future
     * Expected output: No items retrieved
     */
    @Test
    fun getBySourcesLatestNoneTest() {
        val now = DateTime.now()
        dao!!.insert(createNewsItem("123", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("124", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("126", now.plusHours(30).toDate()))

        // before testing, make sure all items are there
        assertThat(dao!!.getBySourcesLatest(arrayOf(123, 124, 125, 126, 127))).hasSize(0)
    }

    /**
     * Special treatment for Kino item which should be in future
     * Expected output: Single item retrieved
     */
    @Test
    fun getBySourcesLatestKinoTest() {
        val now = DateTime.now()
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao!!.insert(createNewsItem("2", now.plusDays(1).toDate()))
        dao!!.insert(createNewsItem("2", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.plusYears(1).toDate()))
        dao!!.insert(createNewsItem("126", now.plusHours(1).toDate()))

        // before testing, make sure all items are there
        assertThat(dao!!.getBySourcesLatest(arrayOf(127, 2))).hasSize(1)
    }

    /**
     * Mixed sample - multiple Kino items, some items in future, some in past
     * Expected output: severla items retrieved
     */
    @Test
    fun getBySourcesLatestMixedTest() {
        val now = DateTime.now()
        // NOTE: Kino source number is hardcoded 2 (through server's backend)
        dao!!.insert(createNewsItem("2", now.plusDays(1).toDate())) //has to be picked
        dao!!.insert(createNewsItem("2", now.plusMonths(1).toDate()))
        dao!!.insert(createNewsItem("125", now.minusMonths(1).toDate())) //has to be picked
        dao!!.insert(createNewsItem("126", now.plusHours(27).toDate()))

        // before testing, make sure all items are there
        assertThat(dao!!.getBySourcesLatest(arrayOf(125, 126, 2))).hasSize(2)
    }
}
