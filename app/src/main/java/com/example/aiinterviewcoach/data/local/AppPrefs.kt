package com.example.aiinterviewcoach.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getTotalXp(): Int {
        return prefs.getInt("total_xp", 0)
    }

    fun addXp(amount: Int) {
        val current = getTotalXp()
        prefs.edit().putInt("total_xp", current + amount).apply()
        recordActivity()
    }

    fun getStreakCount(): Int {
        val lastActivity = prefs.getString("last_activity_date", "")
        if (lastActivity.isNullOrEmpty()) return 0

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        val streak = prefs.getInt("streak_count", 0)
        return if (lastActivity == todayStr || lastActivity == yesterdayStr) {
            streak
        } else {
            // Streak is broken, reset
            prefs.edit().putInt("streak_count", 0).apply()
            0
        }
    }

    fun recordActivity() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastActivity = prefs.getString("last_activity_date", "")
        val streak = prefs.getInt("streak_count", 0)

        if (lastActivity == todayStr) {
            // Already active today
            return
        }

        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        val newStreak = if (lastActivity == yesterdayStr) {
            streak + 1
        } else {
            1
        }

        prefs.edit()
            .putInt("streak_count", newStreak)
            .putString("last_activity_date", todayStr)
            .apply()
    }
}
