package com.nenpsoft.snipit.annotation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nenpsoft.snipit.databinding.FragmentTextInputDialogBinding

interface TextChangeListener {
    fun textChanged(text: String)
}

class TextInputDialogFragment(
    private val textChangeListener: TextChangeListener,
    private var textValue: String = ""
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentTextInputDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTextInputDialogBinding.inflate(inflater, container, false)
        binding.textInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                textValue = s.toString()
                textChangeListener.textChanged(textValue)
            }
        })

        binding.textInput.setOnFocusChangeListener { v, hasFocus ->
            (v as? TextView)?.let {
                if (hasFocus) {
                    it.text = textValue
                    requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                } else {
                    it.hint = textValue
                }
            }
        }
        binding.textInput.setOnEditorActionListener { _, a, _ ->
            if (a == EditorInfo.IME_ACTION_DONE) {
                dismiss()
            }
            return@setOnEditorActionListener false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.textInput.hint = textValue
        binding.textInput.requestFocus()
        super.onViewCreated(view, savedInstanceState)
    }
}