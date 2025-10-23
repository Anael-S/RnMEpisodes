package com.anael.rickandmorty.presentation.compose.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anael.rickandmorty.R
import com.anael.rickandmorty.data.utils.NetworkError

@Composable
fun ErrorState(
    title: String,
    description: String? = null,
    onRetry: (() -> Unit)? = null,
    retryText: String = stringResource(id = R.string.retry),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onRetry) {
                Text(retryText)
            }
        }
    }
}

@Composable
fun rememberErrorStrings(cause: NetworkError): Pair<String, String> {
    return when (cause) {
        NetworkError.NoConnection ->
            stringResource(R.string.err_no_connection_title) to
                    stringResource(R.string.err_no_connection_desc)

        NetworkError.Timeout ->
            stringResource(R.string.err_timeout_title) to
                    stringResource(R.string.err_timeout_desc)

        is NetworkError.Http ->
            stringResource(R.string.err_server_title, cause.code) to
                    stringResource(R.string.err_server_desc)

        is NetworkError.Unknown ->
            stringResource(R.string.err_unknown_title) to
                    stringResource(R.string.err_unknown_desc)
    }
}


