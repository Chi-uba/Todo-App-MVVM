package com.example.chi_ubatodoapp.screen_fragment.add_edit_todo_item


import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chi_ubatodoapp.ADD_TASK_RESULT_OK
import com.example.chi_ubatodoapp.EDIT_TASK_RESULT_OK
import com.example.chi_ubatodoapp.data.TodoItem
import com.example.chi_ubatodoapp.data.TodoItemDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTodoItemViewModel @ViewModelInject constructor(
    private val todoItemDao: TodoItemDao,
    @Assisted private val state: SavedStateHandle // stores data that needs to survive process death
) : ViewModel() {

    // retrieves todoItem passed during navigation from HomeScreen to EditTodoItemScreen
    val todoItem = state.get<TodoItem>("todoItem")

    // retrieves todoItem name from SavedInstanceState,retrieved todoItem or is null.
    // Then saves value back to the SavedInstanceState
    var todoItemName = state.get<String>("todoItemName") ?: todoItem?.name ?: ""
        set(value) {
            field = value
            state.set("todoItemName", value)
        }

    // retrieves todoItem isImportant state from SavedInstanceState,retrieved todoItem or is false.
    // Then saves value back to the SavedInstanceState
    var todoItemImportance = state.get<Boolean>("todoItemImportance") ?: todoItem?.isImportant ?: false
        set(value) {
            field = value
            state.set("todoItemImportance", value)
        }


    private val addEditTodoItemEventChannel = Channel<AddEditTodoItemEvent>()
    val addEditTodoItemEvent = addEditTodoItemEventChannel.receiveAsFlow()

    // does an input validation and inserts the new item to the database
    fun onSaveClick() {
        if (todoItemName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (todoItem != null) {
            val updatedTask = todoItem.copy(name = todoItemName, isImportant = todoItemImportance)
            updateTask(updatedTask)
        } else {
            val newTask = TodoItem(name = todoItemName, isImportant = todoItemImportance)
            createTask(newTask)
        }
    }

    // inserts newly created todoItem to the database
    private fun createTask(todoItem: TodoItem) = viewModelScope.launch {
        todoItemDao.insert(todoItem)
        addEditTodoItemEventChannel.send(AddEditTodoItemEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    // updates an already exisiting todoItem in the database
    private fun updateTask(todoItem: TodoItem) = viewModelScope.launch {
        todoItemDao.update(todoItem)
        addEditTodoItemEventChannel.send(AddEditTodoItemEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    //
    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTodoItemEventChannel.send(AddEditTodoItemEvent.ShowInvalidInputMessage(text))
    }

    // contains different events to be passed to the AddEditTodoItemViewModel
    sealed class AddEditTodoItemEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTodoItemEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTodoItemEvent()
    }
}