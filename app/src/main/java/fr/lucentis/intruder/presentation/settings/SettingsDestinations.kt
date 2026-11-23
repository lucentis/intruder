package fr.lucentis.intruder.presentation.settings

enum class SettingsDestination(val route: String) {
    HOME("settings/home"),
    GLOBAL_PROTECTION("settings/global-protection"),
    MONITORING("settings/monitoring"),
    PHOTO_CAPTURE("settings/photo-capture"),
    TECHNICAL_NOTIFICATIONS("settings/technical-notifications"),
    LOCAL_STORAGE("settings/local-storage"),
    ABOUT("settings/about")
}
