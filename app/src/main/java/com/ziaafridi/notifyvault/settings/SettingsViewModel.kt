package com.ziaafridi.notifyvault.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ziaafridi.notifyvault.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    fun clearAllData(onFinished: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.clearAllTables()
            }
            onFinished()
        }
    }
}