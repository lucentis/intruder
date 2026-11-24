package fr.lucentis.intruder.platform.notification

import android.content.Context
import android.content.Intent
import fr.lucentis.intruder.MainActivity
import fr.lucentis.intruder.domain.notification.AlertScreen
import fr.lucentis.intruder.domain.notification.AlertTarget

// The contract between the notifications and MainActivity: how a destination is written
// into the intent of a notification, and read back when the app is opened.
object AlertIntent {

    private const val EXTRA_SCREEN = "alert_screen"
    private const val EXTRA_EVENT_ID = "alert_event_id"

    // A null target opens the app on its current screen.
    fun create(context: Context, target: AlertTarget?): Intent =
        Intent(context, MainActivity::class.java).apply {
            if (target != null) {
                putExtra(EXTRA_SCREEN, target.screen.name)
                target.eventId?.let { putExtra(EXTRA_EVENT_ID, it) }
            }
        }

    fun parse(intent: Intent?): AlertTarget? {
        if (intent == null) return null

        val name = intent.getStringExtra(EXTRA_SCREEN) ?: return null
        val screen = AlertScreen.entries.firstOrNull { it.name == name } ?: return null

        val eventId = if (intent.hasExtra(EXTRA_EVENT_ID)) {
            intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        } else {
            null
        }

        return AlertTarget(screen, eventId)
    }
}