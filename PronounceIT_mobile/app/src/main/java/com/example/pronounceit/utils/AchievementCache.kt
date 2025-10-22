package com.example.pronounceit.utils

import android.content.Context
import android.util.Log
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.AchievementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton cache manager for achievements data to prevent redundant API calls
 */
object AchievementCache {
    private var cachedAchievements: List<AchievementEntity>? = null
    private var cacheTimestamp: Long = 0
    private val cacheDurationMs = 5 * 60 * 1000L // 5 minutes
    private const val TAG = "AchievementCache"
    
    /**
     * Get achievements with caching - fetches from API only if cache is empty/expired
     * @param context Application context for API calls
     * @param forceRefresh If true, ignores cache and fetches fresh data
     * @return List of achievements or empty list if error
     */
    suspend fun getAchievements(context: Context, forceRefresh: Boolean = false): List<AchievementEntity> {
        return withContext(Dispatchers.IO) {
            // Check if we have valid cached data
            if (!forceRefresh && isCacheValid()) {
                Log.d(TAG, "Returning cached achievements (${cachedAchievements?.size} items)")
                return@withContext cachedAchievements ?: emptyList()
            }
            
            // Fetch fresh data from API
            try {
                Log.d(TAG, "Fetching achievements from API...")
                val response = RetrofitInstance.getApi(context).getAllAchievements()
                
                if (response.isSuccessful) {
                    val achievements = response.body() ?: emptyList()
                    
                    // Update cache
                    cachedAchievements = achievements
                    cacheTimestamp = System.currentTimeMillis()
                    
                    Log.d(TAG, "Successfully cached ${achievements.size} achievements")
                    return@withContext achievements
                } else {
                    Log.e(TAG, "API call failed: ${response.code()}")
                    // Return cached data if available, even if expired
                    return@withContext cachedAchievements ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching achievements from API", e)
                // Return cached data if available, even if expired
                return@withContext cachedAchievements ?: emptyList()
            }
        }
    }
    
    /**
     * Get a specific achievement by ID from cache
     * @param context Application context for API calls if cache miss
     * @param achievementId The ID of the achievement to find
     * @return The achievement entity or null if not found
     */
    suspend fun getAchievementById(context: Context, achievementId: Long): AchievementEntity? {
        val achievements = getAchievements(context)
        return achievements.find { it.id == achievementId }
    }
    
    /**
     * Clear the achievements cache
     */
    fun clearCache() {
        Log.d(TAG, "Clearing achievements cache")
        cachedAchievements = null
        cacheTimestamp = 0
    }
    
    /**
     * Force refresh the cache on next access
     */
    fun invalidateCache() {
        Log.d(TAG, "Invalidating achievements cache")
        cacheTimestamp = 0
    }
    
    /**
     * Check if cached data is still valid
     */
    private fun isCacheValid(): Boolean {
        if (cachedAchievements == null) return false
        
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - cacheTimestamp
        
        return cacheAge < cacheDurationMs
    }
    
    /**
     * Get cache status for debugging
     */
    fun getCacheStatus(): String {
        val size = cachedAchievements?.size ?: 0
        val age = System.currentTimeMillis() - cacheTimestamp
        val valid = isCacheValid()
        return "Cache: $size items, age: ${age}ms, valid: $valid"
    }
}