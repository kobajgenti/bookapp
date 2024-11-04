package com.example.bookapp.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class BookRepository(private val apiService: BookApiService) {

    fun searchBooks(
        query: String,
        offset: Int = 0
    ): Flow<BookResponse> = flow {
        val response = apiService.searchBooks(query, startIndex = offset)
        emit(response)
    }.catch { e ->
        e.printStackTrace()
    }

    suspend fun getBookById(id: String): Book? {
        return try {
            apiService.getBookById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}