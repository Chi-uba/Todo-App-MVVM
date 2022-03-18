package com.example.chi_ubatodoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chi_ubatodoapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

// creates an instance of a room database for saving list of todoItems in a table
@Database(entities = [TodoItem::class], version = 1)
abstract class TodoItemDatabase : RoomDatabase() {

    // gets an instance of the TodoItemDao
    abstract fun todoItemDao(): TodoItemDao

    class Callback @Inject constructor(
        private val database: Provider<TodoItemDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().todoItemDao()


           // creating and inserting dummy data to database created for initial
            // app build and code testing in the application scope
            applicationScope.launch {
                dao.insert(TodoItem("Wash the dishes"))
                dao.insert(TodoItem("Learn android kotlin coroutines"))
                dao.insert(TodoItem("Buy play station", isImportant = true))
                dao.insert(TodoItem("Prepare dinner for family", isComplete = true))
                dao.insert(TodoItem("Call dad to check up on him"))
                dao.insert(TodoItem("Visit a zoo during the weekend", isComplete = true))
                dao.insert(TodoItem("Repair my shoes"))
                dao.insert(TodoItem("Call Cristiano Ronaldo"))
            }
        }
    }
}