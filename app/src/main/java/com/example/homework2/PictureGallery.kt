import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

data class Picture(
    val id: Int,
    val author: String,
    val url: String
)
private var nextId = 1

//Тестовые данные
fun generateSamplePictures(): List<Picture> {
    return listOf(
        Picture(1, "Иван Петров", "https://placehold.net/1.png"),
        Picture(2, "Анна Смирнова", "https://placehold.net/2.png"),
        Picture(3, "Сергей Волков", "https://placehold.net/3.png"),
        Picture(4, "Мария Кузнецова", "https://placehold.net/4.png"),
        Picture(5, "Иван Иванов", "https://placehold.net/5.png")
    )
}


/**
 * Генерирует новую случайную картинку с уникальными ID и URL.
 * Использует placehold.net с уникальным seed для генерации, что
 * гарантирует уникальный URL при каждом вызове.
 */
fun generateNewPicture(): Picture {
    val newAuthor = listOf("Художник A", "Фотограф B", "Мастер C").random()
    val newPicture = Picture(
        id = nextId,
        author = newAuthor,
        url = "https://placehold.net/${nextId}.png"
    )
    nextId++
    return newPicture
}


@Composable
fun PictureGalleryApp() {
    // Основное состояние галереи
    val gallery = remember { mutableStateListOf<Picture>().apply { addAll(generateSamplePictures()) } }

    // Состояние для поиска
    var searchText by remember { mutableStateOf("") }

    // Состояние для режима отображения
    var isGridMode by remember { mutableStateOf(false) } // false = Список, true = Сетка

    // Фильтрация списка (Задание 1)
    val filteredGallery = gallery.filter {
        it.author.contains(searchText, ignoreCase = true) // Поиск без учета регистра
    }

    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                // Поле поиска (Задание 1)
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Поиск по автору") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить поиск")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Кнопка переключения режимов (Задание 2)
                    Button(onClick = { isGridMode = !isGridMode }) {
                        val icon = if (isGridMode) Icons.Default.List else Icons.Default.GridView
                        val text = if (isGridMode) "Список" else "Сетка"
                        Icon(icon, contentDescription = text)
                        Spacer(Modifier.width(8.dp))
                        Text(text)
                    }

                    // Кнопка "Очистить всё" (Задание 3)
                    Button(
                        onClick = { gallery.clear() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Очистить всё")
                    }
                }
            }
        },
        floatingActionButton = {
            // Кнопка "+" для добавления
            FloatingActionButton(onClick = {
                addNewPicture(gallery) // Логика добавления с проверкой (Задание 4)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить картинку")
            }
        }
    ) { paddingValues ->
        // Условное отображение списка или сетки (Задание 2)
        if (isGridMode) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
            ) {
                items(filteredGallery, key = { it.id }) { picture ->
                    PictureCard(picture = picture) {
                        // Удаление при нажатии на картинку
                        gallery.remove(picture)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
            ) {
                items(filteredGallery, key = { it.id }) { picture ->
                    PictureCard(picture = picture) {
                        // Удаление при нажатии на картинку
                        gallery.remove(picture)
                    }
                }
            }
        }
    }
}

/**
 * Логика добавления новой картинки с проверкой на существование (Задание 4)
 */
fun addNewPicture(gallery: SnapshotStateList<Picture>) {
    val newPicture = generateNewPicture()

    // Проверка на существование по url ИЛИ id
    // Картинка не добавляется, если уже есть элемент с таким же url или id.
    val exists = gallery.any { it.url == newPicture.url || it.id == newPicture.id }

    if (!exists) {
        gallery.add(newPicture)
    }
}

/**
 * Компонент карточки картинки
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PictureCard(picture: Picture, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick), // Удаление при нажатии
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Изображение, загружаемое с помощью Glide
            GlideImage(
                model = picture.url,
                contentDescription = picture.author,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) //
            )

            // Подпись
            Text(
                text = picture.author,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(), // Используем стандартную светлую схему
        content = content
    )
}

@Preview(showBackground = true, name = "Галерея в режиме Списка")
@Composable
fun GalleryListPreview() {
    AppTheme {
        PictureGalleryApp()
    }
}

@Preview(showBackground = true, name = "Галерея в режиме Сетки (Grid)")
@Composable
fun GalleryGridPreview() {
    AppTheme {
        GalleryGridModePreview()
    }
}

@Composable
fun GalleryGridModePreview() {
    val gallery = remember { mutableStateListOf<Picture>().apply { addAll(generateSamplePictures()) } }
    var searchText by remember { mutableStateOf("") }

    val isGridMode = true

    val filteredGallery = gallery.filter {
        it.author.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {},
        floatingActionButton = {}
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            items(filteredGallery, key = { it.id }) { picture ->
                PictureCard(picture = picture) {
                }
            }
        }
    }
}