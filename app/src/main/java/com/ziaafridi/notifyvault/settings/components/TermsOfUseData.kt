package com.ziaafridi.notifyvault.settings.components

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

val termsOfUseData = buildAnnotatedString {
//    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
//        append("TERMS OF USE\n")
//    }
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Notify Vault\n")
    }
    append("By accessing and using this service, you accept and agree to be bound by the terms and provisions of this agreement. In addition, when using this service, you shall be subject to any posted guidelines or rules applicable to such services. Any participation in this service will constitute acceptance of this agreement. If you do not agree to abide by the above, please do not use this service.\n")
    append("Notify Vault (also referred to as “the App” or “the Service”) is developed and maintained by the Service Owners (also referred to as “Developers” or “Authors”) as an ad-supported application. This Service is provided to users on an “AS IS” and “AS AVAILABLE” basis, without any warranties of any kind, either express or implied.\n")
    append("The Developers do not guarantee the correct, complete, or uninterrupted behavior of the App under any circumstances. The App depends on system notifications and device behavior, and therefore message recovery may not always be accurate or complete.\n")
    append("In no event shall the Developers be liable for any direct, indirect, incidental, special, or consequential damages, whether physical or not, arising out of the use or inability to use the Service.\n")
    append("The User shall be considered solely responsible for their actions and for any consequences arising from the usage of this Service.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("User Agreement\n")
    }
    append("The User agrees to use the App in compliance with all applicable laws and regulations. In particular, the User agrees to respect the privacy and rights of others and commits to:\n")
    append(" - Inform relevant parties about the usage of this Service where required by law\n")
    append(" - Delete any recovered data (text or multimedia) whenever requested by the rightful owner of that data\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Privacy Policy\n")
    }
    append("By accessing and using this Service, you declare that you have read, understood, and accepted the Privacy Policy.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Notification of Changes\n")
    }
    append("The Service Owners reserve the right to modify these Terms of Use at any time. Continued use of the Service after changes are posted will constitute acceptance of the updated terms. Any changes will be posted on this page.\n")
}