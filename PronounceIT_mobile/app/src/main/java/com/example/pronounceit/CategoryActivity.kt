package com.example.pronounceit

import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pronounceit.adapters.CategoryAdapter
import com.example.pronounceit.databinding.ActivityCategoryBinding
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply zoom in/out animation to the category title image
        val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.category_zoom)
        binding.categoryTitle.startAnimation(zoomAnimation)

        fetchCategories()
    }

    private fun fetchCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.getApi(this@CategoryActivity).getAllCategories()
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        setupRecyclerView(categories)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategoryActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CategoryActivity", "Error fetching categories", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView(categories: List<CategoryEntity>) {
        categoryAdapter = CategoryAdapter(this, categories)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoryRecyclerView.adapter = categoryAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::categoryAdapter.isInitialized) {
            categoryAdapter.releaseResources()
        }
    }
}