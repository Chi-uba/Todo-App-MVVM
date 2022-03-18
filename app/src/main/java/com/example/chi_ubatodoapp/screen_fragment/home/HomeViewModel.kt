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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class HomeViewModel @ViewModelInject constructor(
private val todoItemDao : TodoItemDao,
private val preferencesManager: PreferencesManager,
@Assisted private val state: SavedStateHandle
) : ViewModel() {

    //holds the current typed search query
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow


    private val todoItemEventChannel = Channel<TodoItemEvent>()
    val todoItemEvent = todoItemEventChannel.receiveAsFlow()


    // used multiple flows
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        todoItemDao.getTodoItem(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }
    val todoItem = tasksFlow.asLiveData()
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }
    fun onTodoItemSelected(todoItem: TodoItem) = viewModelScope.launch{
        todoItemEventChannel.send(TodoItemEvent.NavigateToEditTaskScreen(todoItem))
    }

    fun onTodoItemCheckedChanged(todoItem: TodoItem, isChecked: Boolean) = viewModelScope.launch {
        todoItemDao.update(todoItem.copy(isComplete = isChecked))
    }

    fun onTodoItemSwiped(todoItem: TodoItem) = viewModelScope.launch {
        todoItemDao.delete(todoItem)
        todoItemEventChannel.send(TodoItemEvent.ShowUndoDeleteTaskMessage(todoItem))
    }

    fun onUndoDeleteClick(todoItem: TodoItem) = viewModelScope.launch {
        todoItemDao.insert(todoItem)
    }
    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTodoItemSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTodoItemSavedConfirmationMessage("Task updated")
        }
    }
    private fun showTodoItemSavedConfirmationMessage(text: String) = viewModelScope.launch {
        todoItemEventChannel.send(TodoItemEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onAddNewTodoItemClick() = viewModelScope.launch {
        todoItemEventChannel.send(TodoItemEvent.NavigateToAddTaskScreen)
    }

    sealed class TodoItemEvent {
        object NavigateToAddTaskScreen : TodoItemEvent()
        data class NavigateToEditTaskScreen(val todoItem: TodoItem) : TodoItemEvent()
        data class ShowUndoDeleteTaskMessage(val todoItem: TodoItem) : TodoItemEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TodoItemEvent()
    }

}
