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
import com.example.pronounceit.network.models.RegisterRequest
import com.example.pronounceit.network.models.ScoreRecordEntity
import com.example.pronounceit.network.models.WordEntity
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @GET("/api/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Long,
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @POST("/api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ResponseBody>

    // Category Controller Endpoints
    @GET("/api/categories/{categoryId}")
    suspend fun getCategoryById(@Path("categoryId") categoryId: Long): Response<CategoryEntity>

    @GET("/api/categories")
    suspend fun getAllCategories(): Response<List<CategoryEntity>>

    @POST("/api/categories")
    suspend fun createCategory(@Body category: CategoryEntity): Response<CategoryEntity>

    @PUT("/api/categories/{categoryId}")
    suspend fun updateCategory(
        @Path("categoryId") categoryId: Long,
        @Body updatedCategory: CategoryEntity
    ): Response<CategoryEntity>

    @DELETE("/api/categories/{categoryId}")
    suspend fun deleteCategory(@Path("categoryId") categoryId: Long): Response<Void>

    // Class Controller Endpoints
    @GET("/api/classes/{classId}")
    suspend fun getClassById(@Path("classId") classId: Long): Response<ClassEntity>

    @GET("/api/classes")
    suspend fun getAllClasses(): Response<List<ClassEntity>>

    @POST("/api/classes")
    suspend fun createClass(@Body classEntity: ClassEntity): Response<ClassEntity>

    @PUT("/api/classes/{classId}")
    suspend fun updateClass(
        @Path("classId") classId: Long,
        @Body updatedClass: ClassEntity
    ): Response<ClassEntity>

    @DELETE("/api/classes/{classId}")
    suspend fun deleteClass(@Path("classId") classId: Long): Response<Void>

    // Class Member Controller Endpoints
    @GET("/api/class-members/{memberId}")
    suspend fun getClassMemberById(@Path("memberId") memberId: Long): Response<ClassMemberEntity>

    @GET("/api/class-members")
    suspend fun getAllClassMembers(): Response<List<ClassMemberEntity>>

    @POST("/api/class-members")
    suspend fun createClassMember(@Body classMemberEntity: ClassMemberEntity): Response<ClassMemberEntity>

    @PUT("/api/class-members/{memberId}")
    suspend fun updateClassMember(
        @Path("memberId") memberId: Long,
        @Body updatedClassMember: ClassMemberEntity
    ): Response<ClassMemberEntity>

    @DELETE("/api/class-members/{memberId}")
    suspend fun deleteClassMember(@Path("memberId") memberId: Long): Response<Void>

    // Lesson Controller Endpoints
    @GET("/api/lessons/{lessonId}")
    suspend fun getLessonById(@Path("lessonId") lessonId: Long): Response<LessonEntity>

    @GET("/api/lessons")
    suspend fun getAllLessons(): Response<List<LessonEntity>>

    @POST("/api/lessons")
    suspend fun createLesson(@Body lesson: LessonEntity): Response<LessonEntity>

    @PUT("/api/lessons/{lessonId}")
    suspend fun updateLesson(
        @Path("lessonId") lessonId: Long,
        @Body updatedLesson: LessonEntity
    ): Response<LessonEntity>

    @DELETE("/api/lessons/{lessonId}")
    suspend fun deleteLesson(@Path("lessonId") lessonId: Long): Response<Void>

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
    suspend fun createPronounciationAttempt(@Body pronounciationAttempt: PronounciationAttemptEntity): Response<PronounciationAttemptEntity>

    @PUT("/api/pronounciation-attempts/{attemptId}")
    suspend fun updatePronounciationAttempt(
        @Path("attemptId") attemptId: Long,
        @Body updatedPronounciationAttempt: PronounciationAttemptEntity
    ): Response<PronounciationAttemptEntity>

    @DELETE("/api/pronounciation-attempts/{attemptId}")
    suspend fun deletePronounciationAttempt(@Path("attemptId") attemptId: Long): Response<Void>

    // Score Record Controller Endpoints
    @GET("/api/score-records/{scoreId}")
    suspend fun getScoreRecordById(@Path("scoreId") scoreId: Long): Response<ScoreRecordEntity>

    @GET("/api/score-records")
    suspend fun getAllScoreRecords(): Response<List<ScoreRecordEntity>>

    @POST("/api/score-records")
    suspend fun createScoreRecord(@Body scoreRecord: ScoreRecordEntity): Response<ScoreRecordEntity>

    @PUT("/api/score-records/{scoreId}")
    suspend fun updateScoreRecord(
        @Path("scoreId") scoreId: Long,
        @Body updatedScoreRecord: ScoreRecordEntity
    ): Response<ScoreRecordEntity>

    @DELETE("/api/score-records/{scoreId}")
    suspend fun deleteScoreRecord(@Path("scoreId") scoreId: Long): Response<Void>

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

    @DELETE("/api/words/{wordId}")
    suspend fun deleteWord(@Path("wordId") wordId: Long): Response<Void>
}