package com.example.pronounceit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pronounceit.R
import com.example.pronounceit.network.models.LessonEntity


// Lesson Adapter
class LessonAdapter(private val context: Context, private val lessons: List<LessonEntity>) :
    RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size

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
