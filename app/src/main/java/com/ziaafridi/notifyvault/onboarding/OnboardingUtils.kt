package com.ziaafridi.notifyvault.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Device detection functions
fun isXiaomiDevice(): Boolean = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
fun isHuaweiDevice(): Boolean = Build.MANUFACTURER.equals("Huawei", ignoreCase = true) || Build.MANUFACTURER.equals("Honor", ignoreCase = true)
fun isOppoDevice(): Boolean = Build.MANUFACTURER.equals("Oppo", ignoreCase = true) || Build.MANUFACTURER.equals("OnePlus", ignoreCase = true)
fun isVivoDevice(): Boolean = Build.MANUFACTURER.equals("Vivo", ignoreCase = true)
fun isSamsungDevice(): Boolean = Build.MANUFACTURER.equals("Samsung", ignoreCase = true)

fun openNotificationListenerSettings(context: Context) {
    try {
        // Try to open notification listener settings directly
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            // Fallback to general notification listener settings
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            context.startActivity(intent)
        } catch (e2: Exception) {
            // Final fallback to app notification settings
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    }
}

fun openAutoStartSettings(context: Context) {
    try {
        when {
            isXiaomiDevice() -> {
                // Try multiple Xiaomi intents for better compatibility
                val intents = listOf(
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    },
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.xiaomi.mipermission.autostart",
                            "com.xiaomi.mipermission.autostart.AutoStartManagementActivity"
                        )
                    },
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity"
                        )
                        putExtra("extra_pkgname", context.packageName)
                    }
                )
                
                var success = false
                for (intent in intents) {
                    try {
                        context.startActivity(intent)
                        success = true
                        break
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                if (!success) throw Exception("No Xiaomi intent worked")
            }
            isHuaweiDevice() -> {
                val intents = listOf(
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        )
                    },
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                        )
                    }
                )
                
                var success = false
                for (intent in intents) {
                    try {
                        context.startActivity(intent)
                        success = true
                        break
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                if (!success) throw Exception("No Huawei intent worked")
            }
            isOppoDevice() -> {
                val intents = listOf(
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    },
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.oppo.safe",
                            "com.oppo.safe.permission.startup.StartupAppListActivity"
                        )
                    }
                )
                
                var success = false
                for (intent in intents) {
                    try {
                        context.startActivity(intent)
                        success = true
                        break
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                if (!success) throw Exception("No Oppo intent worked")
            }
            isVivoDevice() -> {
                val intents = listOf(
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    },
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                        )
                    }
                )
                
                var success = false
                for (intent in intents) {
                    try {
                        context.startActivity(intent)
                        success = true
                        break
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                if (!success) throw Exception("No Vivo intent worked")
            }
            isSamsungDevice() -> {
                val intents = listOf(
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    },
                    Intent().apply {
                        component = android.content.ComponentName(
                            "com.samsung.android.sm_cn",
                            "com.samsung.android.sm.ui.ram.AutoRunActivity"
                        )
                    },
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
                
                var success = false
                for (intent in intents) {
                    try {
                        context.startActivity(intent)
                        success = true
                        break
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                if (!success) throw Exception("No Samsung intent worked")
            }
            else -> {
                throw Exception("Unsupported device")
            }
        }
    } catch (e: Exception) {
        // Fallback to general app settings
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            // Final fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(intent)
        }
    }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return enabledListeners?.contains(context.packageName) == true
}

/**
 * Opens file manager directly to WhatsApp folder with multiple fallback strategies
 */
fun openWhatsAppFolderInFileManager(context: Context): Boolean {
    val whatsappPaths = listOf(
        // Primary WhatsApp paths (Android 11+)
        "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp",
        "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp%2FWhatsApp",
        
        // Legacy WhatsApp paths (Android 10 and below)
        "content://com.android.externalstorage.documents/tree/primary%3AWhatsApp",
        "content://com.android.externalstorage.documents/tree/primary%3AWhatsApp%2FMedia",
        
        // SD Card paths
        "content://com.android.externalstorage.documents/tree/primary%3Astorage%2Femulated%2F0%2FAndroid%2Fmedia%2Fwhatsapp",
        
        // Alternative storage paths
        "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fwhatsapp"
    )
    
    // Strategy 1: Try to open document tree with WhatsApp-specific paths
    for (path in whatsappPaths) {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    putExtra("android.provider.extra.INITIAL_URI", Uri.parse(path))
                }
                // Add flags to ensure the picker opens in the right location
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            
            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            continue
        }
    }
    
    // Strategy 2: Try to open specific file managers with WhatsApp folder
    val fileManagerIntents = listOf(
        // Google Files
        Intent().apply {
            setPackage("com.google.android.apps.nbu.files")
            action = Intent.ACTION_VIEW
            data = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp")
        },
        
        // Samsung My Files
        Intent().apply {
            setPackage("com.sec.android.app.myfiles")
            action = Intent.ACTION_VIEW
            data = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp")
        },
        
        // ES File Explorer
        Intent().apply {
            setPackage("com.estrongs.android.pop")
            action = Intent.ACTION_VIEW
            data = Uri.parse("file:///storage/emulated/0/Android/media/whatsapp")
        },
        
        // Xiaomi File Manager
        Intent().apply {
            setPackage("com.mi.android.globalFileexplorer")
            action = Intent.ACTION_VIEW
            data = Uri.parse("file:///storage/emulated/0/Android/media/whatsapp")
        }
    )
    
    for (intent in fileManagerIntents) {
        try {
            // Check if the app is installed before trying to launch
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            continue
        }
    }
    
    // Strategy 3: Try to open any available file manager
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "resource/folder"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            return true
        }
    } catch (e: Exception) {
        // Continue to final fallback
    }
    
    // Strategy 4: Fallback to generic document tree picker
    try {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            return true
        }
    } catch (e: Exception) {
        return false
    }
    
    return false
}

/**
 * Gets the most likely WhatsApp folder URI based on Android version and device
 */
fun getWhatsAppFolderUri(context: Context): Uri? {
    val possiblePaths = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11+ - Scoped Storage
        listOf(
            "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp",
            "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp%2FWhatsApp"
        )
    } else {
        // Android 10 and below
        listOf(
            "content://com.android.externalstorage.documents/tree/primary%3AWhatsApp",
            "content://com.android.externalstorage.documents/tree/primary%3AWhatsApp%2FMedia",
            "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fwhatsapp"
        )
    }
    
    return try {
        Uri.parse(possiblePaths.first())
    } catch (e: Exception) {
        null
    }
}

/**
 * Checks if WhatsApp is installed on the device
 * Checks for multiple WhatsApp variants including regular WhatsApp and WhatsApp Business
 */
fun isWhatsAppInstalled(context: Context): Boolean {
    val whatsappPackages = listOf(
        "whatsapp",           // Regular WhatsApp
        "whatsapp.w4b",       // WhatsApp Business
        "com.gbwhatsapp",         // GB WhatsApp (popular mod)
        "com.yowhatsapp",         // YO WhatsApp (popular mod)
        "com.fmwhatsapp"
    )
    
    return whatsappPackages.any { packageName ->
        try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Checks if media folder access has been granted
 */
fun hasMediaFolderAccess(context: Context): Boolean {
    val prefs = context.getSharedPreferences("media_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("whatsapp_folder_uri", null)
    return !uriString.isNullOrEmpty()
}

/**
 * Gets user-friendly instructions for finding WhatsApp folder based on Android version
 */
fun getWhatsAppFolderInstructions(context: Context): String {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            "Navigate to: Android → media → whatsapp"
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            "Navigate to: Android → media → whatsapp or WhatsApp folder in root"
        }
        else -> {
            "Navigate to: WhatsApp folder in device storage"
        }
    }
}

@Composable
fun NavigationDots(currentStep: Int, totalSteps: Int = 7) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (index == currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}