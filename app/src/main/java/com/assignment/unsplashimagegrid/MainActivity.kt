package com.assignment.unsplashimagegrid


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.InputStream
import java.net.URL



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val unsplashApi = retrofit.create(UnsplashApi::class.java)

            var photos by remember { mutableStateOf<List<UnsplashPhoto>>(emptyList()) }

            LaunchedEffect(Unit) {
                photos = unsplashApi.getPhotos(perPage = 10000, page = 1) // Fetch first page
            }

            PhotoGrid(photos)
        }
    }
}

@Composable
fun PhotoGrid(photos: List<UnsplashPhoto>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val columnWidth = (screenWidth / 3).coerceAtLeast(128.dp) // Adjust the min column width as needed

    val listState = rememberLazyListState()

    LazyVerticalGrid(

        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp)
    ) {

        items(photos) { photo ->
            ImageItem(photo = photo, columnWidth = columnWidth)

        }
    }
}


/*suspend fun loadImage(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = URL(url).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}*/

@Composable
fun ImageItem(photo: UnsplashPhoto, columnWidth: Dp) {
    var bitmap by remember(photo) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(photo) {
        bitmap?.let { return@LaunchedEffect }
        try {
            val loadedBitmap = loadAndCacheImage(photo.urls.regularUrl)
            bitmap = loadedBitmap
        } catch (e: Exception) {
            // Handle error gracefully
            e.printStackTrace()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f)
                .width(columnWidth),
            contentScale = ContentScale.Crop
        )
    }
}

object BitmapCache {
    private val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 8
    private val cache = LruCache<String, Bitmap>(cacheSize)

    fun getBitmapFromMemoryCache(url: String): Bitmap? {
        return cache.get(url)
    }

    fun addBitmapToMemoryCache(url: String, bitmap: Bitmap) {
        if (getBitmapFromMemoryCache(url) == null) {
            cache.put(url, bitmap)
        }
    }
}

suspend fun loadAndCacheImage(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val cachedBitmap = BitmapCache.getBitmapFromMemoryCache(url)
            if (cachedBitmap != null) {
                return@withContext cachedBitmap
            }
            val inputStream: InputStream = URL(url).openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            BitmapCache.addBitmapToMemoryCache(url, bitmap)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
