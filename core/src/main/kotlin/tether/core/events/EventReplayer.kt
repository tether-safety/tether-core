package tether.core.events

import tether.core.state.SystemState
import tether.core.state.SystemStateProjector

/**
 * Replays events to rebuild current system state.
 * This allows recovery after crashes, kills, or reboots.
 */
class EventReplayer(
    private val systemStateProjector: SystemStateProjector
) {

    /**
     * Rebuild state by applying all events in order.
     */
    fun replay(events: List<Event>): SystemState {
        var currentState = SystemState.initial()

        for (event in events) {
            currentState = systemStateProjector.apply(currentState, event)
        }

        return currentState
    }
}
