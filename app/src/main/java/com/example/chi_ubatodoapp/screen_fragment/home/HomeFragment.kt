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

    //gets an instance of the HomeViewModel
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var searchView : SearchView

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        val homeAdapter = HomeAdapter(this)

        // binds recyclerview properties to their corresponding objects
        binding.apply {
            recyclerViewTodoItems.apply {
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                // for optimization
                setHasFixedSize(true)
            }
            // a method that defines what happens when an item in recycler view is swiped left or right
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // defines the swiping directions
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
                // calls a viewModel method that deletes the swiped item from database
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = homeAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTodoItemSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTodoItems) // connects the ItemTouchHelper
            // method with recyclerView

            // detects a click on the UI btn and calls a method defined in the viewModel to navigate to the edit screen with a null parameter
            fabCreateTodoItem.setOnClickListener {
                viewModel.onAddNewTodoItemClick()
            }
        }

        // recieves data sent from AddEditTodoItem fragment and calls a method from HomeViewModel that defines what happens next
            setFragmentResultListener("add_edit_request") { _, bundle ->
                val result = bundle.getInt("add_edit_result")
                viewModel.onAddEditResult(result)
            }

        // observes the flow of todoItem list of livedata in the viewModel in order
        // to automatically update the UI when there's an updated list
        viewModel.todoItem.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
        }

        // recieves events from the HomeViewModel and defines what action to be carried out based on the event received.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.todoItemEvent.collect { event ->
                when (event) {
                    is HomeViewModel.TodoItemEvent.ShowUndoDeleteTodoItemMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.todoItem)
                            }.show()
                    }
                    is HomeViewModel.TodoItemEvent.NavigateToAddTodoItemScreen -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddEditTodoItemFragment(null,"New Task")
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TodoItemEvent.NavigateToEditTodoItemScreen -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddEditTodoItemFragment(event.todoItem,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TodoItemEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg,Snackbar.LENGTH_LONG).show()
                    }

                }.exhaustive
            }
        }
        // enable a menu bar at the top of the Fragment
        setHasOptionsMenu(true)
    }

    // calls a method in the HomeViewModel that defines what happens whan a todoItem is clicked
    override fun onItemClick(todoItem: TodoItem) {
        viewModel.onTodoItemSelected(todoItem)
    }

    // calls a method in the HomeViewModel that updates the state of the isComplete check box
    override fun onCheckBoxClick(todoItem: TodoItem, isChecked: Boolean) {
        viewModel.onTodoItemCheckedChanged(todoItem, isChecked)
    }

    // this inflates the option menu i.e converts the already defined xml layout to a useable object
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_todo_item, menu)

        // gets a reference to the search UI element in the menu
        val searchItem = menu.findItem(R.id.ic_search)
         searchView = searchItem.actionView as SearchView

        // gets an instance of HomeViewModel searchQuery
        val savedQuery = viewModel.searchQuery.value

        // checks if HomeViewModel searchQuery is not null and empty so as to expand the search view
        if (savedQuery != null && savedQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(savedQuery, false)
        }
        // updates the HomeviewModel searchQuery with latest typed in search keyword
        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted // used .first()
        // rather than .collect() because we want to read from the flow once when the menu is created
        }
    }
// calls a method from the home viewmodel depending on the menu UI element clicked whem an item in the menu layout is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            // calls a method from the home viewModel that re-arranges the list of todoItems according to name alphabetically
            R.id.sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            // calls a method from the home viewModel that re-arranges the list of todoItems according to date created
            R.id.sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            // calls a method from the home viewModel that hides completed tasks
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
        // removes listener to avoid sending empty string
        searchView.setOnQueryTextListener(null)
    }
    }
