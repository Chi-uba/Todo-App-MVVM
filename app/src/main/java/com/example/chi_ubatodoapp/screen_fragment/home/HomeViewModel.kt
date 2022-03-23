package com.example.chi_ubatodoapp.screen_fragment.home

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.chi_ubatodoapp.ADD_TASK_RESULT_OK
import com.example.chi_ubatodoapp.EDIT_TASK_RESULT_OK
import com.example.chi_ubatodoapp.data.PreferencesManager
import com.example.chi_ubatodoapp.data.SortOrder
import com.example.chi_ubatodoapp.data.TodoItem
import com.example.chi_ubatodoapp.data.TodoItemDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class HomeViewModel @ViewModelInject constructor(
    // gets an instance of todoItem DAO
private val todoItemDao : TodoItemDao,
private val preferencesManager: PreferencesManager,
@Assisted private val state: SavedStateHandle
) : ViewModel() {

    //retrieves previous search query from SavedInstanceState
    val searchQuery = state.getLiveData("searchQuery", "")

    // gets an instance of data stored in datastore from preference manager
    val preferencesFlow = preferencesManager.preferencesFlow

    // passes the TodoItemEvent to the HomeFragment
    private val todoItemEventChannel = Channel<TodoItemEvent>()

    // casts the channel to a flow
    val todoItemEvent = todoItemEventChannel.receiveAsFlow()


    // used multiple flows to edit the list to be displayed based on multiple variables dependent on user's preference
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        todoItemDao.getTodoItem(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    // casts the flow of data from database to a liveData, making it observable to the homeFragment
    val todoItem = tasksFlow.asLiveData()

    // calls a method in preferencesManager to update value of sortOrder when user makes a change
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    // calls a method in preferencesManager to update value of hideCompleted when user makes a change
    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    // sends NavigateToEditTaskScreen event to the channel
    fun onTodoItemSelected(todoItem: TodoItem) = viewModelScope.launch{
        todoItemEventChannel.send(TodoItemEvent.NavigateToEditTodoItemScreen(todoItem))
    }

    // updates database with the new edited version of todoItem
    fun onTodoItemCheckedChanged(todoItem: TodoItem, isChecked: Boolean) = viewModelScope.launch {
        todoItemDao.update(todoItem.copy(isComplete = isChecked))
    }

    // sends ShowUndoDeleteTodoItem event to the channel
    fun onTodoItemSwiped(todoItem: TodoItem) = viewModelScope.launch {
        todoItemDao.delete(todoItem)
        todoItemEventChannel.send(TodoItemEvent.ShowUndoDeleteTodoItemMessage(todoItem))
    }

    // re-inserts an item that was deleted within few seconds
    fun onUndoDeleteClick(todoItem: TodoItem) = viewModelScope.launch {
        todoItemDao.insert(todoItem)
    }

    // logic that decides which string to be displayed on the snackBar after a task is added or updated
    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTodoItemSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTodoItemSavedConfirmationMessage("Task updated")
        }
    }

    // sends an event that triggers showing a snackbar with a text to the HomeFragment through the channel when a task is saved
    private fun showTodoItemSavedConfirmationMessage(text: String) = viewModelScope.launch {
        todoItemEventChannel.send(TodoItemEvent.ShowTaskSavedConfirmationMessage(text))
    }

    // sends an event to the HomeFragment through a TodoItemEvent channel telling it to navigate to AddEditTodoItemScreen
    fun onAddNewTodoItemClick() = viewModelScope.launch {
        todoItemEventChannel.send(TodoItemEvent.NavigateToAddTodoItemScreen)
    }

    // contains different events to be passed to the HomeFragments
    sealed class TodoItemEvent {
        object NavigateToAddTodoItemScreen : TodoItemEvent()
        data class NavigateToEditTodoItemScreen(val todoItem: TodoItem) : TodoItemEvent()
        data class ShowUndoDeleteTodoItemMessage(val todoItem: TodoItem) : TodoItemEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TodoItemEvent()
    }

}
