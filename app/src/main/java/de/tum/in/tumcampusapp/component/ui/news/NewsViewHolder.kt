package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.tufilm.FilmCard
import de.tum.`in`.tumcampusapp.utils.addCompoundDrawablesWithIntrinsicBounds
import org.joda.time.format.DateTimeFormat
import java.util.regex.Pattern

class NewsViewHolder(itemView: View) : CardViewHolder(itemView) {

    private val imageView: ImageView? by lazy { itemView.findViewById<ImageView>(R.id.news_img) }
    private val titleTextView: TextView? by lazy { itemView.findViewById<TextView>(R.id.news_title) }
    private val dateTextView: TextView by lazy { itemView.findViewById<TextView>(R.id.news_src_date) }
    private val sourceTextView: TextView by lazy { itemView.findViewById<TextView>(R.id.news_src_title) }

    fun bind(newsItem: News, newsSource: NewsSources) = with(itemView) {
        val card = if (newsItem.isFilm) FilmCard(context) else NewsCard(context)
        card.setNews(newsItem)
        currentCard = card

        val dateFormatter = DateTimeFormat.mediumDate()
        dateTextView.text = dateFormatter.print(newsItem.date)

        loadNewsSourceInformation(context, newsSource)

        when (itemViewType) {
            R.layout.card_news_film_item -> bindFilmItem(newsItem)
            else -> bindNews(newsItem)
        }
    }

    private fun loadNewsSourceInformation(context: Context, newsSource: NewsSources) {
        sourceTextView.text = newsSource.title

        val newsSourceIcon = newsSource.icon
        if (newsSourceIcon.isNotBlank() && newsSourceIcon != "null") {
            Picasso.get().load(newsSourceIcon).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    val drawable = BitmapDrawable(context.resources, bitmap)
                    sourceTextView.addCompoundDrawablesWithIntrinsicBounds(start = drawable)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) = Unit

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit
            })
        }
    }

    private fun bindFilmItem(newsItem: News) {
        Picasso.get()
                .load(newsItem.image)
                .into(imageView)

        titleTextView?.text = COMPILE.matcher(newsItem.title).replaceAll("")
    }

    private fun bindNews(newsItem: News) {
        val imageUrl = newsItem.image
        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                    .load(newsItem.image)
                    .into(imageView)
        } else {
            imageView?.visibility = View.GONE
        }

        val showTitle = newsItem.isNewspread.not()
        titleTextView?.visibility = if (showTitle) View.VISIBLE else View.GONE

        if (showTitle) {
            titleTextView?.text = newsItem.title
        }
    }

    companion object {
        private val COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*")
    }

}
