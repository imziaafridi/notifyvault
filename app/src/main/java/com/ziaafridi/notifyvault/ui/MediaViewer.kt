package com.ziaafridi.notifyvault.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.ziaafridi.notifyvault.MediaManager
import com.ziaafridi.notifyvault.RecoveredMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

@Composable
fun MediaViewer(
    message: RecoveredMessage,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    val context = LocalContext.current
    val rawType =
        message.mediaType?.let { runCatching { MediaManager.MediaType.valueOf(it) }.getOrNull() }
            ?: MediaManager.MediaType.UNKNOWN
    val mediaType = inferMediaTypeFromPath(message.mediaPath, rawType)

    if (!message.mediaPath.isNullOrEmpty()) {
        when (mediaType) {
            MediaManager.MediaType.IMAGE -> {
                ImageViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }


            MediaManager.MediaType.VIDEO -> {
                VideoViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    context = context,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }

            MediaManager.MediaType.GIF -> {
                GifViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    context = context,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }

            MediaManager.MediaType.AUDIO -> {
                AudioViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    context = context,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }

            MediaManager.MediaType.DOCUMENT -> {
                DocumentViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    context = context,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }

            MediaManager.MediaType.STICKER -> {
                EmojiViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }

            else -> {
                GenericMediaViewer(
                    mediaPath = message.mediaPath,
                    fileName = message.mediaFileName,
                    context = context,
                    modifier = modifier,
                    disableInteractions = disableInteractions,
                )
            }
        }
    } else {
        // Show media placeholder if no file found
        MediaPlaceholder(
            mediaType = mediaType,
            fileName = message.mediaFileName,
            modifier = modifier
        )
    }
}

@Composable
private fun ImageViewer(
    mediaPath: String,
    fileName: String?,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    var showFullscreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !disableInteractions) { showFullscreen = true }
    ) {
        AsyncImage(
            model = File(mediaPath),
            contentDescription = fileName ?: "Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay with file info
//        if (fileName != null) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .background(
//                        Color.Black.copy(alpha = 0.6f),
//                        RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp)
//                    )
//                    .padding(4.dp)
//            ) {
//                Text(
//                    text = fileName,
//                    color = Color.White,
//                    fontSize = 10.sp,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
    }

    if (showFullscreen) {
        FullscreenImageDialog(
            mediaPath = mediaPath,
            contentDescription = fileName ?: "Image",
            onDismiss = { showFullscreen = false }
        )
    }
}

@Composable
private fun VideoViewer(
    mediaPath: String,
    fileName: String?,
    context: Context,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    var showPlayer by remember { mutableStateOf(false) }

    val thumbnail: Bitmap? = remember(mediaPath) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(File(mediaPath), Size(512, 512), null)
            } else {
                @Suppress("DEPRECATION")
                ThumbnailUtils.createVideoThumbnail(
                    mediaPath,
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
            }
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable(enabled = !disableInteractions) { showPlayer = true }
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = fileName ?: "Video",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Video"
            )
        }
    }

    if (showPlayer) {
        FullscreenVideoDialog(
            context = context,
            mediaPath = mediaPath,
            onDismiss = { showPlayer = false }
        )
    }
}

@Composable
private fun AudioViewer(
    mediaPath: String,
    fileName: String?,
    context: Context,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var durationMs by remember { mutableStateOf(0) }
    var positionMs by remember { mutableStateOf(0) }
    val isVoiceNote = remember(fileName) { fileName?.contains("PTT", ignoreCase = true) == true }

    val totalDurationMs: Int = remember(mediaPath) {
        runCatching {
            val r = MediaMetadataRetriever()
            try {
                r.setDataSource(mediaPath)
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
            } finally {
                runCatching { r.release() }
            }
        }.getOrDefault(0)
    }

    DisposableEffect(mediaPath) {
        onDispose {
            runCatching {
                mediaPlayer?.release()
            }
            mediaPlayer = null
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    runCatching { mediaPlayer?.pause() }
                    isPlaying = false
                } else {
                    val player = mediaPlayer ?: createMediaPlayer(context, mediaPath).also { mp ->
                        mediaPlayer = mp
                        durationMs = runCatching { mp.duration }.getOrDefault(totalDurationMs)
                        mp.setOnCompletionListener {
                            isPlaying = false
                            positionMs = 0
                        }
                    }
                    runCatching {
                        player.start()
                        isPlaying = true
                    }
                }
            },
            modifier = Modifier.size(40.dp),
            enabled = !disableInteractions,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause Audio" else "Play Audio",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Slider(
            value = if (durationMs > 0) (positionMs.toFloat() / durationMs) else 0f,
            onValueChange = { fraction ->
                val target = (fraction * durationMs).toInt()
                runCatching {
                    mediaPlayer?.seekTo(target)
                    positionMs = target
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .graphicsLayer(scaleY = 0.7f),
            enabled = !disableInteractions,
            // Added colors to make the slim slider visible
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Right badge similar to the reference (mic + duration)
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isVoiceNote) Icons.Default.Mic else Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(20.dp),
            )
            Text(
                text = formatTime((durationMs.takeIf { it > 0 } ?: totalDurationMs)),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && mediaPlayer != null) {
            positionMs = runCatching { mediaPlayer?.currentPosition ?: 0 }.getOrDefault(0)
            delay(250)
        }
    }
}

@Composable
private fun DocumentViewer(
    mediaPath: String,
    fileName: String?,
    context: Context,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .clickable { openMediaFile(context, mediaPath) },
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//    ) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !disableInteractions) { openMediaFile(context, mediaPath) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "Document",
            modifier = Modifier
                .size(50.dp)
                .background(
                    MaterialTheme.colorScheme.tertiary,
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = fileName ?: "Document",
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

//        Icon(
//            imageVector = Icons.Default.Launch, // Using Launch instead of OpenInNew
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.tertiary
//        )
    }
    //   }
}

@Composable
private fun EmojiViewer(
    mediaPath: String,
    fileName: String?,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    var showFullscreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clickable(enabled = !disableInteractions) { showFullscreen = true },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(mediaPath),
                contentDescription = fileName ?: "Emoji",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }

    if (showFullscreen) {
        FullscreenImageDialog(
            mediaPath = mediaPath,
            contentDescription = fileName ?: "Emoji",
            onDismiss = { showFullscreen = false }
        )
    }
}

@Composable
private fun GenericMediaViewer(
    mediaPath: String,
    fileName: String?,
    context: Context,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !disableInteractions) { openMediaFile(context, mediaPath) }
            .padding(vertical = 8.dp),
        //  verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Attachment,
            contentDescription = "Media File"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = fileName ?: "Media File",
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Launch,
            contentDescription = null
        )
    }
}

@Composable
private fun MediaPlaceholder(
    mediaType: MediaManager.MediaType,
    fileName: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (mediaType) {
                    MediaManager.MediaType.IMAGE -> Icons.Default.Image
                    MediaManager.MediaType.VIDEO -> Icons.Default.Movie // Using Movie instead of VideoFile
                    MediaManager.MediaType.AUDIO -> Icons.Default.MusicNote // Using MusicNote instead of AudioFile
                    MediaManager.MediaType.DOCUMENT -> Icons.Default.Description
                    else -> Icons.Default.Attachment // Using Attachment instead of AttachFile
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName ?: "${
                        mediaType.name.lowercase().replaceFirstChar { it.uppercase() }
                    } file",
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "Media file not found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun openMediaFile(context: Context, mediaPath: String) {
    try {
        val file = File(mediaPath)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(file.extension))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error - could show a toast or log
    }
}

private fun toContentUri(context: Context, mediaPath: String) =
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(mediaPath)
    )

private fun createMediaPlayer(context: Context, mediaPath: String): MediaPlayer {
    val uri = toContentUri(context, mediaPath)
    return MediaPlayer().apply {
        setDataSource(context, uri)
        prepare()
    }
}

@Composable
private fun FullscreenImageDialog(
    mediaPath: String,
    contentDescription: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FullscreenPreviewScaffold(
                onDismiss = onDismiss,
                onDownload = {
                    val savedTo = downloadToDownloads(context = context, mediaPath = mediaPath)
                    if (savedTo != null) {
                        scope.launch { snackbarHostState.showSnackbar("Saved to $savedTo") }
                    }
                },
                onShare = { shareMedia(context, mediaPath) }
            ) { _, _ ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues()),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(mediaPath),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 72.dp)
            )
        }
    }
}

@Composable
private fun FullscreenVideoDialog(
    context: Context,
    mediaPath: String,
    onDismiss: () -> Unit
) {
    val uri = remember(mediaPath) { toContentUri(context, mediaPath) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var durationMs by remember { mutableStateOf(0) }
    var positionMs by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {

        val view = LocalView.current
        LaunchedEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.let {
                // Force solid black background for the window
                it.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))

                // Make the underlying dim layer fully opaque/solid
                it.setDimAmount(1f)

                // Stretch window to exact screen boundaries
                it.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        // Ensure the layout is fills the screen witn black clr
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            FullscreenPreviewScaffold(
                onDismiss = onDismiss,
                onDownload = {
                    val savedTo = downloadToDownloads(context, mediaPath)
                    if (savedTo != null) {
                        scope.launch { snackbarHostState.showSnackbar("Saved to $savedTo") }
                    }
                },
                onShare = { shareMedia(context, mediaPath) },
                showDefaultActions = false
            ) { overlayVisible, _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(uri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    durationMs = mp.duration.coerceAtLeast(0)
                                    isPlaying = true
                                    start()
                                }
                                videoView = this
                            }
                        },
                        update = { /* don't reset URI on recomposition */ }
                    )

                    if (overlayVisible) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.80f))
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val vv = videoView
                                    if (vv != null) {
                                        if (vv.isPlaying) {
                                            vv.pause()
                                            isPlaying = false
                                        } else {
                                            vv.start()
                                            isPlaying = true
                                        }
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Slider(
                                value = if (durationMs > 0) (positionMs.toFloat() / durationMs) else 0f,
                                onValueChange = { fraction ->
                                    val vv = videoView ?: return@Slider
                                    val target = (fraction * durationMs).toInt()
                                    runCatching {
                                        vv.seekTo(target)
                                        positionMs = target
                                    }
                                },
                                modifier = Modifier
                                    .weight(.5f)
                                    .height(20.dp)
                                    .graphicsLayer(scaleY = 0.7f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val savedTo = downloadToDownloads(context, mediaPath)
                                        if (savedTo != null) {
                                            scope.launch { snackbarHostState.showSnackbar("Saved to $savedTo") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(onClick = { shareMedia(context, mediaPath) }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Align the snackbar correctly within the Box
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp)
            )
        }
    }

    LaunchedEffect(videoView, durationMs) {
        while (videoView != null) {
            val vv = videoView ?: break
            if (vv.isPlaying) {
                positionMs = runCatching { vv.currentPosition }.getOrDefault(0)
                isPlaying = true
            } else {
                isPlaying = false
            }
            delay(250)
        }
    }
}

@Composable
private fun GifViewer(
    mediaPath: String,
    fileName: String?,
    context: Context,
    modifier: Modifier = Modifier,
    disableInteractions: Boolean = false,
) {
    var showFullscreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .clickable(enabled = !disableInteractions) { showFullscreen = true },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(mediaPath),
            contentDescription = fileName ?: "GIF",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp),
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(72.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.60f),
                    RoundedCornerShape(36.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "GIF",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (showFullscreen) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            )
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            Box(modifier = Modifier.fillMaxSize()) {
                FullscreenPreviewScaffold(
                    onDismiss = { showFullscreen = false },
                    onDownload = {
                        val savedTo = downloadToDownloads(context, mediaPath)
                        if (savedTo != null) {
                            scope.launch { snackbarHostState.showSnackbar("Saved to $savedTo") }
                        }
                    },
                    onShare = { shareMedia(context, mediaPath) }
                ) { _, _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(WindowInsets.systemBars.asPaddingValues()),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(mediaPath),
                            contentDescription = fileName ?: "GIF",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 72.dp)
                )
            }
        }
    }
}

@Composable
private fun FullscreenPreviewScaffold(
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    showDefaultActions: Boolean = true,
    content: @Composable (overlayVisible: Boolean, toggleOverlay: () -> Unit) -> Unit
) {
    var overlayVisible by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { overlayVisible = !overlayVisible }
    ) {
        content(overlayVisible) { overlayVisible = !overlayVisible }

        if (overlayVisible) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (showDefaultActions && overlayVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.primary,

                        )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,

                        )
                }
            }
        }
    }
}

private fun shareMedia(context: Context, mediaPath: String) {
    runCatching {
        val file = File(mediaPath)
        if (!file.exists()) return
        val uri = toContentUri(context, mediaPath)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = getMimeType(file.extension)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}

private fun downloadToDownloads(context: Context, mediaPath: String): String? {
    return runCatching {
        val src = File(mediaPath)
        if (!src.exists()) return@runCatching null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, src.name)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(src.extension))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri =
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return@runCatching null
            context.contentResolver.openOutputStream(uri)?.use { out ->
                FileInputStream(src).use { input ->
                    input.copyTo(out)
                }
            }
            "Downloads/${src.name}"
        } else {
            null
        }
    }.getOrNull()
}

private fun inferMediaTypeFromPath(
    mediaPath: String?,
    fallback: MediaManager.MediaType
): MediaManager.MediaType {
    val ext = mediaPath?.substringAfterLast('.', "")?.lowercase().orEmpty()
    return when (ext) {
        "jpg", "jpeg", "png", "bmp" -> MediaManager.MediaType.IMAGE
        "webp" -> MediaManager.MediaType.STICKER
        "gif" -> MediaManager.MediaType.GIF
        "mp4", "3gp", "mkv", "avi", "mov", "webm" -> MediaManager.MediaType.VIDEO
        "mp3", "aac", "m4a", "opus", "amr", "ogg", "wav" -> MediaManager.MediaType.AUDIO
        "pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx" -> MediaManager.MediaType.DOCUMENT
        else -> fallback
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun getMimeType(extension: String): String {
    return when (extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "mp4" -> "video/mp4"
        "3gp" -> "video/3gpp"
        "avi" -> "video/x-msvideo"
        "mov" -> "video/quicktime"
        "mp3" -> "audio/mpeg"
        "aac" -> "audio/aac"
        "ogg" -> "audio/ogg"
        "opus" -> "audio/opus"
        "amr" -> "audio/amr"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "txt" -> "text/plain"
        else -> "*/*"
    }
}