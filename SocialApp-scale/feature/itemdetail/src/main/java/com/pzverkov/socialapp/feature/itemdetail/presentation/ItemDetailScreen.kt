package com.pzverkov.socialapp.feature.itemdetail.presentation

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.pzverkov.socialapp.core.ui.loadBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.pzverkov.socialapp.feature.itemdetail.R
import com.pzverkov.socialapp.core.ui.R as UiR
import com.pzverkov.socialapp.core.ui.theme.Accent
import com.pzverkov.socialapp.core.ui.theme.Dimens

// The purchase snackbar formats a string with the item title from a one-shot event, outside
// composition, where stringResource is unavailable. context.getString is the correct call here.
@SuppressLint("LocalContextGetResourceValueCall")
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
                state = current,
                onNavigateBack = onNavigateBack,
                onShareClick = viewModel::onShareClicked,
                onFavoriteClick = viewModel::onFavoriteClicked,
                onBuyClick = viewModel::onBuyClicked,
                onSummarizeClick = viewModel::onSummarizeClicked,
                onTranslateClick = viewModel::onTranslateClicked,
                onShowOriginalClick = viewModel::onShowOriginalClicked,
                onImageLoaded = viewModel::onImageLoaded,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ItemDetailContent(
    state: ItemDetailState.Loaded,
    onNavigateBack: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSummarizeClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onShowOriginalClick: () -> Unit,
    onImageLoaded: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = state.item
    val context = LocalContext.current
    val noMapsMessage = stringResource(R.string.no_maps_app)

    // Generate AI alt text only for screen-reader users on capable devices, so sighted users
    // never pay the inference cost. Falls back to the title until (or unless) it resolves.
    LaunchedEffect(state.canDescribeImage, item.imageUrl) {
        if (state.canDescribeImage && context.isScreenReaderActive()) {
            loadBitmap(context, item.imageUrl)?.let(onImageLoaded)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Box {
                ItemImage(
                    imageUrl = item.imageUrl,
                    contentDescription = state.imageContentDescription ?: item.title,
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
                        Icon(Icons.Default.Share, contentDescription = stringResource(UiR.string.share))
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
                            contentDescription = stringResource(if (item.isFavorite) UiR.string.remove_from_favorites else UiR.string.add_to_favorites),
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
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, noMapsMessage, Toast.LENGTH_SHORT).show()
                        }
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

                TranslationSection(
                    translation = state.translation,
                    onTranslateClick = onTranslateClick,
                    onShowOriginalClick = onShowOriginalClick,
                )

                AiSummarySection(summary = state.summary, onSummarizeClick = onSummarizeClick)

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
private fun TranslationSection(
    translation: TranslationUiState,
    onTranslateClick: () -> Unit,
    onShowOriginalClick: () -> Unit,
) {
    when (translation) {
        TranslationUiState.Hidden -> Unit
        is TranslationUiState.Available, TranslationUiState.Failed -> {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            OutlinedButton(onClick = onTranslateClick) {
                Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                Text(stringResource(R.string.translate_description))
            }
            if (translation == TranslationUiState.Failed) {
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Text(
                    text = stringResource(R.string.translation_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        TranslationUiState.Loading -> {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                Text(
                    text = stringResource(R.string.translation_in_progress),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        is TranslationUiState.Translated -> {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
                shape = RoundedCornerShape(Dimens.CardRadius),
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                    Text(
                        text = stringResource(R.string.translation_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = translation.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    TextButton(onClick = onShowOriginalClick, contentPadding = PaddingValues(0.dp)) {
                        Text(stringResource(R.string.show_original))
                    }
                }
            }
        }
    }
}

@Composable
private fun AiSummarySection(
    summary: SummaryUiState,
    onSummarizeClick: () -> Unit,
) {
    when (summary) {
        SummaryUiState.Hidden -> Unit
        SummaryUiState.Available, SummaryUiState.Failed -> {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            OutlinedButton(onClick = onSummarizeClick) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                Text(stringResource(R.string.summarize_with_ai))
            }
            if (summary == SummaryUiState.Failed) {
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Text(
                    text = stringResource(R.string.ai_summary_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        SummaryUiState.Loading -> {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                Text(
                    text = stringResource(R.string.ai_summary_in_progress),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        is SummaryUiState.Ready -> {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                shape = RoundedCornerShape(Dimens.CardRadius),
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                    Text(
                        text = stringResource(R.string.ai_summary_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = summary.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

private fun Context.isScreenReaderActive(): Boolean {
    val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    return manager.isEnabled && manager.isTouchExplorationEnabled
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
