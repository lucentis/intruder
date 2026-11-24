package fr.lucentis.intruder.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import fr.lucentis.intruder.MainActivity
import fr.lucentis.intruder.domain.notification.TechnicalAlert
import fr.lucentis.intruder.domain.notification.TechnicalAlertNotifier
import fr.lucentis.intruder.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicInteger

class AndroidTechnicalAlertNotifier(
    context: Context,
    private val settingsRepository: SettingsRepository
) : TechnicalAlertNotifier {

    private val appContext = context.applicationContext

    // Seeded with the clock so ids stay unique across process restarts: each occurrence
    // gets its own notification instead of replacing an earlier one that is still displayed.
    private val nextId = AtomicInteger((System.currentTimeMillis() / 1000).toInt())

    override suspend fun notify(alert: TechnicalAlert) {
        val settings = settingsRepository.settings.first()

        val enabled = when (alert) {
            TechnicalAlert.PROTECTION_INTERRUPTED -> settings.notifyProtectionInterrupted
            TechnicalAlert.MISSING_PERMISSION -> settings.notifyMissingPermission
            TechnicalAlert.CAPTURE_PROBLEM -> settings.notifyCaptureProblem
        }

        if (!enabled) return

        val manager = appContext.getSystemService(NotificationManager::class.java)

        // Creating a channel that already exists does nothing.
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Alertes techniques",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Problèmes techniques de la protection"
            }
        )

        // Without the notification permission Android drops the notification silently. The
        // problem stays visible in the app, as the specification requires.
        manager.notify(nextId.incrementAndGet(), buildNotification())
    }

    // The text is the same for every category and says nothing sensitive, because it can be
    // read on the lock screen. The details are in the app.
    private fun buildNotification(): Notification {
        val text = "Une fonctionnalité de protection a rencontré une erreur. " +
            "Ouvrez Intruder pour en savoir plus."

        val openApp = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Problème technique détecté")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setCategory(Notification.CATEGORY_ERROR)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "technical_alerts"
    }
}