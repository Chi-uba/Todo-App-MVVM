package com.example.chi_ubatodoapp.screen_fragment.home



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chi_ubatodoapp.data.TodoItem
import com.example.chi_ubatodoapp.databinding.TodoItemTileBinding

class HomeAdapter(private val listener: OnItemClickListener) : ListAdapter<TodoItem, HomeAdapter.TodoItemViewHolder>(
    DiffCallback()
) {
// defines how we instantiate the binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val binding = TodoItemTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoItemViewHolder(binding)
    }

    // defines how we bind the data to the viewholder
    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TodoItemViewHolder(private val binding: TodoItemTileBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkboxTodoItemState.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkboxTodoItemState.isChecked)
                    }
                }
            }
        }
// this function connects todoItem properties to the state of the todoItem layout UI element
        fun bind(todoItem: TodoItem) {
            binding.apply {
                checkboxTodoItemState.isChecked = todoItem.isComplete
                todoItemName.text = todoItem.name
                todoItemName.paint.isStrikeThruText = todoItem.isComplete
                icImportance.isVisible = todoItem.isImportant
            }
        }
    }
    interface OnItemClickListener {
        fun onItemClick(task: TodoItem)
        fun onCheckBoxClick(task: TodoItem, isChecked: Boolean)
    }
// tells the homeAdapter class how to detect changes to a new list from the flow
    class DiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem == newItem
    }
}