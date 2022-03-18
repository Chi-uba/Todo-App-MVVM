package com.example.chi_ubatodoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


// this is where we define how to interact with the sqlite database using room and flow for
// asyncronous flow of data
@Dao
interface TodoItemDao {
    // retrieves todoItems according to selected order(by date or by time) and if the completed task is hidden
    fun getTodoItem(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<TodoItem>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    @Query("SELECT * FROM todo_item_table WHERE (isComplete != :hideCompleted OR isComplete = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_item_table WHERE (isComplete != :hideCompleted OR isComplete = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC, dateCreated")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<TodoItem>>

    // inserts todoItems to the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todoItem:  TodoItem)

    // updates an alradey exisiting todoItems
    @Update
    suspend fun update(todoItem: TodoItem)

    //deletes the selected item from the database
    @Delete
    suspend fun delete(todoItem: TodoItem)

}