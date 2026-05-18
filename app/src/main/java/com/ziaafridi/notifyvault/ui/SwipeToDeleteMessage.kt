package com.ziaafridi.notifyvault.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeToDeleteMessage(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    // Animation state for swipe offset
    val offsetX = remember { Animatable(0f) }
    val deleteThreshold = with(density) { 80.dp.toPx() }
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Delete background
        if (offsetX.value < 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Message content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (abs(offsetX.value) > deleteThreshold) {
                                    // Trigger delete confirmation
                                    showDeleteConfirmation = true
                                    offsetX.animateTo(0f, tween(200))
                                } else {
                                    // Snap back
                                    offsetX.animateTo(0f, tween(200))
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        scope.launch {
                            val newValue = (offsetX.value + dragAmount).coerceIn(-deleteThreshold * 2, 0f)
                            offsetX.snapTo(newValue)
                        }
                    }
                }
        ) {
            content()
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to delete this message? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}