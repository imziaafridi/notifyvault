package com.ziaafridi.notifyvault.settings.components

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

val privacyPolicyData = buildAnnotatedString {
//    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
//        append("PRIVACY POLICY\n")
//    }
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Reveal WhatsApp Messages\n")
    }
    append("In this Privacy Policy (\"Policy\"), we, the Developers, describe how we collect, use, and protect information obtained from users of the Reveal WhatsApp Messages app (the “App”) and its services (collectively, the “Services”).\n")
    append("If you choose to use this Service, you agree to the collection and use of information in relation to this Policy. Any information collected is used solely for providing and improving the Service and will not be shared with anyone except as described in this Privacy Policy.\n")
    append("By using the App or any of our Services, you agree that your information will be handled as described in this Policy. Your use of our App and any dispute over privacy is subject to this Policy and our Terms of Use.\n")
    append("The terms used in this Privacy Policy have the same meanings as in the Terms of Use unless otherwise defined.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Data Collected by Reveal WhatsApp Messages\n")
    }
    append("The Service provides the ability to retrieve chat messages (text and limited metadata) that may disappear for any reason, through a local notification-based backup mechanism.\n\n")
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("Important data handling principles:\n")
    }
    append(" - All recovered messages are stored locally on the user’s device only\n")
    append(" - No recovered data is uploaded to any server or cloud storage\n")
    append(" - The Developers do not have access to any user messages\n")
    append(" - When the App is uninstalled, all locally stored data is permanently deleted\n")
    append(" - The User is the sole owner and controller of their recovered data\n")
    append("The App does not access private WhatsApp servers or databases and relies only on notifications visible on the user’s device.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Log Data and Analytics\n")
    }
    append("Whenever you use this Service, in the event of an error in the App, some data and information may be collected (through third-party services) called Log Data. This Log Data may include:\n")
    append(" - Device Internet Protocol (\"IP\") address\n")
    append(" - Device name\n")
    append(" - Operating system version\n")
    append(" - App configuration at the time of usage\n")
    append(" - Time and date of usage\n")
    append(" - Crash reports and diagnostic statistics\n")
    append("These logs are used only to improve app stability, performance, and user experience. We do not use this data to personally identify users.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Third-Party Services\n")
    }
    append("The App is supported by advertisements and may use third-party services that collect information used to identify you.\n\n")
    append("These services may include:\n")
    append(" - Google Play Services\n")
    append(" - AdMob\n")
    append(" - Firebase\n")
    append("Each of these services operates under its own privacy policy.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Third-Party Links\n")
    }
    append("Our Services may contain links to third-party websites. Access to and use of such linked websites is not governed by this Policy, but instead by the privacy policies of those third-party websites. We are not responsible for the content or privacy practices of such third-party services.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Contact Us\n")
    }
    append("If you have any questions about this Privacy Policy or the privacy practices of our Services, you can contact us through:\n")
    append(" - The support section within the App\n")
    append(" - The email address listed on the App’s Google Play Store page\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
        append("Changes to This Policy\n")
    }
    append("This Privacy Policy may be updated from time to time. Users are advised to review this page periodically for any changes. Continued use of the Service after changes are posted will signify acceptance of the updated Policy. These changes are effective immediately upon being posted on this page.\n")
}