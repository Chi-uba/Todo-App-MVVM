package com.example.chi_ubatodoapp.di


import android.app.Application
import androidx.room.Room
import com.example.chi_ubatodoapp.data.TodoItemDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// giving dagger hilt instruction on how to create dependencies needed using a module
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton // ensures there is only one instance of this module method
    fun provideDatabase(
        app: Application,
        callback: TodoItemDatabase.Callback
    ) = Room.databaseBuilder(app, TodoItemDatabase::class.java, "todo_item_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()


    // provides the todoItem dao we will need to carry out database operations
    @Provides
    fun provideTodoItemDao(db: TodoItemDatabase) = db.todoItemDao()

    @ApplicationScope// explicitly defining the scope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope