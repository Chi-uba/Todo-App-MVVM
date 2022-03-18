package com.example.chi_ubatodoapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

// Defined a table for sqlite storage using room
@Entity(tableName = "todo_item_table")
// making this class parcelable for easy passing of todoItem objects across fragments
@Parcelize
// craeting a model for each todo_item by declaring its properties
data class TodoItem(
    val name : String,
    val isImportant: Boolean = false,
    val isComplete: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    // defined a unique id for every todo_item object
    @PrimaryKey(autoGenerate = true) val id: Int = 0
): Parcelable {
    // formats the dateCreated variable to a UI friendly String format that's easily understandable
val createdDateFormatted: String
    get() = DateFormat.getTimeInstance().format(dateCreated)
}