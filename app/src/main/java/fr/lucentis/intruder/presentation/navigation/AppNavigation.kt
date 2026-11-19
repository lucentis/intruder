
package fr.lucentis.intruder.presentation.navigation

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import fr.lucentis.intruder.domain.event.EventProcessingOutcome
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.platform.monitoring.FailedUnlockMonitor
import fr.lucentis.intruder.platform.monitoring.SuccessfulUnlockMonitor
import fr.lucentis.intruder.presentation.home.DiagnosticInfo
import fr.lucentis.intruder.presentation.home.HomeScreen
import fr.lucentis.intruder.presentation.settings.SettingsScreen
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.presentation.gallery.GalleryScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(container: AppContainer) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Device Admin is managed by Android, not by our application.
    // We query the system to know whether our receiver is currently active.
    val devicePolicyManager = remember {
        context.getSystemService(DevicePolicyManager::class.java)
    }

    val adminComponent = remember {
        ComponentName(context, FailedUnlockMonitor::class.java)
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

    // Room emits a new list whenever the event table changes.
    val events by remember { container.eventRepository.observeAll() }
        .collectAsState(initial = emptyList())

    // Events are ordered newest first by the DAO.
    val eventCount = events.size
    val latestEvent = events.firstOrNull()
    
    val displayMode by remember { container.galleryRepository.displayMode }
        .collectAsState(initial = GalleryDisplayMode.GRID)

    // The processor publishes its latest diagnostic in memory.
    // This also updates the screen when an event is processed by a background service.
    val diagnostic by container.eventProcessor.diagnostics
        .collectAsState(initial = null)

    val settings by remember { container.settingsRepository.settings }
        .collectAsState(initial = AppSettings())

    // Keep track of the test event so that we can delete only that event.
    var lastTestEventId by remember { mutableStateOf<Long?>(null) }

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
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("⌂") },
                    label = { Text("Accueil") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("⚙") },
                    label = { Text("Réglages") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> GalleryScreen(
                        events = events,
                        displayMode = displayMode,
                        onDisplayModeChange = { mode ->
                            scope.launch {
                                container.galleryRepository.setDisplayMode(mode)
                            }
                        }
                    )

                1 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        SettingsScreen(
                            settings = settings,
                            isAdminActive = isAdminActive,
                            hasCameraPermission = hasCameraPermission,
                            hasTestEvent = lastTestEventId != null,
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
                            onToggleAutomaticCapture = {
                                scope.launch {
                                    container.settingsRepository.update {
                                        it.copy(
                                            automaticCaptureEnabled = !it.automaticCaptureEnabled
                                        )
                                    }
                                }
                            },
                            onToggleGlobalProtection = {
                                scope.launch {
                                    container.settingsRepository.update {
                                        it.copy(
                                            globalProtectionEnabled = !it.globalProtectionEnabled
                                        )
                                    }

                                    synchronizeSuccessfulUnlockMonitor(context, container)
                                }
                            },
                            onToggleFailedAttempts = {
                                scope.launch {
                                    container.settingsRepository.update {
                                        it.copy(
                                            monitorFailedAttempts = !it.monitorFailedAttempts
                                        )
                                    }
                                }
                            },
                            onToggleSuccessfulUnlocks = {
                                scope.launch {
                                    container.settingsRepository.update {
                                        it.copy(
                                            monitorSuccessfulUnlocks =
                                                !it.monitorSuccessfulUnlocks
                                        )
                                    }

                                    synchronizeSuccessfulUnlockMonitor(context, container)
                                }
                            },
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
                                // A delegated property cannot be smart-cast, hence the local copy.
                                val id = lastTestEventId

                                if (id != null) {
                                    scope.launch {
                                        container.eventRepository.deleteEvents(listOf(id))
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
