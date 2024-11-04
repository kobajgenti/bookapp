package com.example.bookapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookapp.network.Book
import com.example.bookapp.network.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> get() = _books

    private val _lastQuery = MutableStateFlow("Harry Potter")
    val lastQuery: StateFlow<String> = _lastQuery

    private var currentOffset = 0
    private var isLoading = false

    init {
        searchBooks("Harry Potter", reset = true)
    }

    fun updateLastQuery(query: String) {
        _lastQuery.value = query
    }

    fun searchBooks(
        query: String,
        reset: Boolean = false
    ) {
        if (isLoading) return

        if (reset) {
            currentOffset = 0
            _books.value = emptyList()
            updateLastQuery(query)
        }

        viewModelScope.launch {
            isLoading = true
            repository.searchBooks(query, currentOffset).collect { response ->
                _books.value = _books.value + response.books
                currentOffset += response.books.size
            }
            isLoading = false
        }
    }

    fun getBookById(id: String, onResult: (Book?) -> Unit) {
        viewModelScope.launch {
            val book = repository.getBookById(id)
            onResult(book)
        }
    }
}