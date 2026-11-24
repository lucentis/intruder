package fr.lucentis.intruder.presentation.common

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// Runs the block each time the app comes back to the foreground. Permissions can be
// changed from Android's own settings while the app is in the background, so the screens
// re-read them on resume instead of trusting a value read once.
@Composable
fun OnResumeEffect(onResume: () -> Unit) {
    val currentOnResume by rememberUpdatedState(onResume)
    val activity = LocalContext.current.findComponentActivity()

    DisposableEffect(activity) {
        val lifecycle = activity?.lifecycle

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) currentOnResume()
        }

        lifecycle?.addObserver(observer)

        onDispose { lifecycle?.removeObserver(observer) }
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}