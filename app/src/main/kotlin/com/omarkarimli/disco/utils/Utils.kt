package com.omarkarimli.disco.utils

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

fun reportException(throwable: Throwable) {
    throwable.printStackTrace()
}

@Suppress("DEPRECATION")
fun setAppLocale(context: Context, locale: Locale) {
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

@Composable
fun Modifier.pulseEffect(
    pulseEnabled: Boolean = true,
    targetScale: Float = 1.15f,
    initialScale: Float = 1f,
    color: Color = MaterialTheme.colorScheme.onSurface,
    shape: RoundedCornerShape = CircleShape,
    animationSpec: DurationBasedAnimationSpec<Float> = tween(1500)
): Modifier {
    val brush = SolidColor(color.copy(0.2f))
    val pulseTransition = rememberInfiniteTransition(label = "PulseTransition")

    // Continuous animations for scale and the base alpha loop
    val pulseScale by pulseTransition.animateFloat(
        initialValue = initialScale,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(animationSpec),
        label = "PulseScale"
    )

    val pulseAlphaLoop by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animationSpec),
        label = "PulseAlpha"
    )

    // Smoothly animate the overall visibility when pulseEnabled changes
    val toggleAlpha by animateFloatAsState(
        targetValue = if (pulseEnabled) 1f else 0f,
        animationSpec = tween(500), // Adjust duration for fade in/out speed
        label = "ToggleAlpha"
    )

    // Only run draw logic if the toggle alpha is visible to save performance
    return if (toggleAlpha > 0f) {
        this.drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)

            drawContext.canvas.save()
            val pivot = center
            drawContext.transform.scale(pulseScale, pulseScale, pivot)

            drawOutline(
                outline = outline,
                brush = brush,
                // Multiply the looping alpha by the toggle alpha for smoothness
                alpha = pulseAlphaLoop * toggleAlpha
            )

            drawContext.canvas.restore()
        }
    } else this
}

@Composable
fun Modifier.doublePulseEffect(
    pulseEnabled: Boolean = true,
    targetScale: Float = 1.15f,
    initialScale: Float = 1f,
    color: Color = MaterialTheme.colorScheme.onSurface,
    shape: RoundedCornerShape = CircleShape,
    duration: Int = 1500,
): Modifier {
    return this
        .pulseEffect(
            pulseEnabled = pulseEnabled,
            targetScale = targetScale,
            initialScale = initialScale,
            color = color,
            shape = shape,
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        )
        .pulseEffect(
            pulseEnabled = pulseEnabled,
            targetScale = targetScale,
            initialScale = initialScale,
            color = color,
            shape = shape,
            animationSpec = tween(
                durationMillis = (duration * 0.7f).toInt(),
                delayMillis = (duration * 0.3f).toInt(),
                easing = LinearEasing
            )
        )
}

@Composable
fun Modifier.wavyGradient(
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceContainer
    ),
    speed: Int = 3000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    // Animates from 0 to 1 to shift the gradient position
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(speed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    this.drawBehind {
        val width = size.width
        val height = size.height

        // We calculate an offset based on the phase to "pull" the gradient
        val offset = phase * width

        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(offset, 0f),
            end = Offset(offset + width, height),
            tileMode = TileMode.Repeated
        )

        drawRect(brush = brush)
    }
}