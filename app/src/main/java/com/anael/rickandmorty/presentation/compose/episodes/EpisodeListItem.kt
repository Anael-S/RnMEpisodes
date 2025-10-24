package com.anael.rickandmorty.presentation.compose.episodes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.anael.rickandmorty.R
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.presentation.utils.DateUtils.formatDateDdMmYyyy

/**
 * Compose UI to display a single episode (of our episode list)
 */
@Composable
fun EpisodeListItem(
    episode: Episode,
    onClick: () -> Unit
) {
    EpisodeCard(
        name = episode.name,
        airDateRaw = episode.airDate,
        code = episode.episodeCode,
        onClick = onClick
    )
}

@Composable
private fun EpisodeCard(
    name: String,
    airDateRaw: String,
    code: String,
    onClick: () -> Unit
) {
    val horizontalPadding = dimensionResource(id = R.dimen.margin_normal)
    val verticalPadding = dimensionResource(id = R.dimen.margin_small)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.card_side_margin))
            .padding(bottom = dimensionResource(id = R.dimen.card_bottom_margin))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = name,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = verticalPadding)
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_small)))

            // Meta info: air date + code
            Text(
                text = "Air date: ${formatDateDdMmYyyy(airDateRaw)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "EP: $code",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
