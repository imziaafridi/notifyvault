package com.ziaafridi.notifyvault.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RememberScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B0000), // Dark red at top
                        Color(0xFF4A0000)  // Darker red at bottom
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // Title
        Text(
            text = context.getString(R.string.onboarding_remember_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // First disclaimer
        Text(
            text = context.getString(R.string.onboarding_remember_silenced_chat),
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Second disclaimer
        Text(
            text = context.getString(R.string.onboarding_remember_before_install),
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Get Started button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = context.getString(R.string.onboarding_get_started),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (index == 3) Color.White else Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}