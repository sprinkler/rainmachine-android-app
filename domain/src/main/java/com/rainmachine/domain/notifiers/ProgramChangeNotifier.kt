package com.rainmachine.domain.notifiers

class ProgramChangeNotifier : Notifier<ProgramChange>()

sealed class ProgramChange {
    object StartStop : ProgramChange()

    object Properties : ProgramChange()
}