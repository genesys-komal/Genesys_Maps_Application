package com.example.mapapplication.navigation.helper

import android.app.Activity
import com.example.mapapplication.originCode.net.models.RouteResponse
import com.example.mapapplication.R
import com.example.mapapplication.helpers.decodePolyline
import com.example.mapapplication.helpers.encodePolyline
import com.example.mapapplication.navigation.NavigationLauncher
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.v5.models.LegAnnotation
import com.mapbox.services.android.navigation.v5.models.LegStep
import com.mapbox.services.android.navigation.v5.models.ManeuverModifier
import com.mapbox.services.android.navigation.v5.models.RouteLeg
import com.mapbox.services.android.navigation.v5.models.StepIntersection
import com.mapbox.services.android.navigation.v5.models.StepManeuver

object ValhallaMapper {
    private fun convertValhallaManeuverTypeToMapbox(valhallaType: Int): Pair<String, String?> {
        return when (valhallaType) {
            0 -> StepManeuver.TURN to null // None
            1 -> StepManeuver.DEPART to null // Start
            2 -> StepManeuver.DEPART to ManeuverModifier.RIGHT // StartRight
            3 -> StepManeuver.DEPART to ManeuverModifier.LEFT // StartLeft
            4 -> StepManeuver.ARRIVE to null // Destination
            5 -> StepManeuver.ARRIVE to ManeuverModifier.RIGHT // DestinationRight
            6 -> StepManeuver.ARRIVE to ManeuverModifier.LEFT // DestinationLeft
            7 -> StepManeuver.NEW_NAME to null // Becomes
            8 -> StepManeuver.CONTINUE to null // Continue
            9 -> StepManeuver.TURN to ManeuverModifier.SLIGHT_RIGHT // SlightRight
            10 -> StepManeuver.TURN to ManeuverModifier.RIGHT // Right
            11 -> StepManeuver.TURN to ManeuverModifier.SHARP_RIGHT // SharpRight
            12 -> StepManeuver.TURN to ManeuverModifier.UTURN // UturnRight
            13 -> StepManeuver.TURN to ManeuverModifier.UTURN // UturnLeft
            14 -> StepManeuver.TURN to ManeuverModifier.SHARP_LEFT // SharpLeft
            15 -> StepManeuver.TURN to ManeuverModifier.LEFT // Left
            16 -> StepManeuver.TURN to ManeuverModifier.SLIGHT_LEFT // SlightLeft
            17 -> StepManeuver.ON_RAMP to ManeuverModifier.STRAIGHT // RampStraight
            18 -> StepManeuver.ON_RAMP to ManeuverModifier.RIGHT // RampRight
            19 -> StepManeuver.ON_RAMP to ManeuverModifier.LEFT // RampLeft
            20 -> StepManeuver.OFF_RAMP to ManeuverModifier.RIGHT // ExitRight
            21 -> StepManeuver.OFF_RAMP to ManeuverModifier.LEFT // ExitLeft
            22 -> StepManeuver.CONTINUE to ManeuverModifier.STRAIGHT // StayStraight
            23 -> StepManeuver.CONTINUE to ManeuverModifier.RIGHT // StayRight
            24 -> StepManeuver.CONTINUE to ManeuverModifier.LEFT // StayLeft
            25 -> StepManeuver.MERGE to null // Merge
            26 -> StepManeuver.ROUNDABOUT to null // RoundaboutEnter
            27 -> StepManeuver.EXIT_ROUNDABOUT to null // RoundaboutExit
            37 -> StepManeuver.MERGE to ManeuverModifier.RIGHT // MergeRight
            38 -> StepManeuver.MERGE to ManeuverModifier.LEFT // MergeLeft
            39 -> StepManeuver.NOTIFICATION to null // ElevatorEnter
            40 -> StepManeuver.NOTIFICATION to null // StepsEnter
            41 -> StepManeuver.NOTIFICATION to null // EscalatorEnter
            42 -> StepManeuver.NOTIFICATION to null // BuildingEnter
            43 -> StepManeuver.NOTIFICATION to null // BuildingExit
            else -> StepManeuver.TURN to null // Default case
        }
    }

    fun turnOnNavigation(response: RouteResponse, activity: Activity) {
        val leg = response.trip.legs.first()
        val decodedShape = decodePolyline(leg.shape)
        val routeLegs = response.trip.legs.map { routeLeg ->
            RouteLeg.builder()
                .annotation(LegAnnotation.builder().build())
                .distance(routeLeg.summary.length)
                .duration(routeLeg.summary.time)
                .steps(routeLeg.maneuvers.map { step ->
                    val geometry = encodePolyline(
                        decodedShape.subList(
                            step.beginShapeIndex,
                            step.endShapeIndex
                        )
                    )

                    val (type, modifier) = convertValhallaManeuverTypeToMapbox(step.type)
                    val stepManeuver = StepManeuver.builder()
                        .rawLocation(
                            doubleArrayOf(
                                decodedShape[step.beginShapeIndex].latitude,
                                decodedShape[step.endShapeIndex].longitude
                            )
                        )
                        .modifier(modifier)
                        .type(type)
                        .bearingBefore(0.0)         // No OSM data for this
                        .bearingAfter(0.0)          // No OSM data for this
                        .instruction(step.instruction)
                        .build()

                    val stepIntersections: MutableList<StepIntersection> =
                        ArrayList<StepIntersection>()
                    val stepIntersection: StepIntersection =
                        StepIntersection.builder() // No other OSM data for this
                            .rawLocation(
                                doubleArrayOf(
                                    decodedShape[step.beginShapeIndex].latitude,
                                    decodedShape[step.endShapeIndex].longitude
                                )
                            )
                            .build()
                    stepIntersections.add(stepIntersection)

                    LegStep.builder()
                        .distance(step.length)
                        .duration(step.time)
                        .geometry(geometry)
                        .maneuver(stepManeuver)
                        .intersections(stepIntersections)
                        .mode(step.travelMode.value)
                        .weight(1.0)
                        .name("Jopa")
                        .build()
                }).build()
        }
//        val routeOptions = RouteOptions.builder()
//            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
//            .profile(DirectionsCriteria.PROFILE_DRIVING)
//            .accessToken("pk.0")
//            .user(userAgent)
//            .requestUuid("c945b0b4-9764-11ed-a8fc-0242ac120002") // fake UUID ver.1
//            .baseUrl("http://www.fakeurl.com") // fake url
//            .coordinates(decodedShape.map { Point.fromLngLat(it.longitude, it.latitude)  })
//            .voiceInstructions(true)
//            .bannerInstructions(true)
//            .build()

        val route = com.mapbox.services.android.navigation.v5.models.DirectionsRoute
            .builder()
            .geometry(leg.shape)
            //.routeOptions(routeOptions)
            .legs(routeLegs)
            .distance(response.trip.summary.length)
            .duration(response.trip.summary.time)
//            .durationTypical()
//            .weight()
            .voiceLanguage("en")
//            .weightName()
            .build()

        val options = NavigationLauncherOptions.builder()
            .directionsRoute(route)
            .shouldSimulateRoute(true)
            .lightThemeResId(R.style.TestNavigationViewDark)
            .darkThemeResId(R.style.TestNavigationViewDark)
            .initialMapCameraPosition(
                CameraPosition.Builder()
                    .target(
                        LatLng(
                            46.994221, 28.90638
                        )
                    ).build()
            )
            .build()

        NavigationLauncher.startNavigation(activity, options)
    }

}