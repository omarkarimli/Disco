package com.omarkarimli.disco.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.omarkarimli.disco.LocalDatabase
import com.omarkarimli.disco.LocalPlayerConnection
import com.omarkarimli.disco.R
import com.omarkarimli.disco.constants.PlayerBackgroundStyle
import com.omarkarimli.disco.constants.PlayerBackgroundStyleKey
import com.omarkarimli.disco.constants.SliderStyle
import com.omarkarimli.disco.constants.SliderStyleKey
import com.omarkarimli.disco.db.entities.LyricsEntity
import com.omarkarimli.disco.extensions.togglePlayPause
import com.omarkarimli.disco.extensions.toggleRepeatMode
import com.omarkarimli.disco.models.MediaMetadata
import com.omarkarimli.disco.ui.component.Lyrics
import com.omarkarimli.disco.ui.component.LocalMenuState
import com.omarkarimli.disco.ui.component.PlayerSliderTrack
import me.saket.squiggles.SquigglySlider
import com.omarkarimli.disco.ui.menu.LyricsMenu
import com.omarkarimli.disco.ui.theme.PlayerColorExtractor
import com.omarkarimli.disco.ui.theme.PlayerSliderColors
import com.omarkarimli.disco.utils.rememberEnumPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.omarkarimli.disco.utils.makeTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    modifier: Modifier = Modifier,
    mediaMetadata: MediaMetadata,
    onBackClick: () -> Unit,
    backgroundAlpha: Float = 1f
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val playerConnection = LocalPlayerConnection.current ?: return
    val player = playerConnection.player
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()
    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.SLIM)
    val currentLyrics by playerConnection.currentLyrics.collectAsState(initial = null)
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    LaunchedEffect(mediaMetadata.id, currentLyrics) {
        if (currentLyrics == null) {
            delay(500)
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        com.omarkarimli.disco.di.LyricsHelperEntryPoint::class.java
                    )
                    val lyricsHelper = entryPoint.lyricsHelper()
                    val lyrics = lyricsHelper.getLyrics(mediaMetadata)
                    database.query {
                        upsert(LyricsEntity(mediaMetadata.id, lyrics))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(C.TIME_UNSET) }
    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    val playerBackground by rememberEnumPreference(PlayerBackgroundStyleKey, PlayerBackgroundStyle.DEFAULT)
    val isSystemInDarkTheme = isSystemInDarkTheme()

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(mediaMetadata.id, playerBackground) {
        if (playerBackground == PlayerBackgroundStyle.GRADIENT && mediaMetadata.thumbnailUrl != null) {
            val cachedColors = gradientColorsCache[mediaMetadata.id]
            if (cachedColors != null) {
                gradientColors = cachedColors
                return@LaunchedEffect
            }
            withContext(Dispatchers.IO) {
                val request = ImageRequest.Builder(context)
                    .data(mediaMetadata.thumbnailUrl)
                    .size(100, 100)
                    .allowHardware(false)
                    .memoryCacheKey("gradient_${mediaMetadata.id}")
                    .build()
                val result = runCatching { context.imageLoader.execute(request).image }.getOrNull()
                if (result != null) {
                    val bitmap = result.toBitmap()
                    val palette = withContext(Dispatchers.Default) {
                        Palette.from(bitmap)
                            .maximumColorCount(8)
                            .resizeBitmapArea(100 * 100)
                            .generate()
                    }
                    val extractedColors = PlayerColorExtractor.extractGradientColors(
                        palette = palette,
                        fallbackColor = fallbackColor
                    )
                    gradientColorsCache[mediaMetadata.id] = extractedColors
                    withContext(Dispatchers.Main) { gradientColors = extractedColors }
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    val textBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
        PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT -> Color.White
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(500)
                position = player.currentPosition
                duration = player.duration
            }
        }
    }

    BackHandler(onBack = onBackClick)

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundAlpha)
        ) {
            when (playerBackground) {
                PlayerBackgroundStyle.BLUR -> {
                    AnimatedContent(
                        targetState = mediaMetadata.thumbnailUrl,
                        transitionSpec = {
                            fadeIn(tween(800)).togetherWith(fadeOut(tween(800)))
                        },
                        label = "blurBackground"
                    ) { thumbnailUrl ->
                        if (thumbnailUrl != null) {
                            AsyncImage(
                                model = thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(if (isSystemInDarkTheme) 150.dp else 100.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
                PlayerBackgroundStyle.GRADIENT -> {
                    AnimatedContent(
                        targetState = gradientColors,
                        transitionSpec = {
                            fadeIn(tween(800)).togetherWith(fadeOut(tween(800)))
                        },
                        label = "gradientBackground"
                    ) { colors ->
                        if (colors.isNotEmpty()) {
                            val gradientColorStops = if (colors.size >= 3) {
                                arrayOf(
                                    0.0f to colors[0],
                                    0.5f to colors[1],
                                    1.0f to colors[2]
                                )
                            } else {
                                arrayOf(
                                    0.0f to colors[0],
                                    0.6f to colors[0].copy(alpha = 0.7f),
                                    1.0f to Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(colorStops = gradientColorStops))
                                    .background(Color.Black.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
                else -> {
                    // DEFAULT background
                }
            }

            if (playerBackground != PlayerBackgroundStyle.DEFAULT) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }

        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .zIndex(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 16.dp)
                                ) { onBackClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.expand_more),
                                contentDescription = stringResource(R.string.close),
                                tint = textBackgroundColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.now_playing),
                                style = MaterialTheme.typography.titleMedium,
                                color = textBackgroundColor
                            )
                            Text(
                                text = mediaMetadata.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = textBackgroundColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 16.dp)
                                ) {
                                    menuState.show {
                                        LyricsMenu(
                                            lyricsProvider = { currentLyrics },
                                            songProvider = { currentSong?.song },
                                            mediaMetadataProvider = { mediaMetadata },
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_horiz),
                                contentDescription = stringResource(R.string.more_options),
                                tint = textBackgroundColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Lyrics(sliderPositionProvider = { sliderPosition })
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (sliderStyle) {
                                SliderStyle.DEFAULT -> {
                                    Slider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = { sliderPosition = it.toLong() },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        colors = PlayerSliderColors.defaultSliderColors(textBackgroundColor, playerBackground,
                                            isSystemInDarkTheme
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                SliderStyle.SQUIGGLY -> {
                                    SquigglySlider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = { sliderPosition = it.toLong() },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        colors = PlayerSliderColors.squigglySliderColors(textBackgroundColor, playerBackground,
                                            isSystemInDarkTheme
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        squigglesSpec = SquigglySlider.SquigglesSpec(
                                            amplitude = if (isPlaying) (2.dp).coerceAtLeast(2.dp) else 0.dp,
                                            strokeWidth = 3.dp,
                                        )
                                    )
                                }
                                SliderStyle.SLIM -> {
                                    Slider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = { sliderPosition = it.toLong() },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                                        track = { sliderState ->
                                            PlayerSliderTrack(
                                                sliderState = sliderState,
                                                colors = PlayerSliderColors.slimSliderColors(textBackgroundColor, playerBackground,
                                                    isSystemInDarkTheme
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = makeTimeString(sliderPosition ?: position),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textBackgroundColor.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textBackgroundColor.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                val controlButtonSize = maxWidth / 5f

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { playerConnection.player.toggleRepeatMode() },
                                        modifier = Modifier.size(controlButtonSize)
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                when (repeatMode) {
                                                    Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL -> R.drawable.repeat
                                                    Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                                                    else -> R.drawable.repeat
                                                }
                                            ),
                                            contentDescription = "Repeat",
                                            tint = if (repeatMode == Player.REPEAT_MODE_OFF) textBackgroundColor.copy(alpha = 0.4f) else textBackgroundColor,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { player.seekToPrevious() },
                                        modifier = Modifier.size(controlButtonSize)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.skip_previous),
                                            contentDescription = null,
                                            tint = textBackgroundColor,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { player.togglePlayPause() },
                                        modifier = Modifier.size(controlButtonSize)
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                when {
                                                    playbackState == STATE_ENDED -> R.drawable.replay
                                                    isPlaying -> R.drawable.pause
                                                    else -> R.drawable.play
                                                }
                                            ),
                                            contentDescription = null,
                                            tint = textBackgroundColor,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { player.seekToNext() },
                                        modifier = Modifier.size(controlButtonSize)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.skip_next),
                                            contentDescription = null,
                                            tint = textBackgroundColor,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled },
                                        modifier = Modifier.size(controlButtonSize)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.shuffle),
                                            contentDescription = "Shuffle",
                                            tint = if (shuffleModeEnabled) textBackgroundColor else textBackgroundColor.copy(alpha = 0.4f),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 16.dp)
                                ) { onBackClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.expand_more),
                                contentDescription = stringResource(R.string.close),
                                tint = textBackgroundColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.now_playing),
                                style = MaterialTheme.typography.titleMedium,
                                color = textBackgroundColor
                            )
                            Text(
                                text = mediaMetadata.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = textBackgroundColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 16.dp)
                                ) {
                                    menuState.show {
                                        LyricsMenu(
                                            lyricsProvider = { currentLyrics },
                                            songProvider = { currentSong?.song },
                                            mediaMetadataProvider = { mediaMetadata },
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_horiz),
                                contentDescription = stringResource(R.string.more_options),
                                tint = textBackgroundColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Lyrics(sliderPositionProvider = { sliderPosition })
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        when (sliderStyle) {
                            SliderStyle.DEFAULT -> {
                                Slider(
                                    value = (sliderPosition ?: position).toFloat(),
                                    valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                    onValueChange = { sliderPosition = it.toLong() },
                                    onValueChangeFinished = {
                                        sliderPosition?.let {
                                            player.seekTo(it)
                                            position = it
                                        }
                                        sliderPosition = null
                                    },
                                    colors = PlayerSliderColors.defaultSliderColors(textBackgroundColor, playerBackground,
                                        isSystemInDarkTheme
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            SliderStyle.SQUIGGLY -> {
                                SquigglySlider(
                                    value = (sliderPosition ?: position).toFloat(),
                                    valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                    onValueChange = { sliderPosition = it.toLong() },
                                    onValueChangeFinished = {
                                        sliderPosition?.let {
                                            player.seekTo(it)
                                            position = it
                                        }
                                        sliderPosition = null
                                    },
                                    colors = PlayerSliderColors.squigglySliderColors(textBackgroundColor, playerBackground,
                                        isSystemInDarkTheme
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    squigglesSpec = SquigglySlider.SquigglesSpec(
                                        amplitude = if (isPlaying) (2.dp).coerceAtLeast(2.dp) else 0.dp,
                                        strokeWidth = 3.dp,
                                    )
                                )
                            }
                            SliderStyle.SLIM -> {
                                Slider(
                                    value = (sliderPosition ?: position).toFloat(),
                                    valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                    onValueChange = { sliderPosition = it.toLong() },
                                    onValueChangeFinished = {
                                        sliderPosition?.let {
                                            player.seekTo(it)
                                            position = it
                                        }
                                        sliderPosition = null
                                    },
                                    thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                                    track = { sliderState ->
                                        PlayerSliderTrack(
                                            sliderState = sliderState,
                                            colors = PlayerSliderColors.slimSliderColors(textBackgroundColor, playerBackground,
                                                isSystemInDarkTheme
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = makeTimeString(sliderPosition ?: position),
                                style = MaterialTheme.typography.labelMedium,
                                color = textBackgroundColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                                style = MaterialTheme.typography.labelMedium,
                                color = textBackgroundColor.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val controlButtonSize = maxWidth / 5f

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { playerConnection.player.toggleRepeatMode() },
                                    modifier = Modifier.size(controlButtonSize)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            when (repeatMode) {
                                                Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL -> R.drawable.repeat
                                                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                                                else -> R.drawable.repeat
                                            }
                                        ),
                                        contentDescription = "Repeat",
                                        tint = if (repeatMode == Player.REPEAT_MODE_OFF) textBackgroundColor.copy(alpha = 0.4f) else textBackgroundColor,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { player.seekToPrevious() },
                                    modifier = Modifier.size(controlButtonSize)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.skip_previous),
                                        contentDescription = null,
                                        tint = textBackgroundColor,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { player.togglePlayPause() },
                                    modifier = Modifier.size(controlButtonSize)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            when {
                                                playbackState == STATE_ENDED -> R.drawable.replay
                                                isPlaying -> R.drawable.pause
                                                else -> R.drawable.play
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = textBackgroundColor,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { player.seekToNext() },
                                    modifier = Modifier.size(controlButtonSize)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.skip_next),
                                        contentDescription = null,
                                        tint = textBackgroundColor,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled },
                                    modifier = Modifier.size(controlButtonSize)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = "Shuffle",
                                        tint = if (shuffleModeEnabled) textBackgroundColor else textBackgroundColor.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
