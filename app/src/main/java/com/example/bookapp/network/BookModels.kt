package com.example.bookapp.network

import com.squareup.moshi.Json

data class BookResponse(
    @Json(name = "items") val books: List<Book> = emptyList()
)

data class Book(
    val id: String,
    @Json(name = "volumeInfo") val info: VolumeInfo
)

data class VolumeInfo(
    val title: String = "",
    val authors: List<String>? = null,
    val description: String? = null,
    @Json(name = "imageLinks") val images: ImageLinks? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val categories: List<String>? = null,
    val averageRating: Double? = null
) {
    fun getFormattedDescription(): String {
        return description?.replace(Regex("<[^>]*>"), "") ?: ""
    }
}

data class ImageLinks(
    val thumbnail: String? = null,
    val smallThumbnail: String? = null
)

data class SearchInfo(
    val textSnippet: String? = null
)