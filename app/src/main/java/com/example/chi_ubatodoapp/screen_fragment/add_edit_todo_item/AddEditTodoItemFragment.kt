package com.example.chi_ubatodoapp.screen_fragment.add_edit_todo_item

import androidx.fragment.app.Fragment
import com.example.chi_ubatodoapp.R


import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chi_ubatodoapp.databinding.FragmentAddEditTodoItemBinding
import com.example.chi_ubatodoapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTodoItemFragment : Fragment(R.layout.fragment_add_edit_todo_item) {

    private val viewModel: AddEditTodoItemViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTodoItemBinding.bind(view)

        binding.apply {
            editTextTodoItemName.setText(viewModel.todoItemName)
            checkboxTodoItemImportance.isChecked = viewModel.todoItemImportance
            checkboxTodoItemImportance.jumpDrawablesToCurrentState()
            textviewDateCreated.isVisible = viewModel.todoItem != null
            textviewDateCreated.text = "Created: ${viewModel.todoItem?.createdDateFormatted}"

            editTextTodoItemName.addTextChangedListener {
                viewModel.todoItemName = it.toString()
            }

            checkboxTodoItemImportance.setOnCheckedChangeListener { _, isChecked ->
                viewModel.todoItemImportance = isChecked
            }

            fabSaveTodoItem.setOnClickListener {
                viewModel.onSaveClick()
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTodoItemEvent.collect { event ->
                when (event) {
                    is AddEditTodoItemViewModel.AddEditTodoItemEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTodoItemViewModel.AddEditTodoItemEvent.NavigateBackWithResult -> {
                        binding.editTextTodoItemName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }         }
    }
}

