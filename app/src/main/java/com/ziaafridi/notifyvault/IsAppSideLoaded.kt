//package com.ziaafridi.notifyvault
//
//import android.content.Context
//import android.os.Build
//
//fun isAppSideloaded(context: Context): Boolean {
//    // Restricted settings only apply to Android 13+ (API 33)
//    if (Build.VERSION.SDK_INT < 33) return false
//
//    return try {
//        val installSourceInfo = context.packageManager.getInstallSourceInfo(context.packageName)
//        val installer = installSourceInfo.installingPackageName
//
//        // List of official stores that DO NOT trigger Restricted Settings
//        val officialStores = listOf(
//            "com.android.vending",             // Google Play Store
//            "com.sec.android.app.samsungapps", // Samsung Galaxy Store
//            "com.amazon.venezia",              // Amazon Appstore
//            "com.heytap.market",               // Oppo App Market
//            "com.huawei.appmarket"             // Huawei AppGallery
//        )
//
//        // If installer is null, it's usually ADB (Android Studio).
//        // If it's not in our list (e.g., com.android.chrome), it's sideloaded.
//        installer != null && installer !in officialStores
//    } catch (e: Exception) {
//        false
//    }
//}