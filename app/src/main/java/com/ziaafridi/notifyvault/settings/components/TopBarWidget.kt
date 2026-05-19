package com.ziaafridi.notifyvault.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziaafridi.notifyvault.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBarWidget(
    onBackClick: (() -> Unit)? = null,
    text: String,
    size: Int = 20,
    weight: FontWeight = FontWeight.SemiBold,
    alignment: TextAlign = TextAlign.Start,
    startPadding: Int = 0,
) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.padding(start = startPadding.dp),
                text = text,
                fontSize = size.sp,
                fontWeight = weight,
                textAlign = alignment,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back_des),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
