package com.omarkarimli.disco.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.omarkarimli.disco.LocalPlayerAwareWindowInsets
import com.omarkarimli.disco.R
import com.omarkarimli.disco.ui.component.IconButton
import com.omarkarimli.disco.ui.utils.backToMain
import com.omarkarimli.disco.BuildConfig

@Immutable
sealed class DevSocialMedias(
    val title: String,
    val url: String,
    @DrawableRes val icon: Int,
) {
    object GitHub : DevSocialMedias(
        title = "GitHub",
        url = "https://github.com/omarkarimli",
        icon = R.drawable.github
    )

    object Instagram : DevSocialMedias(
        title = "Instagram",
        url = "https://www.instagram.com/omar.karimli",
        icon = R.drawable.instagram
    )

    object LinkedIn : DevSocialMedias(
        title = "LinkedIn",
        url = "https://www.linkedin.com/in/omarkarimli/",
        icon = R.drawable.linkedin
    )

    object Linktree : DevSocialMedias(
        title = "Linktree",
        url = "https://linktr.ee/omarkarimli",
        icon = R.drawable.linktree
    )

    object PrivacyPolicy : DevSocialMedias(
        title = "Privacy Policy",
        url = "https://github.com/omarkarimli/Disco/blob/main/PRIVACYPOLICY.md",
        icon = R.drawable.policy
    )

    companion object {
        val ALL_ITEMS = listOf(
            GitHub,
            Instagram,
            LinkedIn,
            Linktree,
            PrivacyPolicy
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
) {
    val uriHandler = LocalUriHandler.current
    val mediaItems = DevSocialMedias.ALL_ITEMS

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )

        Spacer(Modifier.height(4.dp))

        Image(
            painter = painterResource(R.drawable.small_icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.onBackground,
                BlendMode.SrcIn
            ),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { },
        )

        Text(
            text = "Disco",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape,
                    )
                    .padding(
                        horizontal = 6.dp,
                        vertical = 2.dp,
                    ),
            )

            Spacer(Modifier.width(4.dp))

            if (BuildConfig.DEBUG) {
                Spacer(Modifier.width(4.dp))

                Text(
                    text = "DEBUG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape,
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp,
                        ),
                )
            } else {
                Spacer(Modifier.width(4.dp))

                Text(
                    text = BuildConfig.ARCHITECTURE.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape,
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp,
                        ),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "OMAR",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(Modifier.height(8.dp))

        FlowRow {
            repeat(mediaItems.size) { index ->
                val item = mediaItems[index]

                IconButton(
                    onClick = {
                        uriHandler.openUri(item.url)
                    },
                ) {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.title
                    )
                }
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )
}