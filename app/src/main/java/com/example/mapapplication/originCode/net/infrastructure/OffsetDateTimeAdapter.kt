package com.example.mapapplication.originCode.net.infrastructure

import android.annotation.TargetApi
import android.os.Build
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonToken.NULL
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@TargetApi(Build.VERSION_CODES.O)
class OffsetDateTimeAdapter(private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME) : TypeAdapter<OffsetDateTime>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter?, value: OffsetDateTime?) {
        if (value == null) {
            out?.nullValue()
        } else {
            out?.value(formatter.format(value))
        }
    }

    @Throws(IOException::class)
    override fun read(out: JsonReader?): OffsetDateTime? {
        out ?: return null

        return when (out.peek()) {
            NULL -> {
                out.nextNull()
                null
            }

            else -> {
                OffsetDateTime.parse(out.nextString(), formatter)
            }
        }
    }
}
