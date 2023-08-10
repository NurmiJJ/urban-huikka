package fi.sabriina.urbanhuikka.roomdb

import androidx.room.TypeConverter
import com.google.gson.Gson
import fi.sabriina.urbanhuikka.card.Card


class Converters {

    // Converter is required to add Card database
    @TypeConverter
    fun fromString(value: String): Card? {
        val gson = Gson()
        if (value != null) {
            return gson.fromJson(value, Card::class.java)
        }
        return null
    }

    @TypeConverter
    fun cardToString(card : Card?): String {
        val gson = Gson()
        return gson.toJson(card)
    }

}