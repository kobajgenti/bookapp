package com.example.bookapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.bookapp.network.Book
import com.example.bookapp.network.BookRepository
import com.example.bookapp.network.BookApi
import com.example.bookapp.viewmodel.BookViewModel
import com.example.bookapp.viewmodel.BookViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = BookApi.retrofitService
        val repository = BookRepository(apiService)
        val viewModelFactory = BookViewModelFactory(repository)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "search") {
                    composable("search") {
                        BookSearchScreen(navController = navController, viewModel = viewModel(factory = viewModelFactory))
                    }
                    composable("detail/{bookId}") { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId")
                        bookId?.let { id ->
                            BookDetailScreen(
                                viewModel = viewModel(factory = viewModelFactory),
                                bookId = id,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSearchScreen(navController: NavHostController, viewModel: BookViewModel) {
    val books by viewModel.books.collectAsState()
    val lastQuery by viewModel.lastQuery.collectAsState()
    var query by remember { mutableStateOf(lastQuery) }
    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(books) {
        if (books.isNotEmpty()) {
            isInitialLoad = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Search", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search Books") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            viewModel.searchBooks("", reset = true)
                        }) {
                            Text("×", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.searchBooks(query, reset = true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isInitialLoad -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                books.isEmpty() && query.isNotEmpty() -> {  // No results after search
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "No books found",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                books.isEmpty() -> {  // Initial empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Search for a Book",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Enter a title, author, or keyword above",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(books) { _, book ->
                            BookListItem(book = book) {
                                navController.navigate("detail/${book.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = book.info.images?.thumbnail?.replace("http:", "https:"),
                    contentDescription = "Book cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.info.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.info.authors?.joinToString(", ") ?: "Unknown author",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                book.info.publisher?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(viewModel: BookViewModel, bookId: String, navController: NavHostController) {
    var book by remember { mutableStateOf<Book?>(null) }

    LaunchedEffect(bookId) {
        viewModel.getBookById(bookId) { fetchedBook ->
            book = fetchedBook
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.info?.title ?: "Loading...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        if (book == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = book!!.info.images?.thumbnail?.replace("http:", "https:"),
                            contentDescription = "Book cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book!!.info.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book!!.info.authors?.joinToString(", ") ?: "Unknown author",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        book!!.info.publisher?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                book?.info?.description?.let { _ ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Description",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        book!!.info.getFormattedDescription(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                book?.info?.categories?.let { categories ->
                    if (categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            categories.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}