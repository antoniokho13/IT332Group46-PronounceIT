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
    private var categoriesLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply zoom in/out animation to the category title image
        val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.category_zoom)
        binding.categoryTitle.startAnimation(zoomAnimation)

        fetchCategories()
    }

    private fun fetchCategories(forceRefresh: Boolean = false) {
        // Skip if categories already loaded and we're not forcing a refresh
        if (categoriesLoaded && !forceRefresh) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("CategoryActivity", "Attempting to fetch categories from: ${RetrofitInstance.getBaseUrl()}/api/categories")
                val response = RetrofitInstance.getApi(this@CategoryActivity).getAllCategories()
                
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    Log.d("CategoryActivity", "Successfully loaded ${categories.size} categories")
                    withContext(Dispatchers.Main) {
                        setupRecyclerView(categories)
                        // Mark categories as loaded to prevent unnecessary API calls
                        categoriesLoaded = true
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("CategoryActivity", "Failed to load categories: $errorBody (${response.code()})")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategoryActivity, "Failed to load categories: ${response.code()}", Toast.LENGTH_SHORT).show()
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