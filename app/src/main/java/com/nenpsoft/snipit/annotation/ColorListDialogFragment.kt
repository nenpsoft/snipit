package com.nenpsoft.snipit.annotation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nenpsoft.snipit.databinding.FragmentColorListDialogBinding

interface ColorChangeListener {
    fun colorChanged(newColor: Color)
}

class ColorListDialogFragment(val colorChangeListener: ColorChangeListener) :
    BottomSheetDialogFragment() {
    private lateinit var binding: FragmentColorListDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentColorListDialogBinding.inflate(inflater, container, false)
        binding.colorListImageView.setColorFilter(Colors.current.value)

        Colors.supported.forEachIndexed { index, color ->
            val button = binding.colorListGridLayout.getChildAt(index) as? ImageButton
            button?.setColorFilter(color.value)
            button?.tag = color.value
            button?.setOnClickListener {
                binding.colorListImageView.setColorFilter((it.tag as? Int) ?: 0)
                colorChangeListener.colorChanged(color)
            }
        }

        return binding.root
    }
}