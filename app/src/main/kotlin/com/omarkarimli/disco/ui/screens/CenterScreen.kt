package com.omarkarimli.disco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.omarkarimli.innertube.YouTube
import com.omarkarimli.innertube.utils.parseCookieString
import com.omarkarimli.disco.BuildConfig
import com.omarkarimli.disco.LocalPlayerAwareWindowInsets
import com.omarkarimli.disco.R
import com.omarkarimli.disco.constants.AccountChannelHandleKey
import com.omarkarimli.disco.constants.AccountEmailKey
import com.omarkarimli.disco.constants.AccountNameKey
import com.omarkarimli.disco.constants.DataSyncIdKey
import com.omarkarimli.disco.constants.InnerTubeCookieKey
import com.omarkarimli.disco.constants.UseLoginForBrowse
import com.omarkarimli.disco.constants.VisitorDataKey
import com.omarkarimli.disco.constants.YtmSyncKey
import com.omarkarimli.disco.ui.component.InfoLabel
import com.omarkarimli.disco.ui.component.PreferenceEntry
import com.omarkarimli.disco.ui.component.SwitchPreference
import com.omarkarimli.disco.ui.component.TextFieldDialog
import com.omarkarimli.disco.utils.Updater
import com.omarkarimli.disco.utils.rememberPreference
import com.omarkarimli.disco.viewmodels.HomeViewModel
import com.omarkarimli.disco.viewmodels.AccountSettingsViewModel

@Composable
fun CenterScreen(
    navController: NavController,
    latestVersionName: String
) {
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val lazylistState = rememberLazyListState()

    val (accountNamePref, onAccountNameChange) = rememberPreference(AccountNameKey, "")
    val (accountEmail, onAccountEmailChange) = rememberPreference(AccountEmailKey, "")
    val (accountChannelHandle, onAccountChannelHandleChange) = rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val (visitorData, onVisitorDataChange) = rememberPreference(VisitorDataKey, "")
    val (dataSyncId, onDataSyncIdChange) = rememberPreference(DataSyncIdKey, "")

    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)
    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, true)

    val homeViewModel: HomeViewModel = hiltViewModel()
    val accountSettingsViewModel: AccountSettingsViewModel = hiltViewModel()
    val accountName by homeViewModel.accountName.collectAsState()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()

    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }

    LazyColumn(
        state = lazylistState,
        contentPadding = PaddingValues(
            start = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateStartPadding(layoutDirection),
            end = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateEndPadding(layoutDirection),
            top = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateTopPadding(),
            bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        if (isLoggedIn) navController.navigate("account")
                        else navController.navigate("login")
                    }
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                if (isLoggedIn && accountImageUrl != null) {
                    AsyncImage(
                        model = accountImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.login),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isLoggedIn) accountName else stringResource(R.string.login),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                if (isLoggedIn) {
                    OutlinedButton(
                        onClick = {
                            accountSettingsViewModel.logoutAndClearSyncedContent(context, onInnerTubeCookieChange)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.action_logout))
                    }
                }
            }
        }
        if (showTokenEditor) {
            item {
                val text = """
                ***INNERTUBE COOKIE*** =$innerTubeCookie
                ***VISITOR DATA*** =$visitorData
                ***DATASYNC ID*** =$dataSyncId
                ***ACCOUNT NAME*** =$accountNamePref
                ***ACCOUNT EMAIL*** =$accountEmail
                ***ACCOUNT CHANNEL HANDLE*** =$accountChannelHandle
                """.trimIndent()

                TextFieldDialog(
                    initialTextFieldValue = TextFieldValue(text),
                    onDone = { data ->
                        data.split("\n").forEach {
                            when {
                                it.startsWith("***INNERTUBE COOKIE*** =") -> onInnerTubeCookieChange(it.substringAfter("="))
                                it.startsWith("***VISITOR DATA*** =") -> onVisitorDataChange(it.substringAfter("="))
                                it.startsWith("***DATASYNC ID*** =") -> onDataSyncIdChange(it.substringAfter("="))
                                it.startsWith("***ACCOUNT NAME*** =") -> onAccountNameChange(it.substringAfter("="))
                                it.startsWith("***ACCOUNT EMAIL*** =") -> onAccountEmailChange(it.substringAfter("="))
                                it.startsWith("***ACCOUNT CHANNEL HANDLE*** =") -> onAccountChannelHandleChange(it.substringAfter("="))
                            }
                        }
                    },
                    onDismiss = { showTokenEditor = false },
                    singleLine = false,
                    maxLines = 20,
                    isInputValid = {
                        it.isNotEmpty() && "SAPISID" in parseCookieString(it)
                    },
                    extraContent = {
                        InfoLabel(text = stringResource(R.string.token_adv_login_description))
                    }
                )
            }
        }
        item {
            PreferenceEntry(
                title = {
                    Text(
                        when {
                            !isLoggedIn -> stringResource(R.string.advanced_login)
                            showToken -> stringResource(R.string.token_shown)
                            else -> stringResource(R.string.token_hidden)
                        }
                    )
                },
                icon = { Icon(painterResource(R.drawable.token), null) },
                onClick = {
                    if (!isLoggedIn) showTokenEditor = true
                    else if (!showToken) showToken = true
                    else showTokenEditor = true
                },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (isLoggedIn) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.more_content)) },
                    description = null,
                    icon = { Icon(painterResource(R.drawable.add_circle), null) },
                    checked = useLoginForBrowse,
                    onCheckedChange = {
                        YouTube.useLoginForBrowse = it
                        onUseLoginForBrowseChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.yt_sync)) },
                    icon = { Icon(painterResource(R.drawable.cached), null) },
                    checked = ytmSync,
                    onCheckedChange = onYtmSyncChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
        item {
            PreferenceEntry(
                title = { Text(stringResource(R.string.history)) },
                icon = { Icon(painterResource(R.drawable.history), stringResource(R.string.history)) },
                onClick = { navController.navigate("history") }
            )
        }
        item {
            PreferenceEntry(
                title = { Text(stringResource(R.string.stats)) },
                icon = { Icon(painterResource(R.drawable.stats), stringResource(R.string.stats)) },
                onClick = { navController.navigate("stats") }
            )
        }
        item {
            PreferenceEntry(
                title = { Text(stringResource(R.string.integrations)) },
                icon = { Icon(painterResource(R.drawable.integration), null) },
                onClick = { navController.navigate("settings/integrations") }
            )
        }
        item {
            PreferenceEntry(
                title = { Text(stringResource(R.string.settings)) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (latestVersionName != BuildConfig.VERSION_NAME) Badge()
                        }
                    ) {
                        Icon(painterResource(R.drawable.settings), contentDescription = null)
                    }
                },
                onClick = { navController.navigate("settings") }
            )
        }

        if (latestVersionName != BuildConfig.VERSION_NAME) {
            item {
                PreferenceEntry(
                    title = {
                        Text(text = stringResource(R.string.new_version_available))
                    },
                    description = latestVersionName,
                    icon = {
                        BadgedBox(badge = { Badge() }) {
                            Icon(painterResource(R.drawable.update), null)
                        }
                    },
                    onClick = {
                        uriHandler.openUri(Updater.getLatestDownloadUrl())
                    }
                )
            }
        }
    }
}