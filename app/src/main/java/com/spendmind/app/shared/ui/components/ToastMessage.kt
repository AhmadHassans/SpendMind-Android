package com.spendmindai.app.shared.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A lightweight Snackbar-based toast composable.
 *
 * Display it by passing a non-null [message]. When [message] becomes null the snackbar is
 * dismissed automatically.
 *
 * Usage (inside a Scaffold or Box):
 * ```
 * ToastMessage(message = viewModel.toastMessage)
 * ```
 *
 * @param message      Text to display. Pass `null` to hide / not show anything.
 * @param actionLabel  Optional button label (e.g. "Undo"). `null` hides the action.
 * @param onAction     Callback invoked when the action button is tapped.
 * @param duration     How long the snackbar stays visible.
 * @param modifier     Optional [Modifier].
 */
@Composable
fun ToastMessage(
    message: String?,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier.padding(bottom = 8.dp)
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionColor = MaterialTheme.colorScheme.inversePrimary
        )
    }
}

/**
 * Convenience overload that accepts a pre-created [SnackbarHostState] so the caller
 * can share a single host state with other composables in the same scaffold.
 *
 * @param hostState    Shared [SnackbarHostState].
 * @param message      Text to display. Pass `null` to skip.
 * @param actionLabel  Optional button label.
 * @param onAction     Callback when action is tapped.
 * @param duration     Display duration.
 */
@Composable
fun ToastMessage(
    hostState: SnackbarHostState,
    message: String?,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }
}
