package com.example.pronounceit

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.pronounceit.databinding.ActivityInstructionsBinding
import android.widget.ImageView
import android.widget.LinearLayout

class InstructionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstructionsBinding
    private lateinit var instructionsAdapter: InstructionsAdapter
    private var lessonId: Long = -1L
    private var categoryId: Long = -1L
    private var buttonClickSound: MediaPlayer? = null

    // Instruction data using actual tutorial images
    private val instructionData = listOf(
        InstructionSlide("Listen to the word by tapping the play button.", R.drawable.playimg1),
        InstructionSlide("Press the microphone to record your pronunciation.", R.drawable.microphone),
        InstructionSlide("Speak clearly into the microphone, then click the button when finished.", R.drawable.mic_recording_animation),
        InstructionSlide("If correct, you'll see a this message.", R.drawable.correct),
        InstructionSlide("If not, try again!", R.drawable.wrong),
        InstructionSlide("Click the Next button to move to the next word", R.drawable.nextimg1),
        InstructionSlide("Complete all words to finish the lesson!", R.drawable.pronounce_logo)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize button click sound
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click)

        // Get lesson and category IDs from intent
        lessonId = intent.getLongExtra("lessonId", -1L)
        categoryId = intent.getLongExtra("categoryId", -1L)

        if (lessonId == -1L) {
            finish()
            return
        }

        setupViewPager()
        setupNavigationButtons()
        setupSkipButton()
        setupBackButton()
        createPageIndicators()
    }

    private fun playButtonSound() {
        buttonClickSound?.start()
    }

    private fun setupViewPager() {
        instructionsAdapter = InstructionsAdapter(this, instructionData)
        binding.instructionsViewPager.adapter = instructionsAdapter

        // Set up page change callback
        binding.instructionsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationButtons(position)
                updatePageIndicators(position)
            }
        })
    }

    private fun setupNavigationButtons() {
        binding.previousButton.setOnClickListener {
            playButtonSound()
            val currentItem = binding.instructionsViewPager.currentItem
            if (currentItem > 0) {
                binding.instructionsViewPager.currentItem = currentItem - 1
            }
        }

        binding.nextButton.setOnClickListener {
            playButtonSound()
            val currentItem = binding.instructionsViewPager.currentItem
            if (currentItem < instructionData.size - 1) {
                binding.instructionsViewPager.currentItem = currentItem + 1
            }
        }

        binding.startGameButton.setOnClickListener {
            playButtonSound()
            startWordActivity()
        }
    }

    private fun setupSkipButton() {
        binding.skipButton.setOnClickListener {
            playButtonSound()
            startWordActivity()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            playButtonSound()
            onBackPressed()
        }
    }

    private fun createPageIndicators() {
        binding.pageIndicatorContainer.removeAllViews()

        for (i in instructionData.indices) {
            val indicator = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(24, 24)
            layoutParams.setMargins(8, 0, 8, 0)
            indicator.layoutParams = layoutParams

            // Set indicator drawable (you can customize these)
            indicator.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.page_indicator_inactive)
            )

            binding.pageIndicatorContainer.addView(indicator)
        }

        // Set first indicator as active
        updatePageIndicators(0)
    }

    private fun updatePageIndicators(position: Int) {
        for (i in 0 until binding.pageIndicatorContainer.childCount) {
            val indicator = binding.pageIndicatorContainer.getChildAt(i) as ImageView
            if (i == position) {
                indicator.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.page_indicator_active)
                )
            } else {
                indicator.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.page_indicator_inactive)
                )
            }
        }
    }

    private fun updateNavigationButtons(position: Int) {
        // Update previous button
        binding.previousButton.isEnabled = position > 0
        binding.previousButton.alpha = if (position > 0) 1.0f else 0.5f

        // Update next button and start game button
        if (position == instructionData.size - 1) {
            // Last slide - show start game button, hide next button
            binding.nextButton.visibility = View.GONE
            binding.startGameButton.visibility = View.VISIBLE
        } else {
            // Not last slide - show next button, hide start game button
            binding.nextButton.visibility = View.VISIBLE
            binding.startGameButton.visibility = View.GONE
        }
    }

    private fun startWordActivity() {
        val intent = Intent(this, WordActivity::class.java).apply {
            putExtra("lessonId", lessonId)
            putExtra("categoryId", categoryId)
        }
        startActivity(intent)
        finish() // Close instructions activity
    }

    override fun onDestroy() {
        super.onDestroy()
        buttonClickSound?.release()
        buttonClickSound = null
    }

    // Data class for instruction slides
    data class InstructionSlide(
        val text: String,
        val imageResId: Int
    )

    // ViewPager2 Adapter for instruction fragments
    private class InstructionsAdapter(
        fragmentActivity: FragmentActivity,
        private val instructionData: List<InstructionSlide>
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = instructionData.size

        override fun createFragment(position: Int): Fragment {
            return InstructionSlideFragment.newInstance(
                instructionData[position].text,
                instructionData[position].imageResId
            )
        }
    }
}