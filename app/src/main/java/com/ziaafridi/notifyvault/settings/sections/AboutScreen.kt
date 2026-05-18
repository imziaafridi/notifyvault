package com.ziaafridi.notifyvault.settings.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ziaafridi.notifyvault.R
import com.ziaafridi.notifyvault.settings.components.SettingsSection
import com.ziaafridi.notifyvault.settings.components.TopBarWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWidget(
            onBackClick = onBackClick,
            text = stringResource(R.string.about),
            startPadding = 10,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { AboutAppHeader() }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AboutSectionLabel(text = stringResource(R.string.about_section_privacy))
                    PrivacyCommitmentCard()
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AboutSectionLabel(text = stringResource(R.string.about_section_data))
                    DataStorageCard()
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(8.dp),
                )
            }
        }
    }
}

@Composable
private fun AboutSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
    )
}

@Composable
private fun AboutAppHeader() {
    val context = LocalContext.current
    val versionName = remember(context.packageName) {
        runCatching {
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        }.getOrNull() ?: "—"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            tonalElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Use a drawable — adaptive mipmap icons crash with painterResource in Compose.
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ) {
            Text(
                text = "${stringResource(R.string.version_prefix)} $versionName",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.about_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrivacyCommitmentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AboutIconBadge(
                icon = Icons.Filled.Shield,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.your_privacy_is_our_priority),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.about_privacy_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }
    }
}

@Composable
private fun DataStorageCard() {
    val dataPoints = listOf(
        DataPoint(Icons.Filled.PhoneAndroid, R.string.about_data_point_1),
        DataPoint(Icons.Filled.CloudOff, R.string.about_data_point_2),
        DataPoint(Icons.Filled.VerifiedUser, R.string.about_data_point_3),
        DataPoint(Icons.Filled.DeleteForever, R.string.about_data_point_4),
    )

    SettingsSection(contentPadding = PaddingValues(vertical = 4.dp)) {
        dataPoints.forEachIndexed { index, point ->
            AboutFeatureRow(
                icon = point.icon,
                text = stringResource(point.textRes),
            )
            if (index < dataPoints.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

private data class DataPoint(
    val icon: ImageVector,
    val textRes: Int,
)

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AboutIconBadge(icon = icon)

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AboutIconBadge(
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .padding(8.dp),
    )
}
