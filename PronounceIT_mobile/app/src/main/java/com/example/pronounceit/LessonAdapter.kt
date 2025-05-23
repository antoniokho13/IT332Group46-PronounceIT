package com.example.pronounceit.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pronounceit.R
import com.example.pronounceit.network.models.LessonEntity

class LessonAdapter(private val context: Context, private val lessons: List<LessonEntity>) :
    RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    private var buttonSound: MediaPlayer? = null
    var onItemClick: ((LessonEntity) -> Unit)? = null // Add this property

    init {
        // Initialize button sound
        buttonSound = MediaPlayer.create(context, R.raw.button_click)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)

        if (lesson.locked) {
            holder.itemView.alpha = 0.5f
            holder.itemView.isClickable = false
            holder.itemView.setOnClickListener {
                // Optionally show a toast
                Toast.makeText(context, "Complete previous lessons to unlock.", Toast.LENGTH_SHORT).show()
            }
        } else {
            holder.itemView.alpha = 1.0f
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener {
                playButtonSound()
                onItemClick?.invoke(lesson)
            }
        }
    }

    override fun getItemCount(): Int = lessons.size

    private fun playButtonSound() {
        if (buttonSound?.isPlaying == true) {
            buttonSound?.stop()
            buttonSound?.release()
            buttonSound = MediaPlayer.create(context, R.raw.button_click)
        }
        buttonSound?.start()
    }

    fun releaseResources() {
        buttonSound?.release()
        buttonSound = null
    }

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lessonNameTextView: TextView = itemView.findViewById(R.id.lessonNameTextView)
        private val lessonDescriptionTextView: TextView = itemView.findViewById(R.id.lessonDescriptionTextView)
        private val lessonCategoryTextView: TextView = itemView.findViewById(R.id.lessonCategoryTextView)

        fun bind(lesson: LessonEntity) {
            lessonNameTextView.text = lesson.name
            lessonDescriptionTextView.text = lesson.focus
            lessonCategoryTextView.text = lesson.category.name
        }
    }
}
