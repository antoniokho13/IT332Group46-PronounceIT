package com.example.pronounceit

import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchCategories()
    }

    private fun fetchCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getAllCategories()
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
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoryRecyclerView.adapter = CategoryAdapter(this, categories)
    }
}
