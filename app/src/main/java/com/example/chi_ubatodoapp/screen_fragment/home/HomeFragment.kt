package com.example.chi_ubatodoapp.screen_fragment.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chi_ubatodoapp.R
import com.example.chi_ubatodoapp.data.SortOrder
import com.example.chi_ubatodoapp.data.TodoItem
import com.example.chi_ubatodoapp.databinding.FragmentHomeBinding
import com.example.chi_ubatodoapp.util.exhaustive
import com.example.chi_ubatodoapp.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
//import com.example.chi_ubatodoapp.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),HomeAdapter.OnItemClickListener {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var searchView : SearchView

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        val homeAdapter = HomeAdapter(this)

        binding.apply {
            recyclerViewTodoItems.apply {
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = homeAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTodoItemSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTodoItems)

            fabCreateTodoItem.setOnClickListener {
                viewModel.onAddNewTodoItemClick()
            }
        }
            setFragmentResultListener("add_edit_request") { _, bundle ->
                val result = bundle.getInt("add_edit_result")
                viewModel.onAddEditResult(result)
            }

        viewModel.todoItem.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.todoItemEvent.collect { event ->
                when (event) {
                    is HomeViewModel.TodoItemEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.todoItem)
                            }.show()
                    }
                    is HomeViewModel.TodoItemEvent.NavigateToAddTaskScreen -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddEditTodoItemFragment(null,"New Task")
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TodoItemEvent.NavigateToEditTaskScreen -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddEditTodoItemFragment(event.todoItem,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TodoItemEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg,Snackbar.LENGTH_LONG).show()
                    }

                }.exhaustive
            }
        }
        setHasOptionsMenu(true)

    }

    override fun onItemClick(todoItem: TodoItem) {
        viewModel.onTodoItemSelected(todoItem)
    }

    override fun onCheckBoxClick(todoItem: TodoItem, isChecked: Boolean) {
        viewModel.onTodoItemCheckedChanged(todoItem, isChecked)
    }

    // this inflates the option menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_todo_item, menu)

        val searchItem = menu.findItem(R.id.ic_search)
         searchView = searchItem.actionView as SearchView

        val savedQuery = viewModel.searchQuery.value
        if (savedQuery != null && savedQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(savedQuery, false)
        }
        // updates the search view with latest typed in search keyword
        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }
// defines the necessary action whem an item in the menu layout is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            // re-arranges the list of todoItems according to name alphabetically
            R.id.sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            // re-arranges the list of todoItems according to date created
            R.id.sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            // hides completed tasks
            R.id.hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
    }
