package com.omarkarimli.disco.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun ButtonPlaceholder(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .height(ButtonDefaults.MinHeight)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.onSurface),
    )
}
