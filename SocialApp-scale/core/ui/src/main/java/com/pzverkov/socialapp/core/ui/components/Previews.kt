package com.pzverkov.socialapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pzverkov.socialapp.core.model.ErrorType
import com.pzverkov.socialapp.core.ui.theme.SocialAppTheme

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    SocialAppTheme {
        LoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    SocialAppTheme {
        ErrorState(errorType = ErrorType.NETWORK, onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    SocialAppTheme {
        EmptyState(title = "No items found", subtitle = "Check back later")
    }
}
