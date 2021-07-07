package de.tum.`in`.tumcampusapp.component.ui.bibreservation.model

import com.google.gson.annotations.SerializedName

enum class Bib {
    @SerializedName("Chemie")
    Chemie {
        override fun toString(): String {
            return "Chemie"
        }
    },

    @SerializedName("Maschinenwesen")
    Maschinenwesen {
        override fun toString(): String {
            return "Maschinenwesen"
        }
    },

    @SerializedName("Mathematik & Informatik")
    MI {
        override fun toString(): String {
            return "Mathematik & Informatik"
        }
    },

    @SerializedName("Medizin")
    Medizin {
        override fun toString(): String {
            return "Medizin"
        }
    },

    @SerializedName("Physik")
    Physik {
        override fun toString(): String {
            return "Physik"
        }
    },

    @SerializedName("Sport- & Gesundheitswissenschaften")
    Sport {
        override fun toString(): String {
            return "Sport- & Gesundheitswissenschaften"
        }
    },

    @SerializedName("Stammgelände")
    Stammgelaende {
        override fun toString(): String {
            return "Stammgelände"
        }
    },

    @SerializedName("Straubing")
    Straubing {
        override fun toString(): String {
            return "Straubing"
        }
    },

    @SerializedName("Weihenstephan")
    Weihenstephan {
        override fun toString(): String {
            return "Weihenstephan"
        }
    },
    Unknown
}
