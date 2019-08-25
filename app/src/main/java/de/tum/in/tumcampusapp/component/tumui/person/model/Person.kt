package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import java.io.Serializable

/**
 * A person, often an [Employee] working at TUM.
 *
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Xml(name = "row")
data class Person(
    @PropertyElement(name = "geschlecht")
    var gender: String = "",
    @PropertyElement(name = "obfuscated_id")
    var id: String = "",
    @PropertyElement(name = "vorname")
    var name: String = "",
    @PropertyElement(name = "familienname")
    var surname: String = "",
    @PropertyElement(name = "bild_url")
    var imageUrl: String = ""
) : Serializable {

    val fullName: String
        get() = "$name $surname"

    val fullImageUrl: String
        get() = "https://campus.tum.de/tumonline/$imageUrl".replace("&amp;", "&")

    companion object {

        private const val serialVersionUID = -5210814076506102292L

        const val FEMALE = "W"
        const val MALE = "M"

        @JvmStatic fun fromRecent(r: Recent): Person {
            val split = r.name.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val p = Person()
            p.id = split[0]
            p.name = split[1]
            return p
        }

    }

}
