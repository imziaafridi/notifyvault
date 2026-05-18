package com.ziaafridi.notifyvault.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ziaafridi.notifyvault.R
import com.ziaafridi.notifyvault.settings.components.SettingsItem
import com.ziaafridi.notifyvault.settings.components.SettingsSection
import com.ziaafridi.notifyvault.settings.components.TopBarWidget
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val context = LocalContext.current
    val appPackageName = context.packageName
    var showDeleteAllDataDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val viewModel: SettingsViewModel = viewModel()

    val sendEmailChooserTitle = stringResource(R.string.send_email_chooser_title)
    val dataDeletedMessage = stringResource(R.string.data_deleted)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            TopBarWidget(
                onBackClick = onBackClick,
                text = stringResource(R.string.settings_screen),
                startPadding = 10,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp + padding.calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SettingsSection {
                        SettingsItem(
                            icon = painterResource(R.drawable.contact_support),
                            title = stringResource(R.string.support),
                            description = stringResource(R.string.contact_us_for_support),
                            showDivider = false,
                        ) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("imziaafridi"))
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Notify Vault contact support",
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    sendEmailChooserTitle,
                                ),
                            )
                        }
                    }
                }

                item {
                    SettingsSection {
                        SettingsItem(
                            icon = painterResource(R.drawable.info),
                            title = stringResource(R.string.about),
                            description = stringResource(R.string.about_subtitle),
                            showDivider = false,
                        ) { onAboutClick() }

//                        SettingsItem(
//                            icon = painterResource(R.drawable.rating),
//                            title = stringResource(R.string.rate_us),
//                            description = stringResource(R.string.support_us_on_playstore),
//                        ) {
//                            val appDetail =
//                                "https://play.google.com/store/apps/details?id=$appPackageName"
//                            val intent = Intent(Intent.ACTION_VIEW, appDetail.toUri()).apply {
//                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            }
//                            context.startActivity(intent)
//                        }
//
//                        SettingsItem(
//                            icon = painterResource(R.drawable.external_link),
//                            title = stringResource(R.string.more_apps_by_dev),
//                            description = stringResource(R.string.discover_more_apps_on_playstore),
//                            showDivider = false,
//                        ) {
//                            val developerId = "123482368683246342"
//                            val developerPage =
//                                "https://play.google.com/store/apps/dev?id=$developerId"
//                            val intent = Intent(Intent.ACTION_VIEW, developerPage.toUri()).apply {
//                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            }
//                            context.startActivity(intent)
//                        }
                    }
                }

                item {
                    SettingsSection {
                        SettingsItem(
                            icon = painterResource(R.drawable.privacy),
                            title = stringResource(R.string.privacy_policy),
                            description = stringResource(R.string.read_our_privacy_policy),
                        ) {
//                            onPrivacyPolicyClick()
                        }

                        SettingsItem(
                            icon = painterResource(R.drawable.terms_of_use),
                            title = stringResource(R.string.terms_of_use),
                            description = stringResource(R.string.read_our_terms_of_use),
                            showDivider = false,
                        ) {
//                            onTermsClick()
                        }
                    }
                }

                item {
                    SettingsSection {
                        SettingsItem(
                            icon = painterResource(R.drawable.setting),
                            title = stringResource(R.string.delete_all_data),
                            description = stringResource(R.string.delete_all_data_subtitle),
                            showDivider = false,
                        ) { showDeleteAllDataDialog = true }
                    }
                }
            }
        }
    }

    if (showDeleteAllDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDataDialog = false },
            title = { Text(text = stringResource(R.string.delete_all_data)) },
            text = { Text(text = stringResource(R.string.delete_all_data_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDataDialog = false
                        viewModel.clearAllData {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = dataDeletedMessage
                                )
                            }
                        }

//                        scope.launch {
//                            withContext(Dispatchers.IO) {
//                                AppDatabase.getDatabase(context).clearAllTables()
//                            }
//                            snackbarHostState.showSnackbar(
//                                message = context.getString(R.string.data_deleted),
//                            )
//                        }
                    },
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDataDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

