package com.example.pronounceit.network.models

import com.google.gson.annotations.SerializedName

data class PronunciationCheckResponse(
    @SerializedName("correct") val correct: Boolean,
    @SerializedName("feedbackMessage") val feedbackMessage: String,
    @SerializedName("transcribedText") val transcribedText: String? // Can be null if transcription fails
)