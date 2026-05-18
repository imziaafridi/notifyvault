package com.ziaafridi.notifyvault.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ziaafridi.notifyvault.R
import com.ziaafridi.notifyvault.settings.components.TopBarWidget
import com.ziaafridi.notifyvault.settings.components.termsOfUseData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfUseScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
//            .padding(horizontal = 5.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make the content scrollable

    ) {
        //"Terms of Use"
        TopBarWidget(onBackClick, stringResource(R.string.terms_of_use))
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            text = termsOfUseData,
            textAlign = TextAlign.Justify,

            )
    }
}
