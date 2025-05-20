package com.example.pronounceit.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pronounceit.LessonActivity
import com.example.pronounceit.R
import com.example.pronounceit.databinding.ItemCategoryBinding
import com.example.pronounceit.network.models.CategoryEntity

class CategoryAdapter(
    private val context: Context,
    private val categoryList: List<CategoryEntity>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var buttonSound: MediaPlayer? = null

    init {
        // Initialize button sound
        buttonSound = MediaPlayer.create(context, R.raw.button_click)
    }

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        // binding.root is the actual View
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Inflate the layout using ItemCategoryBinding
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.categoryName.text = category.name
        holder.binding.root.setOnClickListener {
            playButtonSound()
            val intent = Intent(context, LessonActivity::class.java)
            intent.putExtra("categoryId", category.categoryId) // Pass the ID, not just the name
            context.startActivity(intent)
        }
    }

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
}