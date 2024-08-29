package com.example.mapapplication.helpers

import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.geometry.LatLng

fun decodePolyline(polyline: String, precision: Int = 6): List<LatLng> {
    return PolylineUtils.decode(polyline, precision).map { LatLng(it.latitude(), it.longitude()) }
}

fun encodePolyline(list: List<LatLng>): String {
    return PolylineUtils.encode(list.map { Point.fromLngLat(it.longitude, it.latitude) }, 6)
}

fun Point.toLatLng(): LatLng {
    return LatLng(this.latitude(), this.longitude())
}

fun LatLng.toPoint(): Point {
    return Point.fromLngLat(this.longitude, this.latitude)
}

fun List<Double>.toLatLng(): LatLng {
    require(this.size == 2) { "Array must contain exactly two elements to convert to LatLng" }
    return LatLng(this[1], this[0])
}