package com.ziaafridi.notifyvault

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ziaafridi.notifyvault.settings.sections.AboutScreen
import com.ziaafridi.notifyvault.settings.sections.PrivacyPolicyScreen
import com.ziaafridi.notifyvault.settings.SettingScreen
import com.ziaafridi.notifyvault.settings.sections.TermsOfUseScreen
import com.ziaafridi.notifyvault.ui.theme.NotifyVaultTheme
import com.ziaafridi.notifyvault.ui.theme.VaultTheme
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotifyVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val onboardingPrefs = remember { OnboardingPreferences(this@MainActivity) }
                    val isOnboardingCompleted = remember { mutableStateOf(onboardingPrefs.isOnboardingCompleted()) }
                    val isPermissionGranted = remember { mutableStateOf(isNotificationServiceEnabled()) }
                    val navController = rememberNavController()

                    DisposableEffect(Unit) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                isPermissionGranted.value = isNotificationServiceEnabled()
                            }
                        }
                        lifecycle.addObserver(observer)
                        onDispose {
                            lifecycle.removeObserver(observer)
                        }
                    }

                    if (!isOnboardingCompleted.value) {
                        OnboardingScreen(
                            onComplete = {
                                onboardingPrefs.setOnboardingCompleted()
                                isOnboardingCompleted.value = true
                            }
                        )
                    } else {
                        PermissionScreen(
                            isGranted = isPermissionGranted.value,
                            onGrantClick = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotifyVaultTheme {
        Greeting("Android")
    }
}

@Composable
fun PermissionScreen(
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    navController: NavHostController
) {
    if (isGranted) {
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )

            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable("home") {
                ConversationListScreen(navController)
            }
            composable(
                "detail/{conversationId}/{sender}",
            ) { backStackEntry ->
                val conversationIdEncoded = backStackEntry.arguments?.getString("conversationId") ?: ""
                val senderEncoded = backStackEntry.arguments?.getString("sender") ?: ""
                val conversationId = Uri.decode(conversationIdEncoded)
                val sender = Uri.decode(senderEncoded)

                MessageDetailScreen(conversationId = conversationId, sender = sender) {
                    navController.popBackStack()
                }
            }
            composable("settings") {
                SettingScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPrivacyPolicyClick = {
                        navController.navigate("privacy_policy")
                    },
                    onTermsClick = { navController.navigate("terms_of_use") },
                    onAboutClick = { navController.navigate("about_us") }
                )
            }

            composable("privacy_policy") {
                PrivacyPolicyScreen(onBackClick = {
                    navController.popBackStack()
                })
            }
            composable("terms_of_use") {
                TermsOfUseScreen(onBackClick = {
                    navController.popBackStack()
                })
            }
            composable("about_us") {
                AboutScreen(onBackClick = {
                    navController.popBackStack()
                })
            }

        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.grant_notification_access),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.notification_read_access_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onGrantClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = VaultTheme.primaryButtonColors(),
            ) {
                Text(
                    text = stringResource(R.string.grant_permission),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.notification_access_description),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}