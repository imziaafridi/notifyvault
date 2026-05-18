package com.ziaafridi.notifyvault

import android.content.Context
import android.content.SharedPreferences

class OnboardingPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "onboarding_prefs", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
    
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    fun setOnboardingCompleted() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }
    
    fun resetOnboarding() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, false)
            .apply()
    }
    
    // For testing: Call this function to reset onboarding during development
    // Example: OnboardingPreferences(context).resetOnboarding()
}