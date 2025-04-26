package com.example.pronounceit.network

import com.example.pronounceit.network.models.UserResponse
import com.example.pronounceit.network.models.LoginRequest
import com.example.pronounceit.network.models.LoginResponse
import com.example.pronounceit.network.models.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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
}