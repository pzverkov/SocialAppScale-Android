package com.pzverkov.socialapp.feature.itemdetail.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import com.pzverkov.socialapp.core.ui.components.ErrorState
import com.pzverkov.socialapp.core.ui.components.ItemImage
import com.pzverkov.socialapp.core.ui.components.LoadingIndicator
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.pzverkov.socialapp.R
import com.pzverkov.socialapp.core.ui.theme.Accent
import com.pzverkov.socialapp.core.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ItemDetailViewModel =
        assistedMetroViewModel<ItemDetailViewModel, ItemDetailViewModel.Factory> { create(itemId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ItemDetailEvent.ShareItem -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
                is ItemDetailEvent.ShowPurchaseMessage -> {
                    val msg = context.getString(R.string.purchase_coming_soon, event.itemTitle)
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val current = state) {
            is ItemDetailState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
            is ItemDetailState.Error -> ErrorState(
                errorType = current.errorType,
                onRetry = viewModel::retry,
                modifier = Modifier.padding(padding),
            )
            is ItemDetailState.Loaded -> ItemDetailContent(
                item = current.item,
                onNavigateBack = onNavigateBack,
                onShareClick = viewModel::onShareClicked,
                onFavoriteClick = viewModel::onFavoriteClicked,
                onBuyClick = viewModel::onBuyClicked,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ItemDetailContent(
    item: ItemDetailUiModel,
    onNavigateBack: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Box {
                ItemImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.DetailImageHeight),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpacingSm),
                ) {
                    FilledIconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    FilledIconButton(
                        onClick = onShareClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                    }
                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                    FilledIconButton(
                        onClick = onFavoriteClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            contentColor = if (item.isFavorite) Accent else Color.White,
                        ),
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(if (item.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites),
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(Dimens.SpacingLg)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(Dimens.SpacingSm),
                ) {
                    Text(
                        text = item.formattedPrice,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = Dimens.SpacingMd,
                            vertical = Dimens.SpacingXs,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingMd))
                Text(text = item.title, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(item.location)}"))
                        context.startActivity(intent)
                    },
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingXl))
                Text(
                    text = stringResource(R.string.description_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingXl))
                SellerCard(sellerName = item.sellerName)
                Spacer(modifier = Modifier.height(Dimens.SpacingLg))
            }
        }

        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Button(
                onClick = onBuyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpacingMd)
                    .height(Dimens.ButtonHeight),
                shape = RoundedCornerShape(Dimens.CardRadius),
            ) {
                Text(stringResource(R.string.buy_now), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SellerCard(sellerName: String) {
    val initials = sellerName.take(2).uppercase()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(Dimens.CardRadius),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.AvatarSize)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.width(Dimens.SpacingMd))
            Column {
                Text(text = sellerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.view_profile),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
