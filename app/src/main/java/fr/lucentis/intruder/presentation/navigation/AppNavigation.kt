package fr.lucentis.intruder.presentation.navigation

import android.Manifest
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import fr.lucentis.intruder.di.AppContainer
import fr.lucentis.intruder.domain.event.EventPersistenceException
import fr.lucentis.intruder.domain.event.EventProcessingOutcome
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.platform.monitoring.FailedUnlockMonitor
import fr.lucentis.intruder.platform.monitoring.SuccessfulUnlockMonitor
import fr.lucentis.intruder.presentation.common.OnResumeEffect
import fr.lucentis.intruder.presentation.debug.DebugScreen
import fr.lucentis.intruder.presentation.debug.DiagnosticInfo
import fr.lucentis.intruder.presentation.gallery.EventDetailScreen
import fr.lucentis.intruder.presentation.gallery.GalleryScreen
import fr.lucentis.intruder.presentation.settings.SettingsHost
import fr.lucentis.intruder.presentation.settings.protectionOverview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(container: AppContainer) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    var selectedEventId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    // Room emits a new list whenever the event table changes.
    val events by remember { container.eventRepository.observeAll() }
        .collectAsState(initial = emptyList())

    // Find the event currently displayed in the detail screen.
    val selectedEvent = selectedEventId?.let { id ->
        events.firstOrNull { it.id == id }
    }

    BackHandler(enabled = selectedEvent != null) {
        selectedEventId = null
    }

    // Device Admin is managed by Android, not by our application.
    // We query the system to know whether our receiver is currently active.
    val devicePolicyManager = remember {
        context.getSystemService(DevicePolicyManager::class.java)
    }

    val adminComponent = remember {
        ComponentName(context, FailedUnlockMonitor::class.java)
    }

    val notificationManager = remember {
        context.getSystemService(NotificationManager::class.java)
    }

    var isAdminActive by remember {
        mutableStateOf(devicePolicyManager.isAdminActive(adminComponent))
    }

    // Android displays its own confirmation screen. When it closes, we query
    // DevicePolicyManager again instead of assuming that the user accepted.
    val adminActivationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isAdminActive = devicePolicyManager.isAdminActive(adminComponent)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // areNotificationsEnabled covers both the runtime permission (Android 13+) and the
    // switch the user can turn off in the system settings.
    var notificationsEnabled by remember {
        mutableStateOf(notificationManager.areNotificationsEnabled())
    }

    // These three can be changed from Android's own settings while the app is in the
    // background, so they are read again each time the app comes back to the foreground.
    OnResumeEffect {
        isAdminActive = devicePolicyManager.isAdminActive(adminComponent)
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        notificationsEnabled = notificationManager.areNotificationsEnabled()
    }

    // Events are ordered newest first by the DAO.
    val eventCount = events.size
    val latestEvent = events.firstOrNull()

    val displayMode by remember { container.galleryRepository.displayMode }
        .collectAsState(initial = GalleryDisplayMode.GRID)

    // The processor publishes its latest diagnostic in memory.
    // This also updates the screen when an event is processed by a background service.
    val diagnostic by container.eventProcessor.diagnostics
        .collectAsState(initial = null)

    val settings by container.settingsRepository.settings
        .collectAsState(initial = AppSettings())

    val overview = protectionOverview(settings, isAdminActive, hasCameraPermission)

    // Keep track of the test event so that we can delete only that event.
    var lastTestEventId by remember { mutableStateOf<Long?>(null) }

    val appVersion = remember { readVersionName(context) }

    val diagnosticInfo = diagnostic?.let { result ->
        val processing = when (val outcome = result.outcome) {
            is EventProcessingOutcome.Recorded ->
                "Événement enregistré (ID ${outcome.event.id})."

            is EventProcessingOutcome.Ignored ->
                "Événement ignoré. Raison : ${outcome.reason}"

            is EventProcessingOutcome.PersistenceFailed ->
                "Échec de l'enregistrement Room. Photo orpheline : ${
                    if (outcome.orphanPhotoLeftBehind) "oui" else "non"
                }."
        }

        val captureFailure = result.captureFailure?.let { failure ->
            "Raison : ${failure.reason ?: "Inconnue"}\n" +
                "Étape : ${failure.stage ?: "Non déterminée"}\n" +
                "Détail : ${failure.technicalDetails ?: "Aucun détail fourni."}"
        }

        DiagnosticInfo(
            eventType = result.command.type.toString(),
            detectedAt = result.command.detectedAt.toString(),
            processing = processing,
            captureFailure = captureFailure
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    if (selectedEvent != null) {
                        EventDetailScreen(
                            event = selectedEvent,
                            onBack = {
                                selectedEventId = null
                            },
                            onDelete = {
                                val eventId = selectedEvent.id
                                selectedEventId = null

                                scope.launch {
                                    deleteEventsWithFeedback(
                                        container = container,
                                        context = context,
                                        ids = listOf(eventId)
                                    )
                                }
                            }
                        )
                    } else {
                        GalleryScreen(
                            events = events,
                            displayMode = displayMode,
                            onDisplayModeChange = { mode ->
                                scope.launch {
                                    container.galleryRepository.setDisplayMode(mode)
                                }
                            },
                            onEventClick = { event ->
                                selectedEventId = event.id
                            },
                            onDeleteEvents = { ids ->
                                scope.launch {
                                    deleteEventsWithFeedback(
                                        container = container,
                                        context = context,
                                        ids = ids
                                    )
                                }
                            },
                            protectionState = overview.global
                        )
                    }
                }

                1 -> SettingsHost(
                    settings = settings,
                    isAdminActive = isAdminActive,
                    hasCameraPermission = hasCameraPermission,
                    notificationsEnabled = notificationsEnabled,
                    eventCount = eventCount,
                    appVersion = appVersion,
                    onUpdate = { transform ->
                        scope.launch {
                            val before = container.settingsRepository.settings.first()
                            container.settingsRepository.update(transform)
                            val after = container.settingsRepository.settings.first()

                            // The unlock monitor is a foreground service: only start or
                            // stop it when a setting that controls it has changed.
                            val monitorChanged =
                                before.globalProtectionEnabled != after.globalProtectionEnabled ||
                                    before.monitorSuccessfulUnlocks != after.monitorSuccessfulUnlocks

                            if (monitorChanged) {
                                synchronizeSuccessfulUnlockMonitor(context, container)
                            }
                        }
                    },
                    onActivateAdmin = {
                        val intent = Intent(
                            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                        ).apply {
                            putExtra(
                                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                adminComponent
                            )
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Intruder doit pouvoir détecter les tentatives de déverrouillage échouées."
                            )
                        }

                        adminActivationLauncher.launch(intent)
                    },
                    onRequestCameraPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onOpenAppSettings = {
                        openSystemScreen(
                            context,
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    },
                    onOpenNotificationSettings = {
                        openSystemScreen(
                            context,
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
                                Settings.EXTRA_APP_PACKAGE,
                                context.packageName
                            )
                        )
                    },
                    onOpenGallery = { selectedTab = 0 }
                )

                2 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    DebugScreen(
                        latestEvent = latestEvent,
                        diagnostic = diagnosticInfo,
                        hasTestEvent = lastTestEventId != null,
                        onAddTestEvent = {
                            scope.launch {
                                lastTestEventId = container.eventRepository.insert(
                                    Event(
                                        type = EventType.UNLOCK_SUCCESS,
                                        timestamp = System.currentTimeMillis(),
                                        photoPath = null,
                                        captureStatus = CaptureStatus.SKIPPED,
                                        failureReason = null
                                    )
                                )
                            }
                        },
                        onDeleteTestEvent = {
                            val id = lastTestEventId

                            if (id != null) {
                                scope.launch {
                                    deleteEventsWithFeedback(
                                        container = container,
                                        context = context,
                                        ids = listOf(id)
                                    )
                                    lastTestEventId = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun readVersionName(context: Context): String = try {
    @Suppress("DEPRECATION")
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "inconnue"
} catch (exception: PackageManager.NameNotFoundException) {
    "inconnue"
}

// Opens a system settings screen. A device without that screen would throw, and a missing
// settings page must not crash the app.
private fun openSystemScreen(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Impossible d'ouvrir les paramètres Android.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private suspend fun synchronizeSuccessfulUnlockMonitor(
    context: Context,
    container: AppContainer
) {
    // Read the persisted settings after the update, rather than relying on
    // the potentially not-yet-updated Compose state.
    val settings = container.settingsRepository.settings.first()

    val serviceIntent = Intent(
        context,
        SuccessfulUnlockMonitor::class.java
    )

    if (settings.globalProtectionEnabled && settings.monitorSuccessfulUnlocks) {
        ContextCompat.startForegroundService(context, serviceIntent)
    } else {
        context.stopService(serviceIntent)
    }
}

// Deletes events and tells the user when something went wrong, instead of crashing.
private suspend fun deleteEventsWithFeedback(
    container: AppContainer,
    context: Context,
    ids: List<Long>
) {
    try {
        val result = container.eventRepository.deleteEvents(ids)
        val failures = result.photoDeletionFailures

        if (failures > 0) {
            val deleted = if (result.deletedEvents > 1) {
                "événements supprimés"
            } else {
                "événement supprimé"
            }

            val photos = if (failures > 1) {
                "photos n'ont pas pu être supprimées"
            } else {
                "photo n'a pas pu être supprimée"
            }

            Toast.makeText(
                context,
                "${result.deletedEvents} $deleted, mais $failures $photos.",
                Toast.LENGTH_LONG
            ).show()
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: EventPersistenceException) {
        Toast.makeText(
            context,
            "La suppression a échoué. Réessayez.",
            Toast.LENGTH_LONG
        ).show()
    }
}