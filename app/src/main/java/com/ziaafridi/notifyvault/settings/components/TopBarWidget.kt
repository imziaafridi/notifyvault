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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziaafridi.notifyvault.R
import com.ziaafridi.notifyvault.ui.theme.VaultTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBarWidget(
    onBackClick: (() -> Unit)? = null,
    text: String,
    size: Int = 20,
    weight: FontWeight = FontWeight.Medium,
    alignment: TextAlign = TextAlign.Start,
    startPadding: Int = 0,
) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.padding(start = startPadding.dp),
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = size.sp,
                    fontWeight = weight,
                ),
                textAlign = alignment,
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back_des),
                    )
                }
            }
        },
        colors = VaultTheme.topAppBarColors(),
    )
}
