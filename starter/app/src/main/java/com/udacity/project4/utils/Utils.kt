package com.udacity.project4.utils
val GEO_FENCE_EVENT = "GEO_FENCE_EVENT"
private const val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12

/**
 * For this sample, geofences expire after twelve hours.
 */
const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
const val GEOFENCE_RADIUS_IN_METERS = 1609f // 1 mile, 1.6 km
