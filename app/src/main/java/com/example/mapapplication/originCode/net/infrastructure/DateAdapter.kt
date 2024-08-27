package com.example.mapapplication.originCode.net.infrastructure

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

class DateAdapter(
    private val dateFormat: SimpleDateFormat = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
) : JsonSerializer<Date>, JsonDeserializer<Date> {
    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement = JsonPrimitive(src?.let { dateFormat.format(it) })

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date? = json?.asString?.let { dateFormat.parse(it) }
}