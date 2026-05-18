package com.ziaafridi.notifyvault.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
        modifier = Modifier
            .padding(start = 16.dp),
        title = {
            Text(
                modifier = Modifier
                    .padding(start = startPadding.dp),
                text = text,
                fontSize = size.sp,
                fontWeight = weight,
                textAlign = alignment,
            )
        },
        navigationIcon = {
            // Only show the back button if a callback is provided
            if (onBackClick != null) {
                IconButton(
                    modifier = Modifier
                        .size(20.dp),
                    onClick = onBackClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = stringResource(R.string.navigate_back_des),
                    )
                }
            }
        }
    )
}
