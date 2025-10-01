package com.example.pronounceit.network

import com.example.pronounceit.network.models.CategoryEntity
import com.example.pronounceit.network.models.ClassEntity
import com.example.pronounceit.network.models.ClassMemberEntity
import com.example.pronounceit.network.models.LessonEntity
import com.example.pronounceit.network.models.UserResponse
import com.example.pronounceit.network.models.LoginRequest
import com.example.pronounceit.network.models.LoginResponse
import com.example.pronounceit.network.models.ProgressTrackerEntity
import com.example.pronounceit.network.models.PronounciationAttemptEntity
import com.example.pronounceit.network.models.PronounciationAttemptPostDTO
import com.example.pronounceit.network.models.PronunciationCheckResponse
import com.example.pronounceit.network.models.RegisterRequest
import com.example.pronounceit.network.models.ScoreRecordDTO
import com.example.pronounceit.network.models.ScoreRecordEntity
import com.example.pronounceit.network.models.UpdateUserRequest
import com.example.pronounceit.network.models.WordEntity
import com.example.pronounceit.network.models.AchievementEntity
import com.example.pronounceit.network.models.StreakDTO
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @GET("/api/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Long,
        @Header("Authorization") token: String
    ): Response<com.example.pronounceit.network.models.UserResponseWithPoints>

    @POST("/api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ResponseBody>

    // Add this method to your AuthApi interface
    @PUT("/api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    // Points management endpoints
    @PATCH("/api/users/{userId}/points/add")
    suspend fun addPointsToUser(
        @Path("userId") userId: Long,
        @Body request: Map<String, Int>
    ): Response<UserResponse>

    @PATCH("/api/users/{userId}/points/subtract")
    suspend fun subtractPointsFromUser(
        @Path("userId") userId: Long,
        @Body request: Map<String, Int>
    ): Response<UserResponse>

    @PATCH("/api/users/{userId}/points/set")
    suspend fun setUserPoints(
        @Path("userId") userId: Long,
        @Body request: Map<String, Int>
    ): Response<UserResponse>

    @GET("/api/categories")
    suspend fun getAllCategories(): Response<List<CategoryEntity>>

    // Lesson Controller Endpoints
    @GET("/api/lessons/{lessonId}")
    suspend fun getLessonById(@Path("lessonId") lessonId: Long): Response<LessonEntity>

    @GET("/api/lessons")
    suspend fun getAllLessons(): Response<List<LessonEntity>>


    // Progress Tracker Controller Endpoints
    @GET("/api/progress-trackers/{progressId}")
    suspend fun getProgressTrackerById(@Path("progressId") progressId: Long): Response<ProgressTrackerEntity>

    @GET("/api/progress-trackers")
    suspend fun getAllProgressTrackers(): Response<List<ProgressTrackerEntity>>

    @POST("/api/progress-trackers")
    suspend fun createProgressTracker(@Body progressTracker: ProgressTrackerEntity): Response<ProgressTrackerEntity>

    @PUT("/api/progress-trackers/{progressId}")
    suspend fun updateProgressTracker(
        @Path("progressId") progressId: Long,
        @Body updatedProgressTracker: ProgressTrackerEntity
    ): Response<ProgressTrackerEntity>

    @DELETE("/api/progress-trackers/{progressId}")
    suspend fun deleteProgressTracker(@Path("progressId") progressId: Long): Response<Void>

    // Pronunciation Attempt Controller Endpoints
    @GET("/api/pronounciation-attempts/{attemptId}")
    suspend fun getPronounciationAttemptById(@Path("attemptId") attemptId: Long): Response<PronounciationAttemptEntity>

    @GET("/api/pronounciation-attempts")
    suspend fun getAllPronounciationAttempts(): Response<List<PronounciationAttemptEntity>>

    @POST("/api/pronounciation-attempts")
    suspend fun createPronounciationAttempt(
        @Body attempt: PronounciationAttemptPostDTO
    ): Response<PronounciationAttemptEntity>

    @PUT("/api/pronounciation-attempts/{attemptId}")
    suspend fun updatePronounciationAttempt(
        @Path("attemptId") attemptId: Long,
        @Body updatedPronounciationAttempt: PronounciationAttemptEntity
    ): Response<PronounciationAttemptEntity>

    @DELETE("/api/pronounciation-attempts/{attemptId}")
    suspend fun deletePronounciationAttempt(@Path("attemptId") attemptId: Long): Response<Void>

    @GET("/api/pronounciation-attempts/session")
    suspend fun getAttemptsBySession(
        @Query("lessonId") lessonId: Long,
        @Query("sessionId") sessionId: String
    ): Response<List<PronounciationAttemptEntity>>

    // Score Record Controller Endpoints
    @GET("/api/score-records/{scoreId}")
    suspend fun getScoreRecordById(@Path("scoreId") scoreId: Long): Response<ScoreRecordEntity>

    @GET("/api/score-records")
    suspend fun getAllScoreRecords(): Response<List<ScoreRecordEntity>>

    @POST("api/score-records/save-session-score")
    suspend fun createScoreRecord(@Body scoreRecordDTO: ScoreRecordDTO): Response<ScoreRecordEntity>

    @GET("/api/achievements/active")  // Changed from "achievements/active"
    suspend fun getActiveAchievements(): Response<List<AchievementEntity>>

    @PUT("/api/score-records/{scoreId}")
    suspend fun updateScoreRecord(
        @Path("scoreId") scoreId: Long,
        @Body updatedScoreRecord: ScoreRecordEntity
    ): Response<ScoreRecordEntity>

    @DELETE("/api/score-records/{scoreId}")
    suspend fun deleteScoreRecord(@Path("scoreId") scoreId: Long): Response<Void>

    @GET("/api/score-records/by-session")
    suspend fun getScoreRecordBySession(
        @Query("lessonId") lessonId: Long,
        @Query("sessionId") sessionId: String
    ): Response<ScoreRecordEntity>

    @GET("/api/score-records/latest")
    suspend fun getLatestScoreRecord(
        @Query("userId") userId: Long,
        @Query("lessonId") lessonId: Long
    ): Response<ScoreRecordEntity>

    // Word Controller Endpoints
    @GET("/api/words/{wordId}")
    suspend fun getWordById(@Path("wordId") wordId: Long): Response<WordEntity>

    @GET("/api/words")
    suspend fun getAllWords(): Response<List<WordEntity>>

    @POST("/api/words")
    suspend fun createWord(@Body word: WordEntity): Response<WordEntity>

    @PUT("/api/words/{wordId}")
    suspend fun updateWord(
        @Path("wordId") wordId: Long,
        @Body updatedWord: WordEntity
    ): Response<WordEntity>

    @GET("/api/words/lesson/{lessonId}")  // New endpoint to get words by lesson ID
    suspend fun getWordsByLessonId(@Path("lessonId") lessonId: Long): Response<List<WordEntity>>

    @DELETE("/api/words/{wordId}")
    suspend fun deleteWord(@Path("wordId") wordId: Long): Response<Void>

    @GET("/api/categories/{categoryId}/lessons") // Added this endpoint
    suspend fun getLessonsByCategoryId(@Path("categoryId") categoryId: Long): Response<List<LessonEntity>>

    //TextToSpeech Controller
    @POST("/api/tts")
    suspend fun synthesizeText(@Body text: String): Response<ResponseBody>

    @Multipart
    @POST("/api/words/{wordId}/check-pronunciation")
    suspend fun checkPronunciation(
        @Path("wordId") wordId: Long,
        @Part audio: MultipartBody.Part
    ): Response<PronunciationCheckResponse>

    // Add this endpoint for badge images

    @GET("api/achievements")
    suspend fun getAllAchievements(): Response<List<AchievementEntity>>

    @GET("/api/achievements/{achievementId}/badge")
    suspend fun getAchievementBadge(@Path("achievementId") achievementId: Long): Response<ResponseBody>

    @GET("api/streaks/{userId}")
    suspend fun getStreak(@Path("userId") userId: Long): Response<StreakDTO>

    @POST("api/streaks/{userId}")
    suspend fun createStreak(@Path("userId") userId: Long): Response<StreakDTO>

    @PUT("api/streaks/{userId}")
    suspend fun updateStreak(@Path("userId") userId: Long): Response<StreakDTO>

    // Mark user activity for streak (optional date query in ISO format YYYY-MM-DD)
    @POST("/api/streaks/{userId}/activity")
    suspend fun markStreakActivity(@Path("userId") userId: Long, @Query("date") date: String? = null): Response<StreakDTO>
}