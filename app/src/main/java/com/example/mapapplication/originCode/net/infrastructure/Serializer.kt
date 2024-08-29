package com.example.mapapplication.originCode.net.infrastructure

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Date

object Serializer {
    @JvmStatic
    val gsonBuilder: GsonBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        GsonBuilder()
            .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(ByteArray::class.java, ByteArrayAdapter())
    } else {
        GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateAdapter())
            .registerTypeAdapter(ByteArray::class.java, ByteArrayAdapter())
    }

    @JvmStatic
    val gson: Gson by lazy {
        gsonBuilder.create()
    }
}
