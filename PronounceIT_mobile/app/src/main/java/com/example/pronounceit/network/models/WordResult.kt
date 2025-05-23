package com.example.pronounceit.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordResult(
    val word: String,
    val correct: Boolean,
    val attempts: Int
) : Parcelable