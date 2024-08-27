package com.example.mapapplication.activity

import com.google.gson.*
import com.mapbox.geojson.*
import java.lang.reflect.Type

class GeometryDeserializer : JsonDeserializer<Geometry> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Geometry {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString

        return when (type) {
            "Point" -> {
                val coordinates = jsonObject.getAsJsonArray("coordinates")
                Point.fromLngLat(coordinates[0].asDouble, coordinates[1].asDouble)
            }
            "LineString" -> {
                val coordinates = jsonObject.getAsJsonArray("coordinates").map { it.asJsonArray.map { coord -> coord.asDouble } }
                LineString.fromLngLats(coordinates.map { Point.fromLngLat(it[0], it[1]) })
            }
            "Polygon" -> {
                val coordinates = jsonObject.getAsJsonArray("coordinates").map { ring -> ring.asJsonArray.map { coord -> coord.asJsonArray.map { c -> c.asDouble } } }
                Polygon.fromLngLats(coordinates.map { it.map { Point.fromLngLat(it[0], it[1]) } })
            }
            else -> throw JsonParseException("Unsupported geometry type: $type")
        }
    }
}
