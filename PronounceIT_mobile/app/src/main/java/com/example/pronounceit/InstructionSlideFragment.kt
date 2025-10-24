package com.example.pronounceit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pronounceit.databinding.ItemInstructionSlideBinding

class InstructionSlideFragment : Fragment() {

    private var _binding: ItemInstructionSlideBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TEXT = "text"
        private const val ARG_IMAGE_RES_ID = "image_res_id"

        fun newInstance(text: String, imageResId: Int): InstructionSlideFragment {
            val fragment = InstructionSlideFragment()
            val args = Bundle().apply {
                putString(ARG_TEXT, text)
                putInt(ARG_IMAGE_RES_ID, imageResId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemInstructionSlideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            val text = args.getString(ARG_TEXT) ?: ""
            val imageResId = args.getInt(ARG_IMAGE_RES_ID)

            binding.instructionText.text = text
            
            // Set the instruction image
            // You can replace this with Glide if you plan to use URLs later
            binding.instructionImage.setImageResource(imageResId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}