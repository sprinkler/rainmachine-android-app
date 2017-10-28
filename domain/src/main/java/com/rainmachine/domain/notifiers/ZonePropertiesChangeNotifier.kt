package com.rainmachine.domain.notifiers

class ZonePropertiesChangeNotifier : Notifier<ZonePropertiesChange>()

data class ZonePropertiesChange(val zoneId: Long, val zoneName: String, val isEnabled: Boolean,
                                val isMasterValve: Boolean)