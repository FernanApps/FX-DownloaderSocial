package pe.fernan.downloader.social.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.fernan.downloader.social.BuildConfig
import pe.fernan.downloader.social.R
import pe.fernan.downloader.social.core.model.DataState
import pe.fernan.downloader.social.core.model.Download
import pe.fernan.downloader.social.core.model.Source
import pe.fernan.downloader.social.core.model.SourceItem
import pe.fernan.downloader.social.ui.MainViewModel
import pe.fernan.downloader.social.utils.ShareUtils
import pe.fernan.downloader.social.worker.DownloadWorker

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MainScreen(viewModel: MainViewModel) {

    val context = LocalContext.current

    val downloadUrl by viewModel.downloadUrl.observeAsState("")
    val dataVideo by viewModel.dataVideo.observeAsState()
    val loading by viewModel.loading.observeAsState()
    val downloadState by viewModel.downloadStatus.observeAsState()
    val sources by viewModel.sources.observeAsState()
//val sources: List<Source>? = dataVideo?.sources
//    LaunchedEffect(dataVideo) {
//        sources = dataVideo?.sources?.map {
//            SourceItem(it.url, it.title, it.type, false)
//        }?.toMutableList()
//    }
    val clipboardManager = LocalClipboardManager.current

    var enableButttonDownload by rememberSaveable {
        mutableStateOf(true)

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            modifier = Modifier.padding(25.dp),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            colorFilter = ColorFilter.tint(
                color = colorResource(id = R.color.ic_launcher_background)
            )
        )
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        Text(text = versionName.toString(), modifier = Modifier.padding(8.dp))



        ClipboardTextField(
            value = downloadUrl,
            onValueChange = { viewModel.setDownloadUrl(it) },
            onPasteClick = {
                enableButttonDownload = true
                clipboardManager
                    .getText()
                    ?.let { clipboardText ->
                        viewModel.setDownloadUrl(clipboardText.toString())
                    }
            },
            label = {
                Text(text = "Paste link here")
            }
        )

        Button(
            onClick = {
                if (downloadUrl.isNotEmpty()) {
                    enableButttonDownload = false
                    viewModel.downloadVideo()
                }
            },
            enabled = enableButttonDownload,
            modifier = Modifier.fillMaxWidth(),
            shape = shape
        ) {
            Text(text = "Download")
        }


        //Text(text = tiktokUrl)
        Spacer(modifier = Modifier.size(25.dp))
        if (loading == null) {
            Text(text = "None Sources")
        } else {
            if (loading!!) {
                CircularProgressBar()
            } else {
                if (dataVideo != null && sources != null) {
                    Text(
                        text = "Available Sources",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(15.dp)
                    )
                    SourceList(sourceList = sources!!) { item ->
                        if (item.download.isDownload) {
                            ShareUtils.videoTo(
                                videoFile = DownloadWorker.getPathVideo(
                                    context, dataVideo!!.id + item.source.extension
                                ),
                                context = context
                            )
                        } else {
                            viewModel.startWork(
                                authorName = dataVideo!!.author.name,
                                fileName = dataVideo!!.id,
                                item.source
                            )

                        }

                    }
                }
            }
        }


        if (downloadState != null) {
            when (downloadState) {
                is DataState.Error -> TODO()
                DataState.Finished -> TODO()
                DataState.Loading -> TODO()
                is DataState.Progress -> TODO()
                is DataState.Success -> TODO()
                null -> TODO()
                else -> {}
            }


        }

    }


}


@Composable
fun CircularProgressBar() {
    Box(
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Composable
fun SourceList(
    modifier: Modifier = Modifier,
    sourceList: List<SourceItem>,
    onItemClick: (SourceItem) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        items(sourceList) { source ->
            SourceItem(item = source, onItemClick = onItemClick)
        }
    }
}


@Composable
fun SourceItem(item: SourceItem, onItemClick: (SourceItem) -> Unit) {

    println("item Up :::::: ${item.download} :::: ")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        ProgressButton(
            title = item.source.title,
            isDownload = item.download.isDownload,
            progress = item.download.progress,
            onClick = {
                onItemClick(item)
            },
            shape = shape,
        )
    }

}

@Composable
fun ProgressButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDownload: Boolean,
    progress: Int = 0,
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = Color.White,
    progressColors: List<Color> = listOf(
        colorResource(id = R.color.gradient_start),
        colorResource(id = R.color.gradient_end)
    ),
    finishColor: Color = colorResource(id = R.color.gradient_end),
    shape: Shape,
) {

    val isFinishDownload = isDownload
    val statusProgress = progress / 100.toFloat()
    val progressState = remember { mutableStateOf(0.0f) }

//    when (status) {
//        is DataState.Error -> Unit
//        DataState.Finished -> Unit
//        DataState.Initial -> Unit
//        DataState.Loading -> {
//            LaunchedEffect(Unit) {
//                while (true) {
//                    delay(500)
//                    progressState.value += 0.025f
//                    if (progressState.value > 1.1f) {
//                        progressState.value = 0.0f
//                    }
//                    progress = progressState.value
//                }
//            }
//        }
//
//        is DataState.Progress -> {
//            progress = status.progress / 100.toFloat()
//        }
//
//        is DataState.Success -> {
//            isFinishDownload = status.data
//        }
//    }


    val modifier = modifier.padding(
        horizontal = 18.dp,
        vertical = 12.dp
    )

    val gradientProgressBrush = Brush.horizontalGradient(progressColors)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .fillMaxWidth()
            .padding(0.dp)
            .background(if (isFinishDownload) finishColor else containerColor)
            .clickable {
                onClick()
            },
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .fillMaxWidth(statusProgress)
                .background(brush = gradientProgressBrush),
        ) {
            Text(text = "", modifier = modifier)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            modifier = modifier.weight(0.8f),
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
        if (isFinishDownload) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Icon",
                modifier = Modifier
                    .weight(0.2f)
                    .size(18.dp),
                tint = contentColor
            )
        }
    }

}

val shape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    label: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = shape,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        maxLines = 1,
        label = label,
        textStyle = textStyle,
        trailingIcon = {
            Box(
                modifier = Modifier.padding(end = 10.dp)
            ) {

                val shape = RoundedCornerShape(10.dp)
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = shape
                        )
                        .clip(shape)
                        .clickable {
                            onPasteClick()
                        }
                        .padding(horizontal = 15.dp, vertical = 8.dp),
                    text = "Pegar"
                )
            }
        }
    )
}


fun invertedColor(colorToInvert: Color) = colorToInvert.copy(
    red = 1.0f - colorToInvert.red,
    green = 1.0f - colorToInvert.green,
    blue = 1.0f - colorToInvert.blue
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
fun TEs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var downloadUrl by remember {
            mutableStateOf("")
        }


        Image(
            modifier = Modifier.padding(25.dp),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            colorFilter = ColorFilter.tint(
                color = colorResource(id = R.color.ic_launcher_background)
            )
        )


        ClipboardTextField(
            value = downloadUrl,
            onValueChange = { downloadUrl = it },
            onPasteClick = {

            },
            label = {
                Text(text = "Paste link here")
            }
        )

        Button(
            onClick = {

            },
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
            shape = shape
        ) {
            Text(text = "Download")
        }


        SourceList(
            modifier = Modifier.fillMaxWidth(),
            sourceList = listOf(
                SourceItem(source = Source("url", "title"), download = Download()),
                SourceItem(source = Source("url", "title"), download = Download())

            ), onItemClick = {

            }
        )
    }

    return

    val colorList = listOf(
        "primary" to MaterialTheme.colorScheme.primary,
        "onPrimary" to MaterialTheme.colorScheme.onPrimary,
        "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
        "onPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
        "inversePrimary" to MaterialTheme.colorScheme.inversePrimary,
        "secondary" to MaterialTheme.colorScheme.secondary,
        "onSecondary" to MaterialTheme.colorScheme.onSecondary,
        "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
        "onSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
        "tertiary" to MaterialTheme.colorScheme.tertiary,
        "onTertiary" to MaterialTheme.colorScheme.onTertiary,
        "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
        "onTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
        "background" to MaterialTheme.colorScheme.background,
        "onBackground" to MaterialTheme.colorScheme.onBackground,
        "surface" to MaterialTheme.colorScheme.surface,
        "onSurface" to MaterialTheme.colorScheme.onSurface,
        "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
        "onSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
        "surfaceTint" to MaterialTheme.colorScheme.surfaceTint,
        "inverseSurface" to MaterialTheme.colorScheme.inverseSurface,
        "inverseOnSurface" to MaterialTheme.colorScheme.inverseOnSurface,
        "error" to MaterialTheme.colorScheme.error,
        "onError" to MaterialTheme.colorScheme.onError,
        "errorContainer" to MaterialTheme.colorScheme.errorContainer,
        "onErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
        "outline" to MaterialTheme.colorScheme.outline,
        "outlineVariant" to MaterialTheme.colorScheme.outlineVariant,
        "scrim" to MaterialTheme.colorScheme.scrim
    )

    LazyVerticalGrid(
        modifier = Modifier.padding(top = 48.dp),
        columns = GridCells.Fixed(2)
    ) {
        items(colorList) {
            Text(
                text = it.first,
                color = invertedColor(it.second),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(it.second)
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            )
        }
    }


//    Box(
//        Modifier
//            .padding(25.dp)
//            .fillMaxSize()
//            .indication(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = rememberRipple(bounded = true)
//            ),
//    ) {
//
//
//
//    }


//        val progressState = remember { mutableStateOf(0.0f) }
//
//        // Ejecutar un coroutine que actualice el progreso cada 100 milisegundos
//        LaunchedEffect(Unit) {
//            while (true) {
//                delay(500)
//                progressState.value += 0.05f
//                if (progressState.value > 1.1f) {
//                    progressState.value = 0.0f
//                }
//            }
//        }
//
//        CustomButton(
//            onClick = { /* Acción del botón */ },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp),
//            progress = progressState.value
//        ){
//            Text(text = "title")
//            if (true) {
//                Icon(
//                    imageVector = Icons.Default.Share,
//                    contentDescription = "Icon",
//                    modifier = Modifier.padding(start = 10.dp)
//                )
//            }
//        }
//    }

}



