package com.example.chi_ubatodoapp.data


import android.content.Context
import android.util.Log
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Defines the error tag for when an exception is thrown while retrieving data from datastore
private const val TAG = "PreferencesManager"

//Defines the two options of arranging the todoList been displayed
enum class SortOrder { BY_NAME, BY_DATE }

// saves the user's filter preferences of the todoList been displayed
data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

@Singleton // ensures only a single instance of this class is created
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
// creates an instance of datastore object
    private val dataStore = context.createDataStore("user_preferences")

    // retrieves sortOrder and hideCompleted value saved in the datastore instance created
    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }

    // updates the value of sortOrder saved in the datastore
    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    // updates the value of hideCompleted saved in the datastore
    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    // defines the preference keys for uniquely storing the sortOrder and hideCompleted values
    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}