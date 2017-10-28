package com.rainmachine.domain.util

import com.rainmachine.domain.model.LocationDetails
import com.rainmachine.domain.model.Provision
import com.rainmachine.domain.model.ZoneProperties
import org.joda.time.LocalTime

object DomainUtils {

    const val DEFAULT_WS_DAYS = 2
    const val DEFAULT_RAIN_SENSITIVITY = 0.8f
    const val DEFAULT_WIND_SENSITIVITY = 0.5f
    const val DEFAULT_WATER_ZONE_TIMER = 5 * 60 // in seconds
    const val DEVICE_CACHE_TIMEOUT = 60 // in seconds

    /* Calculates the volume of water that was applied on a zone for specified seconds (in metric
            system)
            It uses zone.area (m^2) if defined or zone.flow if not. If both defined and zone uses
            drip it uses .flow (m^3/h) */
    @JvmStatic
    fun computeWaterVolume(zoneProperties: ZoneProperties, seconds: Int): Float {
        var isDrip = false
        if (zoneProperties.sprinklerHeads == ZoneProperties.SprinklerHeads.SURFACE_DRIP
                || zoneProperties.sprinklerHeads == ZoneProperties.SprinklerHeads.BUBBLERS) {
            isDrip = true
        }

        val hasArea = zoneProperties.area(true) > 1
        val hasFlow = zoneProperties.flowRate(true) > 0

        var areaVolume = 0f
        var flowVolume = 0f

        if (hasArea) {
            areaVolume = MetricCalculator.rateToCubicMeters(zoneProperties.precipitationRate(true),
                    zoneProperties.area(true), seconds)
        }
        if (hasFlow) {
            flowVolume = Math.round(zoneProperties.flowRate(true) * seconds / 3600 * 1000) / 1000f
        }
        if (isDrip && hasFlow) {
            return flowVolume
        }
        return if (!hasArea) {
            flowVolume
        } else {
            areaVolume
        }
    }

    @JvmStatic
    fun isAtLeastOneWeekDaySelected(weekdays: BooleanArray): Boolean {
        return weekdays.any { it }
    }

    @JvmStatic
    fun isBetweenInclusive(startTime: LocalTime, endTime: LocalTime, time: LocalTime): Boolean {
        return !time.isBefore(startTime) && !time.isAfter(endTime)
    }

    @JvmStatic
    fun inferredLocation(provision: Provision): LocationDetails {
        val locationDetails = LocationDetails()
        locationDetails.latitude = provision.location.latitude
        locationDetails.longitude = provision.location.longitude
        val timezone = provision.location.timezone
        val isAmerica = timezone.startsWith("America") || timezone.startsWith("US")
        // Best guess scenario: Consider US country although it might be any country from America
        locationDetails.country = if (isAmerica) "US" else "Other"
        // Best guess scenario: Consider California although it might not be
        val isMaybeCalifornia = timezone.equals("America/Los_Angeles", ignoreCase = true)
                || timezone.equals("US/Pacific", ignoreCase = true)
        // Best guess scenario: Consider Florida although it might not be
        val isMaybeFlorida = timezone.equals("US/Eastern", ignoreCase = true)
                || timezone.equals("US/Central", ignoreCase = true)
        locationDetails.administrativeArea = when {
            isMaybeCalifornia -> "CA"
            isMaybeFlorida -> "FL"
            else -> "Other"
        }
        return locationDetails
    }
}
