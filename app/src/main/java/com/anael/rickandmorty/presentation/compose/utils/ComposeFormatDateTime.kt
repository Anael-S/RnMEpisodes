package com.anael.rickandmorty.presentation.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Small Compose utils to format the date (use Context)
 */
@Composable
fun rememberFormatDateTime(): (Long) -> String {
    val context = LocalContext.current
    // Cache the context-bound formatters for performance
    val dateFmt = remember { android.text.format.DateFormat.getMediumDateFormat(context) }
    val timeFmt = remember { android.text.format.DateFormat.getTimeFormat(context) }
    return remember(dateFmt, timeFmt) {
        { epoch ->
            val date = java.util.Date(epoch)
            "${dateFmt.format(date)} - ${timeFmt.format(date)}"
        }
    }
}

@Composable
fun rememberFormatTime(): (Long) -> String {
    val context = LocalContext.current
    val timeFmt = remember { android.text.format.DateFormat.getTimeFormat(context) }
    return remember(timeFmt) { { epoch -> timeFmt.format(java.util.Date(epoch)) } }
}
